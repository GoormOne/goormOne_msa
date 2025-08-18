package com.example.msaorderservice.order.dto;

import java.time.OffsetDateTime;
import java.util.List;
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
public class CustomerOrderDetailRes {
	private UUID orderId;
	private UUID customerId;
	private UUID storeId;
	private String storeName;
	private OrderStatus orderStatus;
	private int totalPrice;
	private OffsetDateTime createdAt;

	private List<OrderItemDto> items;

	@Getter
	@Builder
	public static class OrderItemDto {
		private UUID menuId;
		private String menuName;
		private int quantity;
		private int menuPrice;
		private int lineTotal;
	}
}
