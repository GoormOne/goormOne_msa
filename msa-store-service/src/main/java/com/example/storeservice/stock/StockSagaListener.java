package com.example.storeservice.stock;

import com.example.storeservice.stock.model.*;
import com.example.storeservice.stock.service.StockSagaService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

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
    private final StockSagaService saga;

    @KafkaListener(topics = "#{@topicRegistry.inboundTopics}")
    public void onMessage(
            @Payload String json,
            @Header(value = "x-event-type", required = false) String headerType,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {

        log.debug("[KAFKA][RECV] topic={} headerType={} body={}", topic, headerType, json); // ☆ BP

        try {
            JsonNode root = mapper.readTree(json);
            String type = getText(root, "type");
            if (type == null || type.isBlank()) type = headerType; // 폴백

            if (type == null || type.isBlank()) {
                log.warn("[KAFKA][WARN] missing type. topic={}, body={}", topic, json);
                return;
            }

            switch (type) {
                case ORDER_CREATED            -> saga.onOrderCreated(asOrderCreated(root));
                case PAYMENT_STATUS_CHANGED   -> saga.onPaymentChanged(asPaymentChanged(root));
                case ORDER_STATUS_CHANGED     -> saga.onOrderStatusChanged(asOrderStatusChanged(root));
                default -> log.info("[KAFKA][IGNORE] type={} topic={}", type, topic);
            }
        } catch (Exception e) {
            log.error("[KAFKA][ERROR] parse/handle topic={} body={}", topic, json, e);
        }
    }

    // --------- JSON helpers ---------
    private static String getText(JsonNode n, String f) { return n.hasNonNull(f) ? n.get(f).asText() : null; }
    private static UUID getUUID(JsonNode n, String f)   { return Optional.ofNullable(getText(n,f)).map(java.util.UUID::fromString).orElse(null); }
    private static int  getInt (JsonNode n, String f)   { return n.hasNonNull(f) ? n.get(f).asInt() : 0; }

    private static java.util.List<OrderItem> asItems(JsonNode root) {
        if (!root.has("items") || !root.get("items").isArray()) return java.util.List.of();
        java.util.List<OrderItem> items = new java.util.ArrayList<>();
        for (JsonNode it : root.get("items")) {
            items.add(OrderItem.builder()
                    .menuId(getUUID(it, "menuId"))
                    .qty(getInt(it, "qty"))
                    .build());
        }
        return items;
    }

    private OrderCreatedEvent asOrderCreated(JsonNode root) {
        return OrderCreatedEvent.builder()
                .type(ORDER_CREATED)
                .eventId(getUUID(root,"eventId"))
                .orderId(getUUID(root,"orderId"))
                .storeId(getUUID(root,"storeId"))
                .occurredAt(Optional.ofNullable(getText(root,"occurredAt")).map(Instant::parse).orElse(Instant.now()))
                .customerId(getUUID(root,"customerId"))
                .ownerId(getUUID(root,"ownerId"))
                .items(asItems(root))
                .build();
    }

    private PaymentStatusChangedEvent asPaymentChanged(JsonNode root) {
        return PaymentStatusChangedEvent.builder()
                .type(PAYMENT_STATUS_CHANGED)
                .eventId(getUUID(root,"eventId"))
                .orderId(getUUID(root,"orderId"))
                .storeId(getUUID(root,"storeId"))
                .occurredAt(Optional.ofNullable(getText(root,"occurredAt")).map(Instant::parse).orElse(Instant.now()))
                .customerId(getUUID(root,"customerId"))
                .ownerId(getUUID(root,"ownerId"))
                .fromStatus(Enums.PaymentStatus.valueOf(getText(root,"fromStatus")))
                .toStatus(Enums.PaymentStatus.valueOf(getText(root,"toStatus")))
                .items(asItems(root))
                .build();
    }

    private OrderStatusChangedEvent asOrderStatusChanged(JsonNode root) {
        return OrderStatusChangedEvent.builder()
                .type(ORDER_STATUS_CHANGED)
                .eventId(getUUID(root,"eventId"))
                .orderId(getUUID(root,"orderId"))
                .storeId(getUUID(root,"storeId"))
                .occurredAt(Optional.ofNullable(getText(root,"occurredAt")).map(Instant::parse).orElse(Instant.now()))
                .customerId(getUUID(root,"customerId"))
                .ownerId(getUUID(root,"ownerId"))
                .fromStatus(Enums.OrderStatus.valueOf(getText(root,"fromStatus")))
                .toStatus(Enums.OrderStatus.valueOf(getText(root,"toStatus")))
                .paymentStatus(Enums.PaymentStatus.valueOf(getText(root,"paymentStatus")))
                .items(asItems(root))
                .build();
    }
}