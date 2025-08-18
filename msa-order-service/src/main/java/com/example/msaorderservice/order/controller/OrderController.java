package com.example.msaorderservice.order.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.msaorderservice.order.dto.OrderCreateReq;
import com.example.msaorderservice.order.dto.OrderCreateRes;
import com.example.msaorderservice.order.dto.CustomerOrderDetailRes;
import com.example.msaorderservice.order.dto.OrderSummaryRes;
import com.example.msaorderservice.order.dto.OwnerOrderDetailRes;
import com.example.msaorderservice.order.entity.OrderStatus;
import com.example.msaorderservice.order.service.OrderService;

import jakarta.validation.Valid;
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
	public Page<OrderSummaryRes> getMyOrders(
		@RequestHeader("X-User-Id") UUID customerId,
		Pageable pageable
	) {
		return orderService.getMyOrders(customerId, pageable);
	}

	@GetMapping("/{orderId}")
	public CustomerOrderDetailRes getMyOrderDetail(
		@RequestHeader("X-User-Id") UUID customerId,
		@PathVariable UUID orderId
	) {
		return orderService.getMyOrderDetail(customerId, orderId);
	}

	@GetMapping("/owner")
	public Page<OrderSummaryRes> getOwnerOrders(
		@RequestHeader("X-User-Id") UUID ownerId,
		Pageable pageable
	) {
		return orderService.getOwnerOrders(ownerId, pageable);
	}

	@GetMapping("/owner/{orderId}")
	public OwnerOrderDetailRes getOwnerOrderDetail(
		@RequestHeader("X-User-Id") UUID ownerId,
		@PathVariable UUID orderId
	) {
		return orderService.getOwnerOrderDetail(orderId, ownerId);
	}

	@PatchMapping("/owner/{orderId}/status")
	public OwnerOrderDetailRes updateOrderStatusByOwner(
		@RequestHeader("X-User-Id") UUID ownerId,
		@PathVariable UUID orderId,
		@Valid @RequestBody OrderStatus newStatus
	) {
		return orderService.updateOrderStatusByOwner(ownerId, orderId, newStatus);
	}
}
