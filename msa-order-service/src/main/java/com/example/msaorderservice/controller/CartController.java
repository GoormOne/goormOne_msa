package com.example.msaorderservice.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.common.ApiResponse;

import com.example.msaorderservice.dto.CartItemAddReq;
import com.example.msaorderservice.dto.CartItemAddRes;
import com.example.msaorderservice.dto.CartItemsPageRes;
import com.example.msaorderservice.entity.CartItemEntity;
import com.example.msaorderservice.service.CartService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/carts")
public class CartController {

	private final CartService cartService;

	@PostMapping("/add")
	public ResponseEntity<ApiResponse<CartItemAddRes>> addItem(@Valid @RequestBody CartItemAddReq req) {
		CartItemEntity saved = cartService.addItem(req);
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(ApiResponse.success(CartItemAddRes.from(saved)));
	}

	@GetMapping
	public CartItemsPageRes getMyCartItems(@RequestHeader("X-User-Id") UUID userId,
		@RequestParam(required = false) Integer page,
		@RequestParam(required = false) Integer size) {
		return cartService.getMyCartItemsPage(userId, page, size);
	}

	@DeleteMapping("/items")
	public ResponseEntity<Void> clearMyCartItems(
		@RequestHeader("X-User-Id") UUID userId) {

		cartService.clearCartItems(userId);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/items/{cartItemId}")
	public ResponseEntity<Void> deleteMyCartItem(@RequestHeader("X-User-Id") UUID userId,
		@PathVariable UUID cartItemId) {

		cartService.deleteCartItem(userId, cartItemId);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping
	public ResponseEntity<Void> deleteMyCart(@RequestHeader("X-User-Id") UUID userId) {

		cartService.deleteCart(userId);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/items/{menuId}/increase")
	public ResponseEntity<Void> increaseQuantity(@RequestHeader("X-User-Id") UUID userId,
		@PathVariable UUID menuId) {

		cartService.increaseQuantity(userId, menuId);
		return ResponseEntity.noContent().build();
	}

	@PatchMapping("/items/{menuId}/decrease")
	public ResponseEntity<Void> decreaseQuantity(@RequestHeader("X-User-Id") UUID userId,
		@PathVariable UUID menuId) {

		cartService.decreaseQuantity(userId, menuId);
		return ResponseEntity.noContent().build();
	}
}
