package com.example.msaorderservice.cart.dto;

import java.util.UUID;

import com.example.msaorderservice.cart.entity.CartItemEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
