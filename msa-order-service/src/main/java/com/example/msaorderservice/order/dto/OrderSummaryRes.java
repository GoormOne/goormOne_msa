package com.example.msaorderservice.order.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.example.msaorderservice.order.entity.OrderStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderSummaryRes {
	private UUID orderId;
	private String storeName;
	private OrderStatus orderStatus;
	private int totalPrice;
	private OffsetDateTime createdAt;
	private String summaryMenu;
}
