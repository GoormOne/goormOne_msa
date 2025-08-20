package com.example.msaorderservice.order.controller;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.common.dto.OrderCheckoutView;
import com.example.common.dto.PaymentStatusUpdatedReq;
import com.example.common.entity.PaymentStatus;
import com.example.msaorderservice.order.entity.OrderEntity;
import com.example.msaorderservice.order.entity.OrderItemEntity;

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

	@PatchMapping("/{orderId}/status/paid")
	@Transactional
	public ResponseEntity<Void> markPaid(@RequestHeader("X-User-Id") UUID customerId,
		@PathVariable UUID orderId,
		@RequestBody PaymentStatusUpdatedReq req) {
		OrderEntity order = orderRepository.findByOrderIdAndCustomerId(orderId, customerId)
			.orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

		PaymentStatus curr = order.getPaymentStatus();
		PaymentStatus next = req.getPaymentStatus();

		boolean allowed =
			(curr == PaymentStatus.PENDING && (next == PaymentStatus.PAID || next == PaymentStatus.FAILED)) ||
				(curr == PaymentStatus.PAID && next == PaymentStatus.REFUNDED);

		if ((!allowed)) {
			throw new IllegalStateException("허용되지 않은 상태입니다. 현재=" + curr + ", 요청=" + next);
		}
		order.setPaymentStatus(next);

		var audit = orderAuditRepository.findById(orderId)
			.orElseThrow(() -> new IllegalStateException("주문 감사 레코드를 찾을 수 없습니다."));
		audit.setUpdatedAt(OffsetDateTime.now());
		audit.setUpdatedBy(customerId);

		return ResponseEntity.noContent().build();
	}
}
