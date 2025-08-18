package com.example.msaorderservice.order.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.msaorderservice.order.entity.OrderEntity;
import com.example.msaorderservice.order.entity.OrderItemEntity;
import com.example.msaorderservice.order.entity.OrderStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderRes {
	private UUID orderId;
	private UUID customerId;
	private UUID storeId;
	private OrderStatus orderStatus;
	private int totalPrice;
	private LocalDateTime createdAt;

	private List<OrderItemDto> items;

	@Getter
	@Builder
	public static class OrderItemDto {
		private UUID menuId;
		private String menuName;
		private int quantity;
		private int menuPrice;
	}
}
