package com.example.msaorderservice.order.service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.example.msaorderservice.order.dto.CustomerOrderDetailRes;
import com.example.msaorderservice.order.dto.OrderSummaryRes;
import com.example.msaorderservice.order.dto.OwnerOrderDetailRes;
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
	public Page<OrderSummaryRes> getMyOrders(UUID customerId, Pageable pageable) {
		Page<OrderEntity> page = orderRepository.findByCustomerId(customerId, pageable);

		List<UUID> orderIds = page.stream().map(OrderEntity::getOrderId).toList();

		Map<UUID, List<OrderItemEntity>> itemsByOrderId = orderItemRepository
			.findByOrderId_OrderIdIn(orderIds)
			.stream()
			.collect(Collectors.groupingBy(it -> it.getOrderId().getOrderId()));

		Map<UUID, OffsetDateTime> createdAtByOrderId = orderAuditRepository.findAllById(orderIds)
			.stream()
			.collect(Collectors.toMap(OrderAuditEntity::getAuditId, OrderAuditEntity::getCreatedAt));

		Map<UUID, String> storeNameCache = new HashMap<>();

		return page.map(o -> {
			List<OrderItemEntity> items = itemsByOrderId.getOrDefault(o.getOrderId(), List.of());

			String storeName = storeNameCache.computeIfAbsent(
				o.getStoreId(),
				sid -> storeClient.getStoreDetail(sid).getStoreName()
			);

			OffsetDateTime createdAt = createdAtByOrderId.get(o.getOrderId());

			String preview;
			int count = items.size();
			if (count == 0) {
				preview = "-";
			} else if (count == 1) {
				preview = items.get(0).getMenuName();
			} else {
				preview = items.get(0).getMenuName() + " 외 " + (count - 1) + "개";
			}

			return OrderSummaryRes.builder()
				.orderId(o.getOrderId())
				.storeId(o.getStoreId())
				.storeName(storeName)
				.orderStatus(o.getOrderStatus())
				.totalPrice(o.getTotalPrice())
				.createdAt(createdAt)
				.summaryTitle(preview)
				.itemCount(count)
				.build();
		});
	}

	@Override
	@Transactional(readOnly = true)
	public CustomerOrderDetailRes getMyOrderDetail(UUID customerId, UUID orderId) {
		OrderEntity order = orderRepository.findByOrderIdAndCustomerId(orderId, customerId)
			.orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

		List<OrderItemEntity> items = orderItemRepository.findByOrderId_OrderId(orderId);

		String storeName = storeClient.getStoreDetail(order.getStoreId()).getStoreName();
		OffsetDateTime createdAt = orderAuditRepository.findById(orderId)
			.map(OrderAuditEntity::getCreatedAt)
			.orElse(null);

		return CustomerOrderDetailRes.builder()
			.orderId(order.getOrderId())
			.customerId(customerId)
			.storeId(order.getStoreId())
			.storeName(storeName)
			.orderStatus(order.getOrderStatus())
			.totalPrice(order.getTotalPrice())
			.createdAt(createdAt)
			.items(items.stream().map(it ->
				CustomerOrderDetailRes.OrderItemDto.builder()
					.menuId(it.getMenuId())
					.menuName(it.getMenuName())
					.quantity(it.getQuantity())
					.menuPrice(it.getMenuPrice())
					.lineTotal(it.getLineTotal())
					.build()
			).toList())
			.build();
	}


	@Override
	@Transactional(readOnly = true)
	public Page<OrderSummaryRes> getOwnerOrders(UUID ownerId, Pageable pageable) {
		throw new UnsupportedOperationException("owner 주문 목록 조회는 store-service의 '점주 매장 목록' API가 필요합니다.");
	}


	@Override
	@Transactional(readOnly = true)
	public OwnerOrderDetailRes getOwnerOrderDetail(UUID orderId, UUID ownerId) {

		OrderEntity order = orderRepository.findById(orderId)
			.orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

		List<OrderItemEntity> items = orderItemRepository.findByOrderId_OrderId(orderId);

		String storeName = storeClient.getStoreDetail(order.getStoreId()).getStoreName();
		OffsetDateTime createdAt = orderAuditRepository.findById(orderId)
			.map(OrderAuditEntity::getCreatedAt)
			.orElse(null);

		return OwnerOrderDetailRes.builder()
			.orderId(order.getOrderId())
			.storeId(order.getStoreId())
			.customerId(order.getCustomerId())
			.storeName(storeName)
			.orderStatus(order.getOrderStatus())
			.totalPrice(order.getTotalPrice())
			.createdAt(createdAt)
			.items(items.stream().map(it ->
				OwnerOrderDetailRes.OrderItemDto.builder()
					.menuId(it.getMenuId())
					.menuName(it.getMenuName())
					.quantity(it.getQuantity())
					.menuPrice(it.getMenuPrice())
					.lineTotal(it.getLineTotal())
					.build()
			).toList())
			.build();
	}

	@Override
	@Transactional
	public OwnerOrderDetailRes updateOrderStatusByOwner(UUID ownerId, UUID orderId, OrderStatus newStatus) {
		OrderEntity order = orderRepository.findById(orderId)
			.orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

		order.setOrderStatus(newStatus);
		orderRepository.save(order);

		orderAuditRepository.findById(orderId).ifPresent(a -> {
			a.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
			a.setUpdatedBy(ownerId);
			orderAuditRepository.save(a);
		});

		List<OrderItemEntity> items = orderItemRepository.findByOrderId_OrderId(orderId);
		String storeName = storeClient.getStoreDetail(order.getStoreId()).getStoreName();
		OffsetDateTime createdAt = orderAuditRepository.findById(orderId)
			.map(OrderAuditEntity::getCreatedAt)
			.orElse(null);

		return OwnerOrderDetailRes.builder()
			.orderId(order.getOrderId())
			.storeId(order.getStoreId())
			.storeName(storeName)
			.orderStatus(order.getOrderStatus())
			.totalPrice(order.getTotalPrice())
			.createdAt(createdAt)
			.items(items.stream().map(it ->
				OwnerOrderDetailRes.OrderItemDto.builder()
					.menuId(it.getMenuId())
					.menuName(it.getMenuName())
					.quantity(it.getQuantity())
					.menuPrice(it.getMenuPrice())
					.lineTotal(it.getLineTotal())
					.build()
			).toList())
			.build();
	}
}
