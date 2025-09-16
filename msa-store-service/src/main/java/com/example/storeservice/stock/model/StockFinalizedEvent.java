package com.example.storeservice.stock.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

/**
 * Outbound: stock.finalized (Stock -> Order)
 * - PENDING -> PAID 에서 품목 전체 finalize + DB 반영에 성공했을 때 발행
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class StockFinalizedEvent extends BaseEvent {
    private UUID            customerId;
    private List<OrderItem> items;
}
