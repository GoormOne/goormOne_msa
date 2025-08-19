package com.example.msaorderservice.order.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.msaorderservice.order.dto.OrderCheckoutView;
import com.example.msaorderservice.order.entity.OrderEntity;
import com.example.msaorderservice.order.entity.OrderItemEntity;
import com.example.msaorderservice.order.entity.OrderStatus;
import com.example.msaorderservice.order.entity.PaymentStatus;
import com.example.msaorderservice.order.repository.OrderAuditRepository;
import com.example.msaorderservice.order.repository.OrderItemRepository;
import com.example.msaorderservice.order.repository.OrderRepository;
import com.example.msaorderservice.order.service.StoreClient;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/internal/orders")
@RequiredArgsConstructor
public class OrderInternalController {

	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	private final OrderAuditRepository orderAuditRepository;
	private final StoreClient storeClient;

	@GetMapping("/{orderId}/checkout")
	public OrderCheckoutView getOrderForCheckout(@PathVariable UUID orderId,
		@RequestHeader("X-User-Id") UUID customerId) {
		OrderEntity order = orderRepository.findByOrderIdAndCustomerId(orderId, customerId)
			.orElseThrow(() -> new IllegalStateException("주문을 찾을 수 없습니다."));

		if (order.getPaymentStatus() != PaymentStatus.PENDING) {
			throw new IllegalStateException("결제 가능한 상태가 아닙니다.");
		}

		List<OrderItemEntity> items = orderItemRepository.findByOrderId_OrderId(orderId);
		String storeName = storeClient.getStoreDetail(order.getStoreId()).getStoreName();

		String first = items.isEmpty() ? storeName : items.get(0).getMenuName();
		int more = Math.max(0, items.size() - 1);
		String orderName = (more > 0) ? (first + "외" + more + "건") : first;

		int totalQty = items.stream().mapToInt(OrderItemEntity::getQuantity).sum();

		return OrderCheckoutView.builder()
			.orderId(orderId)
			.storeName(storeName)
			.orderName(orderName)
			.amount(order.getTotalPrice())
			.totalQuantity(totalQty)
			.items(items.stream().map(it ->
				OrderCheckoutView.Item.builder()
					.menuName(it.getMenuName())
					.price(it.getMenuPrice())
					.quantity(it.getQuantity())
					.build()
			).toList())
			.build();
	}

	@PostMapping("/{orderId}/status/paid")
	@Transactional
	public void markPaid(@RequestHeader("X-User-Id") UUID customerId,
		@PathVariable UUID orderId) {
		OrderEntity order = orderRepository.findByOrderIdAndCustomerId(orderId, customerId)
			.orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
		order.setPaymentStatus(PaymentStatus.PAID);
	}

	@PostMapping("/{orderId}/status/failed")
	@Transactional
	public void markFailed(@RequestHeader("X-User-Id") UUID customerId,
		@PathVariable UUID orderId) {
		OrderEntity order = orderRepository.findByOrderIdAndCustomerId(orderId, customerId)
			.orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
		order.setPaymentStatus(PaymentStatus.FAILED);
	}
}
