package com.example.storeservice.chat;

import com.example.storeservice.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatResponseListener implements SmartLifecycle, InitializingBean {

    private final StringRedisTemplate redis;
    private final ObjectMapper om;
    private final ReviewService reviewService;

    @Value("${app.chat.responseStream}") private String responseStream;
    @Value("${app.chat.responseKeyPrefix}") private String respKeyPrefix;
    @Value("${app.chat.responseTtlSeconds}") private int respTtlSec;
    @Value("${app.chat.req2qidPrefix:chat:req2qid:}") private String req2qidPrefix;

    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;
    private ExecutorService executor;
    private volatile boolean running = false;

    @Override
    public void afterPropertiesSet() {
        this.executor = Executors.newFixedThreadPool(
                4, new CustomizableThreadFactory("chat-resp-")
        );

        StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainerOptions.<String, MapRecord<String, String, String>>builder()
                        .batchSize(10)
                        .executor(this.executor)
                        .pollTimeout(Duration.ofSeconds(2))
                        .build();

        this.container = StreamMessageListenerContainer.create(redis.getConnectionFactory(), options);

        // 컨슈머 그룹 생성 (이미 있으면 예외 무시)
        try {
            redis.opsForStream().createGroup(responseStream, ReadOffset.latest(), "resp-group");
            log.info("Created consumer group 'resp-group' on stream '{}'", responseStream);
        } catch (Exception e) {
            log.info("Consumer group may already exist: {}", e.getMessage());
        }

        // 메시지 수신 핸들러 등록
        this.container.receive(
                Consumer.from("resp-group", "store-service"),
                StreamOffset.create(responseStream, ReadOffset.lastConsumed()),
                (MapRecord<String, String, String> message) -> {
                    try {
                        // FastAPI에서 보낸 JSON 필드 읽기
                        String requestId = message.getValue().get("request_id");
                        String answer = message.getValue().get("answer");
                        log.info("응답 수신: requestId={}, answer={}",  requestId, answer);

                        if (requestId != null && answer != null) {
                            // 1) 캐시에 저장 (동기 폴링 대비)
                            redis.opsForValue().set(respKeyPrefix + requestId, answer, Duration.ofSeconds(respTtlSec));

                            // 2) requestId → questionId 매핑 찾아서 DB 업데이트
                            String qidStr = redis.opsForValue().get(req2qidPrefix + requestId);
                            if (qidStr != null) {
                                reviewService.updateAnswer(UUID.fromString(qidStr), answer);
                                // 매핑키는 정리해도 됨
                                redis.delete(req2qidPrefix + requestId);
                            } else {
                                log.warn("No mapping found for requestId={}, skip DB update", requestId);
                            }
                        }

                        // ACK
                        redis.opsForStream().acknowledge(responseStream, "resp-group", message.getId());

                    } catch (Exception ex) {
                        log.error("Failed to handle response stream message {}", message.getId(), ex);
                        // 필요시 DLQ 기록 로직 추가
                    }
                }
        );
    }

    @Override
    public void start() {
        if (!running) {
            this.container.start();
            this.running = true;
            log.info("ChatResponseListener started");
        }
    }

    @Override
    public void stop() {
        if (running) {
            try { this.container.stop(); } catch (Exception e) { log.warn("Error stopping container", e); }
            try { this.executor.shutdownNow(); } catch (Exception e) { log.warn("Error shutting down executor", e); }
            this.running = false;
            log.info("ChatResponseListener stopped");
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}