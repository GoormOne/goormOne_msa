package com.example.storeservice.stock;

import com.example.storeservice.stock.model.*;
import com.example.storeservice.stock.model.Enums;
import com.example.storeservice.stock.service.StockSagaService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

import static com.example.storeservice.stock.StockEventHeaders.*;
import static com.example.storeservice.stock.StockTypes.*;

/**
 * - inbound topics: order-events, payment-events (TopicRegistry 통해 주입)
 * - payload 내부 type 으로 분기. type 부재 시 x-event-type 헤더로 폴백.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockSagaListener {

    private final ObjectMapper mapper = new ObjectMapper();
    private final TopicRegistry topics;
    private final StockSagaService saga;

    @KafkaListener(topics = "${topics.order.events}")
    public void onMessage(
            @Payload String body,
            @Header(name = X_EVENT_TYPE, required = false) String headerType,
            @Header(name = X_CORRELATION_ID, required = false) String corr,
            @Header(name = X_CAUSATION_ID, required = false) String cause,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment ack
    ) {
        log.debug("[KAFKA][RECV] topic={} key={} type(h)={} corr={} cause={} body={}",
                topic, key, headerType, corr, cause, body);

        try {
            JsonNode root = mapper.readTree(body);
            String type = optText(root, "type");
            if (type == null || type.isBlank()) type = headerType;

            if (type == null || type.isBlank()) {
                log.warn("[KAFKA][WARN] missing type. topic={}, key={}, body={}", topic, key, body);
                ack.acknowledge(); // 스킵
                return;
            }

            switch (type) {
                case ORDER_CREATED -> saga.onOrderCreated(asOrderCreated(root), corr, cause);
                case PAYMENT_STATUS_CHANGED -> saga.onPaymentChanged(asPaymentChanged(root), corr, cause);
                case ORDER_STATUS_CHANGED -> saga.onOrderStatusChanged(asOrderStatusChanged(root), corr, cause);
                default -> log.info("[KAFKA][IGNORE] type={} topic={} key={}", type, topic, key);
            }

            ack.acknowledge(); // 성공 처리

        } catch (Exception e) {
            // 에러 핸들러(DeadLetterPublishingRecoverer)로 전달되도록 ack 하지 않음
            log.error("[KAFKA][ERROR] topic={} key={} body={}", topic, key, body, e);
            throw new RuntimeException(e);
        }
    }

    // === JSON helpers ===
    private static String optText(JsonNode n, String f) {
        return (n != null && n.hasNonNull(f)) ? n.get(f).asText() : null;
    }
    private static UUID optUUID(JsonNode n, String f) {
        String v = optText(n, f); return v == null ? null : java.util.UUID.fromString(v);
    }
    private static int optInt(JsonNode n, String f) {
        return (n != null && n.hasNonNull(f)) ? n.get(f).asInt() : 0;
    }

    private static java.util.List<OrderItem> asItems(JsonNode root) {
        if (!root.has("items") || !root.get("items").isArray()) return List.of();
        List<OrderItem> items = new ArrayList<>();
        for (JsonNode it : root.get("items")) {
            items.add(OrderItem.builder()
                    .menuId(optUUID(it, "menuId"))
                    .qty(optInt(it, "qty"))
                    .build());
        }
        return items;
    }

    private OrderCreatedEvent asOrderCreated(JsonNode root) {
        return OrderCreatedEvent.builder()
                .type(ORDER_CREATED)
                .eventId(optUUID(root, "eventId"))
                .orderId(optUUID(root, "orderId"))
                .storeId(optUUID(root, "storeId"))
                .occurredAt(parseInstant(optText(root,"occurredAt")))
                .customerId(optUUID(root, "customerId"))
                .ownerId(optUUID(root, "ownerId"))
                .items(asItems(root))
                .build();
    }

    private PaymentStatusChangedEvent asPaymentChanged(JsonNode root) {
        var status = Enums.PaymentStatus.valueOf(optText(root, "status"));
        return PaymentStatusChangedEvent.builder()
                .type(PAYMENT_STATUS_CHANGED)
                .eventId(optUUID(root, "eventId")) // 있을 수도, 없을 수도
                .orderId(optUUID(root, "orderId"))
                .storeId(optUUID(root, "storeId"))
                .occurredAt(parseInstant(optText(root, "occurredAt")))
                .status(status)
                .changedAt(parseInstant(optText(root, "changedAt")))
                .build();
    }

    private OrderStatusChangedEvent asOrderStatusChanged(JsonNode root) {
        var st = Enums.OrderStatus.valueOf(optText(root, "status"));
        var pay = optText(root, "paymentStatus");
        return OrderStatusChangedEvent.builder()
                .type(ORDER_STATUS_CHANGED)
                .eventId(optUUID(root, "eventId"))
                .orderId(optUUID(root, "orderId"))
                .storeId(optUUID(root, "storeId"))
                .occurredAt(parseInstant(optText(root,"occurredAt")))
                .status(st)
                .paymentStatus(pay == null ? null : Enums.PaymentStatus.valueOf(pay))
                .changedAt(parseInstant(optText(root,"changedAt")))
                .build();
    }

    private static java.time.Instant parseInstant(String s) {
        if (s == null || s.isBlank()) return java.time.Instant.now();
        try {
            // OffsetDateTime iso-8601 호환 문자열도 허용
            return OffsetDateTime.parse(s).toInstant();
        } catch (DateTimeParseException ex) {
            try { return Instant.parse(s); } catch (Exception ignore) { return Instant.now(); }
        }
    }
}