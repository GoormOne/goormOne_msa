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
import java.util.UUID;

import static com.example.storeservice.stock.StockEventHeaders.*;

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

    public void sendToStock(Object payload, String key, String eventType,
                            String correlationId, String causationId,
                            Map<String, String> extraHeaders) {
        try {
            String json = om.writeValueAsString(payload);
            Message<String> msg = MessageBuilder.withPayload(json)
                    .setHeader(KafkaHeaders.TOPIC, topics.getStockEvents())
                    .setHeader(KafkaHeaders.KEY, key)
                    .setHeader(X_EVENT_TYPE, eventType)
                    .setHeader(X_EVENT_VERSION, "1")
                    .setHeader(X_CORRELATION_ID, nvl(correlationId, UUID.randomUUID().toString()))
                    .setHeader(X_CAUSATION_ID, nvl(causationId, safeEventId(payload)))
                    .setHeader(X_PRODUCER, "stock-service")
                    .copyHeaders(extraHeaders == null ? Map.of() : extraHeaders)
                    .build();

            log.debug("[KAFKA][PRODUCE] topic={} key={} type={} corr={} cause={} json={}",
                    topics.getStockEvents(), key, eventType, correlationId, causationId, json);

            kafkaTemplate.send(msg);
        } catch (Exception e) {
            log.error("[KAFKA][ERROR] send type={} key={} payload={}", eventType, key, payload, e);
            throw new RuntimeException("Kafka send failed", e);
        }
    }

    private static String nvl(String v, String alt) { return (v == null || v.isBlank()) ? alt : v; }

    /** payload에 eventId 필드가 있으면 causation 기본값으로 사용 */
    private static String safeEventId(Object payload) {
        try {
            var f = payload.getClass().getMethod("getEventId");
            Object v = f.invoke(payload);
            return v != null ? v.toString() : UUID.randomUUID().toString();
        } catch (Exception ignore) {
            return UUID.randomUUID().toString();
        }
    }
}
