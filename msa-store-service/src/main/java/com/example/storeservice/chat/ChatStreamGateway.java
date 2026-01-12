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
    @Value("${app.chat.req2qidPrefix}") private String req2qidPrefix;
    @Value("${app.chat.responseTtlSeconds}") private int respTtlSec;

    /** 요청 생성 및 XADD
     * Redis Streams에 이벤트 발행만 하는 함수
     * 이 질문을 DB의 어떤 레코드와 연결해야 한다는 정보가 없음.
     * 응답이 돌아올 때, Listener 입장에서 request_id만 있고, 그걸 어떤 question_id와 매핑해야 하는지 알 수 없음.
     * */
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
    /** 발행 + RDB questionId와의 매핑 저장
     * sendRequest 호출 + 매핑 저장
     * request_id (RS에서 오가는 ID) / question_id (RDB p_review_queries PK) -> 이 두 개를 Redis에 임시 저장
     * Listener가 FastAPI 응답 (request_id, answer) 받았을 때,
     * 1. request_id -> Redis 조회
     * 2. 대응하는 question_id 찾아옴
     * 3. 그걸로 DB 업데이트
     * */
    public UUID sendRequestAndCorrelate(UUID storeId, UUID menuId, String question, UUID questionId) {
        UUID reqId = sendRequest(storeId, menuId, question);
        redis.opsForValue().set(req2qidPrefix + reqId, questionId.toString(), Duration.ofSeconds(respTtlSec));
        return reqId;
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