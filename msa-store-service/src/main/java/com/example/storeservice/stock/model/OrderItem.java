package com.example.storeservice.stock.model;

import lombok.*;

import java.util.UUID;

// 주문 품목
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class OrderItem {
    private UUID menuId;    // MenuInventory.pk
    private int  qty;
}
