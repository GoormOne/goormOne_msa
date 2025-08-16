package com.example.msaorderservice.cart.service;

import java.util.UUID;

import com.example.msaorderservice.cart.dto.CartItemAddReq;
import com.example.msaorderservice.cart.dto.CartItemsPageRes;
import com.example.msaorderservice.cart.entity.CartItemEntity;

public interface CartService {
	CartItemEntity addItem(CartItemAddReq req);

	CartItemsPageRes getMyCartItemsPage(UUID customerId, Integer page, Integer size);

	void clearCartItems(UUID customerId);

	void deleteCart(UUID customerId);

	void deleteCartItem(UUID customerId, UUID cartItemId);

	void increaseQuantity(UUID customerId, UUID menuId);

	void decreaseQuantity(UUID customerId, UUID menuId);
}
