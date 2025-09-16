package com.example.storeservice.stock.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Inbound: order.created (Order -> Stock)
 * - Stock은 "실제 재고 확인 없이" 예약 장부만 올린다.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class OrderCreatedEvent extends BaseEvent {
    private UUID            customerId;
    private List<OrderItem> items;
}
