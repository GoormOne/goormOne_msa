package com.example.msaorderservice.dto;

import java.util.UUID;

import com.example.msaorderservice.entity.CartItemEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CartItemAddRes {
	private UUID cartId;
	private UUID cartItemId;
	private UUID menuId;
	private int quantity;

	public static CartItemAddRes from(CartItemEntity entity) {
		return CartItemAddRes.builder()
			.cartItemId(entity.getCartItemId())
			.cartId(entity.getCartId())
			.menuId(entity.getMenuId())
			.quantity(entity.getQuantity())
			.build();
	}
}
