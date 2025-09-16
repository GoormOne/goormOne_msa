package com.example.storeservice.stock;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * JSON 메시지 전송.
 * - x-event-type 헤더를 함께 넣어 상호운용성(주문팀 코드) 확보
 * - stock 서비스는 기본적으로 stock-events 로만 발행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaJsonProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final TopicRegistry topics;
    private final ObjectMapper om = new ObjectMapper();

    public void sendToStock(Object payload, String key, String eventType) {
        sendWithTopic(topics.getStockEvents(), payload, key, eventType, null);
    }

    public void sendWithTopic(String topic, Object payload, String key, String eventType, Map<String,String> extraHeaders) {
        try {
            String json = om.writeValueAsString(payload);
            Message<String> msg = MessageBuilder.withPayload(json)
                    .setHeader(KafkaHeaders.TOPIC, topic)
                    .setHeader(KafkaHeaders.KEY, key)
                    .setHeader("x-event-type", eventType)
                    .setHeader("x-event-version", "1")
                    .setHeader("x-producer", "stock-service")
                    .copyHeaders(extraHeaders == null ? Map.of() : extraHeaders)
                    .build();
            log.debug("[KAFKA][PRODUCE] topic={} key={} type={} json={}", topic, key, eventType, json);
            kafkaTemplate.send(msg);
        } catch (Exception e) {
            log.error("[KAFKA][ERROR] send topic={} key={} type={} payload={}", topic, key, eventType, payload, e);
            throw new RuntimeException("Kafka send failed", e);
        }
    }
}
