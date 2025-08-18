package com.example.msaorderservice.order.service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.msaorderservice.cart.dto.MenuLookUp;
import com.example.msaorderservice.cart.entity.CartEntity;
import com.example.msaorderservice.cart.entity.CartItemEntity;
import com.example.msaorderservice.order.dto.OrderCreateReq;
import com.example.msaorderservice.order.dto.OrderCreateRes;
import com.example.msaorderservice.order.dto.OrderLineRes;
import com.example.msaorderservice.order.dto.OrderRes;
import com.example.msaorderservice.order.dto.StoreLookUp;
import com.example.msaorderservice.order.entity.OrderAuditEntity;
import com.example.msaorderservice.order.entity.OrderEntity;
import com.example.msaorderservice.order.entity.OrderItemEntity;
import com.example.msaorderservice.order.entity.OrderStatus;
import com.example.msaorderservice.order.entity.PaymentStatus;
import com.example.msaorderservice.order.repository.OrderAuditRepository;
import com.example.msaorderservice.order.repository.OrderItemRepository;
import com.example.msaorderservice.order.repository.OrderRepository;
import com.example.msaorderservice.cart.repository.CartItemRepository;
import com.example.msaorderservice.cart.repository.CartRepository;
import com.example.msaorderservice.cart.service.MenuClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

	private final CartRepository cartRepository;
	private final CartItemRepository cartItemRepository;
	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	private final OrderAuditRepository orderAuditRepository;
	private final MenuClient menuClient;
	private final StoreClient storeClient;

	@Override
	@Transactional
	public OrderCreateRes createOrder(UUID customerId, OrderCreateReq req) {
		CartEntity cart = cartRepository.findFirstByCustomerId(customerId)
			.orElseThrow(() -> new IllegalStateException("장바구니가 없습니다."));
		var cartItems = cartItemRepository.findByCartId(cart.getCartId(), Pageable.unpaged()).getContent();
		if (cartItems.isEmpty())
			throw new IllegalStateException("장바구니가 비어 있습니다.");

		UUID storeId = cart.getStoreId();

		OrderEntity order = new OrderEntity();
		order.setCustomerId(customerId);
		order.setStoreId(storeId);
		order.setAddressId(req.getAddressId());
		order.setRequestMessage(req.getRequestMessage());
		order.setOrderStatus(OrderStatus.PENDING);
		order.setPaymentStatus(PaymentStatus.PENDING);

		List<OrderItemEntity> toSave = new ArrayList<>();
		List<OrderLineRes> lines = new ArrayList<>();

		int total = 0;
		for (CartItemEntity ci : cartItems) {
			MenuLookUp menu = menuClient.getMenuDetail(ci.getMenuId());

			int unitPrice = menu.getMenuPrice();
			int qty = ci.getQuantity();

			OrderItemEntity oi = OrderItemEntity.builder()
				.orderId(order)
				.menuId(ci.getMenuId())
				.menuName(menu.getMenuName())
				.menuPrice(unitPrice)
				.quantity(qty)
				.lineTotal(unitPrice * qty)
				.build();

			toSave.add(oi);

			total += oi.getLineTotal();
			lines.add(new OrderLineRes(
				ci.getMenuId(),
				menu.getMenuName(),
				qty,
				unitPrice
			));
		}
		order.setTotalPrice(total);

		order = orderRepository.save(order);
		OrderEntity savedOrder = order;
		toSave.forEach(i -> i.setOrderId(savedOrder));
		orderItemRepository.saveAll(toSave);

		OrderAuditEntity audit = OrderAuditEntity.builder()
			.orderId(order)
			.createdAt(OffsetDateTime.now())
			.createdBy(customerId)
			.build();

		orderAuditRepository.save(audit);

		return new OrderCreateRes(
			order.getOrderId(),
			storeId,
			total,
			order.getOrderStatus(),
			order.getPaymentStatus(),
			lines
		);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<OrderRes> getMyOrders(UUID customerId, Pageable pageable) {

	}

	@Override
	@Transactional(readOnly = true)
	public OrderRes getMyOrderDetail(UUID customerId, UUID orderId) {
		OrderEntity order = orderRepository.findByOrderIdAndCustomerId(orderId, customerId)
			.orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

		List<OrderItemEntity> items = orderItemRepository.findByOrderId_OrderId(orderId);

		StoreLookUp store = storeClient.getStoreDetail(order.getStoreId());

		OffsetDateTime createdAt = orderAuditRepository.findByAuditId(orderId)
			.map(OrderAuditEntity::getCreatedAt)
			.orElseThrow(() -> new IllegalStateException("감사 로그가 없습니다."));

		return toOrderRes(order, items, createdAt, store.getStoreName());
	}

	private OrderRes toOrderRes(OrderEntity o, List<OrderItemEntity> items, OffsetDateTime createdAt,
		String storeName) {
		return OrderRes.builder()
			.orderId(o.getOrderId())
			.customerId(o.getCustomerId())
			.storeId(o.getStoreId())
			.storeName(storeName)
			.orderStatus(o.getOrderStatus())
			.totalPrice(o.getTotalPrice())
			.createdAt(createdAt)
			.items(items.stream()
				.map(it -> OrderRes.OrderItemDto.builder()
					.menuId(it.getMenuId())
					.menuName(it.getMenuName())
					.quantity(it.getQuantity())
					.menuPrice(it.getMenuPrice())
					.lineTotal(it.getLineTotal())
					.build())
				.collect(Collectors.toList()))
			.build();
	}

	@Override
	@Transactional
	public OrderRes updateOrderStatusByOwner(UUID ownerId, UUID orderId, OrderStatus newStatus) {
		OrderEntity order = orderRepository.findById(orderId)
			.orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

		StoreLookUp store = storeClient.getStoreDetail(order.getStoreId());

		order.setOrderStatus(newStatus);
		orderRepository.save(order);

		var audit = orderAuditRepository.findById(orderId)
			.orElseThrow(() -> new IllegalStateException("주문 감사 레코드가 없습니다."));
		audit.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
		audit.setUpdatedBy(ownerId);
		orderAuditRepository.save(audit);

		var items = orderItemRepository.findByOrderId_OrderId(orderId);
		return toOrderRes(order, items, audit.getUpdatedAt(), store.getStoreName());
	}
}
