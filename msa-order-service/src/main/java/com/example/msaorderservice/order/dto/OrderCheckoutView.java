package com.example.msaorderservice.order.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCheckoutView {
	private UUID orderId;
	private String storeName;
	private String orderName;
	private int amount;
	private int totalQuantity;
	private List<Item> items;

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class Item {
		private String menuName;
		private int price;
		private int quantity;
	}
}
