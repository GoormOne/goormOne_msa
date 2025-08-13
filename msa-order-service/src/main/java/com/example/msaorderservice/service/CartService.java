package com.example.msaorderservice.service;

import java.util.UUID;

import com.example.msaorderservice.dto.CartItemAddReq;
import com.example.msaorderservice.dto.CartItemRes;
import com.example.msaorderservice.dto.CartItemsPageRes;
import com.example.msaorderservice.dto.MenuLookUp;
import com.example.msaorderservice.entity.CartItemEntity;

public interface CartService {
	CartItemEntity addItem(CartItemAddReq req);

	CartItemsPageRes getMyCartItemsPage(UUID userId, Integer page, Integer size);

	void clearCartItems(UUID userId);

	void deleteCart(UUID userId);

	void deleteCartItem(UUID userId, UUID cartItemId);
}
