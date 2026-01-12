package com.example.storeservice.stock.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

/**
 * Outbound: stock.reservation.result (Stock -> Order)
 * - order.created 처리 결과(항상 success=true 예상)
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class StockReservationResultEvent extends BaseEvent {
    private boolean         success;
    private UUID            customerId;
    private List<OrderItem> items;
    private String          reason;  // 실패 시 사유
}
