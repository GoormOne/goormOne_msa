package com.example.storeservice.stock;

import com.ctc.wstx.shaded.msv_core.reader.xmlschema.IncludeState;
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

    private final ObjectMapper mapper;
    private final TopicRegistry topics;
    private final StockSagaService saga;

    @KafkaListener(topics = "${topics.order.events}")
    public void onMessage(
            @Payload String body,
            @Header(name = X_EVENT_TYPE, required = false) String headerType,
            @Header(name = X_EVENT_VERSION, required = false) String version,
            @Header(name = X_PRODUCER, required = false) String producer,
            @Header(KafkaHeaders.RECEIVED_KEY) String key,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            Acknowledgment ack
    ) {
        log.debug("[KAFKA][RECV] topic={} key={} type={} ver={} producer={} len={}",
                topic, key, headerType, version, producer, body == null ? 0 : body.length());
        log.trace("[KAFKA][RECV][BODY] {}", body);

        try {
            JsonNode root = mapper.readTree(body);
            String type = optText(root, "type");
            if (type == null || type.isBlank()) type = headerType;

            if (type == null || type.isBlank()) {
                log.warn("[KAFKA][WARN] missing event type. topic={} key={}", topic, key);
                ack.acknowledge(); // 스킵
                return;
            }

            switch (type) {
                case ORDER_CREATED          -> saga.onOrderCreated(asOrderCreated(root));
                case PAYMENT_STATUS_CHANGED -> saga.onPaymentChanged(asPaymentChanged(root));
                case ORDER_STATUS_CHANGED   -> saga.onOrderStatusChanged(asOrderStatusChanged(root));
                default -> log.info("[KAFKA][IGNORE] type={} topic={} key={}", type, topic, key);
            }

            ack.acknowledge(); // 성공 처리
            log.debug("[KAFKA][ACK] topic={} key={}", topic, key);

        } catch (ReservationNotReadyException e) {
            // 재시도 유도: ack 하지 않고 예외 전파
            log.warn("[KAFKA][RETRY] {} body={}", e.getMessage(), body);
            throw e;
        } catch (Exception e) {
            // 기타 오류도 NACK → 에러 핸들러/재시도/ DLT
            log.error("[KAFKA][ERROR] topic={} key={} body={}", topic, key, body, e);
            throw new RuntimeException(e);
        }
    }

    // === JSON helpers ===
    private static String optText(JsonNode n, String f) {
        return (n != null && n.hasNonNull(f)) ? n.get(f).asText() : null;
    }
    private static UUID optUUID(JsonNode n, String f) {
        String v = optText(n, f); return (v == null || v.isBlank()) ? null : UUID.fromString(v);
    }
    private static int optInt(JsonNode n, String f) {
        return (n != null && n.hasNonNull(f)) ? n.get(f).asInt() : 0;
    }
    private static Instant parseInstant(String s) {
        if (s == null || s.isBlank()) return Instant.now();
        try { return OffsetDateTime.parse(s).toInstant(); }
        catch (DateTimeParseException ex) { try { return Instant.parse(s); } catch (Exception ignore) { return Instant.now(); } }
    }

    private static List<OrderItem> asItems(JsonNode root) {
        if (!root.has("items") || !root.get("items").isArray()) return List.of();
        List<OrderItem> items = new ArrayList<>();
        for (JsonNode it : root.get("items")) {
            items.add(OrderItem.builder()
                    .menuId(optUUID(it, "menuId"))
                    .qty(optInt(it, "qty"))
                    .build()
            );
        }
        return items;
    }

    private OrderCreatedEvent asOrderCreated(JsonNode root) {
        return OrderCreatedEvent.builder()
                .eventId(optUUID(root,"eventId"))
                .orderId(optUUID(root,"orderId"))
                .occurredAt(parseInstant(optText(root,"occurredAt")))
                .customerId(optUUID(root,"customerId"))
                .items(asItems(root))
                .build();
    }
    private PaymentStatusChangedEvent asPaymentChanged(JsonNode root) {
        var status = Enums.PaymentStatus.valueOf(optText(root, "status"));
        return PaymentStatusChangedEvent.builder()
                .eventId(optUUID(root,"eventId"))
                .orderId(optUUID(root,"orderId"))
                .occurredAt(parseInstant(optText(root,"occurredAt")))
                .status(status)
                .changedAt(parseInstant(optText(root,"changedAt")))
                .build();
    }
    private OrderStatusChangedEvent asOrderStatusChanged(JsonNode root) {
        var st  = Enums.OrderStatus.valueOf(optText(root,"status"));
        var pay = optText(root,"paymentStatus");
        return OrderStatusChangedEvent.builder()
                .eventId(optUUID(root,"eventId"))
                .orderId(optUUID(root,"orderId"))
                .occurredAt(parseInstant(optText(root,"occurredAt")))
                .status(st)
                .paymentStatus(pay == null ? null : Enums.PaymentStatus.valueOf(pay))
                .changedAt(parseInstant(optText(root,"changedAt")))
                .build();
    }
}
