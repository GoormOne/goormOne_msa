package com.example.storeservice.stock.model;

import lombok.*;
import com.example.storeservice.stock.model.Enums.PaymentStatus;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Inbound: payment.status.changed (Order -> Stock)
 * - PENDING -> PAID: 이때 최초로 실제 재고 검증 및 차감(write-through)
 * - PENDING -> FAILED: 예약 유지 (재시도 고려)
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class PaymentStatusChangedEvent extends BaseEvent {
    private PaymentStatus   status;
    private Instant         changedAt;
}
