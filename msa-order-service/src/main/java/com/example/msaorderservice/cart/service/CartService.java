package com.example.msaorderservice.cart.service;

import java.util.UUID;

import com.example.msaorderservice.cart.dto.CartItemAddReq;
import com.example.msaorderservice.cart.dto.CartItemsPageRes;
import com.example.msaorderservice.cart.entity.CartItemEntity;

public interface CartService {
	CartItemEntity addItem(CartItemAddReq req);

	CartItemsPageRes getMyCartItemsPage(UUID userId, Integer page, Integer size);

	void clearCartItems(UUID userId);

	void deleteCart(UUID userId);

	void deleteCartItem(UUID userId, UUID cartItemId);

	void increaseQuantity(UUID userId, UUID menuId);

	void decreaseQuantity(UUID userId, UUID menuId);
}
