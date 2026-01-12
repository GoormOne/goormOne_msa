package com.example.storeservice.stock.model;

import com.example.storeservice.stock.model.Enums.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Inbound: order.status.changed (Order -> Stock)
 * - PENDING -> CONFIRMED: Stock은 별도 동작 없음(이미 PAID에서 finalize 완료)
 * - PENDING -> CANCELLED & paymentStatus==PAID: Redis/DB 재회복 후 stock.restored 발행
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class OrderStatusChangedEvent extends BaseEvent {
    private OrderStatus     status;
    private PaymentStatus   paymentStatus;
    private Instant         changedAt;
}
