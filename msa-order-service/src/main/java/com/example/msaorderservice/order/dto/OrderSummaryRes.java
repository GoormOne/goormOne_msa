package com.example.msaorderservice.order.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.example.msaorderservice.order.entity.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryRes {
	private UUID orderId;
	private UUID storeId;
	private String storeName;
	private String summaryTitle;
	private int itemCount;
	private OrderStatus orderStatus;
	private int totalPrice;
	private OffsetDateTime createdAt;
}
