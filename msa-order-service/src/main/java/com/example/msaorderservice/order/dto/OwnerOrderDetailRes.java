package com.example.msaorderservice.order.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.example.msaorderservice.order.entity.OrderStatus;
import com.example.msaorderservice.order.entity.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerOrderDetailRes {
	private UUID orderId;
	private UUID storeId;
	private String storeName;
	private UUID customerId;
	private List<OrderItemDto> items;
	private int totalPrice;
	private OrderStatus orderStatus;
	private PaymentStatus paymentStatus;
	private OffsetDateTime createdAt;
	private String requestMessage;

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class OrderItemDto {
		private UUID menuId;
		private String menuName;
		private int quantity;
		private int menuPrice;
		private int lineTotal;
	}
}
