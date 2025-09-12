package com.example.storeservice.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatStreamGateway {

    private final StringRedisTemplate redis;
    private final ObjectMapper om;

    @Value("${app.chat.requestStream}") private String requestStream;
    @Value("${app.chat.responseKeyPrefix}") private String respKeyPrefix;
    @Value("${app.chat.responseTtlSeconds}") private int respTtlSec;

    /** 요청 생성 및 XADD */
    public UUID sendRequest(UUID storeId, UUID menuId, String query) {
        UUID requestId = UUID.randomUUID();
        ChatRequestMsg msg = ChatRequestMsg.builder()
                .requestId(requestId)
                .storeId(storeId)
                .menuId(menuId)
                .query(query)
                .build();

        Map<String, String> fields = Map.of(
                "request_id", msg.getRequestId().toString(),
                "store_id", msg.getStoreId().toString(),
                "menu_id", msg.getMenuId().toString(),
                "query", msg.getQuery()
        );
        redis.opsForStream().add(requestStream, fields);
        return requestId;
    }

    /** 동기 대기 (UUID 버전) */
    public String awaitAnswer(UUID requestId, long timeoutMs) {
        return awaitAnswer(requestId.toString(), timeoutMs);
    }

    /** 동기 대기 (String 버전) */
    public String awaitAnswer(String requestId, long timeoutMs) {
        String key = respKeyPrefix + requestId;
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            String answer = redis.opsForValue().get(key);
            if (answer != null) return answer;
            try { Thread.sleep(100L); } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return null; // timeout
    }

    /** 응답 수동 주입 (UUID) */
    public void saveAnswerToCache(UUID requestId, String answer) {
        saveAnswerToCache(requestId.toString(), answer);
    }

    /** 응답 수동 주입 (String) */
    public void saveAnswerToCache(String requestId, String answer) {
        redis.opsForValue().set(respKeyPrefix + requestId, answer, Duration.ofSeconds(respTtlSec));
    }
}