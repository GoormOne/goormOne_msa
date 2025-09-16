package com.example.storeservice.stock.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

/**
 * Outbound: stock.shortage (Stock -> Order)
 * - 결제(PAID) 시점 finalize 실패(실제 재고 부족 등) 시 발행
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class StockShortageEvent extends BaseEvent {
    private UUID            customerId;
    private List<OrderItem> items;
    private String          message;
}
