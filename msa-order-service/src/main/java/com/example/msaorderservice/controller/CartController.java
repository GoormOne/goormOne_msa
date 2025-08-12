package com.example.msaorderservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.common.ApiResponse;

import com.example.msaorderservice.dto.CartItemAddReq;
import com.example.msaorderservice.dto.CartItemAddRes;
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
}
