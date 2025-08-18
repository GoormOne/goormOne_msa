package com.example.msaorderservice.order.controller;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.msaorderservice.order.dto.OrderCreateReq;
import com.example.msaorderservice.order.dto.OrderCreateRes;
import com.example.msaorderservice.order.dto.OrderRes;
import com.example.msaorderservice.order.service.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

	private final OrderService orderService;

	@PostMapping
	public ResponseEntity<OrderCreateRes> createOrder(@RequestHeader("X-User-Id")UUID customerId,
		@RequestBody OrderCreateReq req) {
		OrderCreateRes res = orderService.createOrder(customerId, req);
		return ResponseEntity.ok(res);
	}

	@GetMapping
	public ResponseEntity<Page<OrderRes>> getMyOrders(
		@RequestHeader("X-User-Id") UUID customerId,
		Pageable pageable) {
		return ResponseEntity.ok(orderService.getMyOrders(customerId, pageable));
	}

	@GetMapping("/{orderId}")
	public ResponseEntity<OrderRes> getMyOrderDetail(
		@RequestHeader("X-User-Id") UUID customerId,
		@PathVariable UUID orderId) {
		return ResponseEntity.ok(orderService.getMyOrderDetail(customerId, orderId));
	}
}
