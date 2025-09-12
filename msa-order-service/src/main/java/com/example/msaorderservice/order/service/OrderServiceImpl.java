package com.example.msaorderservice.order.service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.example.common.exception.BusinessException;
import com.example.common.exception.CommonCode;
import com.example.msaorderservice.order.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.example.common.entity.PaymentStatus;
import com.example.msaorderservice.cart.dto.MenuLookUp;
import com.example.msaorderservice.cart.entity.CartEntity;
import com.example.msaorderservice.cart.entity.CartItemEntity;
import com.example.msaorderservice.order.client.MenuInventoryClient;
import com.example.msaorderservice.order.client.PaymentClient;
import com.example.msaorderservice.order.client.StoreClient;
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
import com.example.msaorderservice.order.kafka.service.OrchestratorService;
import com.example.msaorderservice.order.repository.OrderAuditRepository;
import com.example.msaorderservice.order.repository.OrderItemRepository;
import com.example.msaorderservice.order.repository.OrderRepository;
import com.example.msaorderservice.cart.repository.CartItemRepository;
import com.example.msaorderservice.cart.repository.CartRepository;
import com.example.msaorderservice.cart.service.MenuClient;

import lombok.RequiredArgsConstructor;

@Slf4j
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
	private final MenuInventoryClient menuInventoryClient;
	private final PaymentClient paymentClient;
	private final OrchestratorService orchestratorService;

	@Override
	@Transactional
	public OrderCreateRes createOrder(UUID customerId, OrderCreateReq req) {
		if (orderRepository.existsByCustomerIdAndPaymentStatus(customerId, PaymentStatus.PENDING)) {
			throw new BusinessException(CommonCode.ORDER_IS_NOT_PAID);
		}

		CartEntity cart = cartRepository.findFirstByCustomerId(customerId)
			.orElseThrow(() -> new BusinessException(CommonCode.CART_NOT_FOUND));
		var cartItems = cartItemRepository.findByCartId(cart.getCartId(), Pageable.unpaged()).getContent();
		if (cartItems.isEmpty())
			throw new BusinessException(CommonCode.CART_ITEM_NOT_FOUND);

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
			MenuLookUp menu = menuClient.getMenuDetail(storeId,ci.getMenuId());

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

		List<OrderItemEntity> reserved = new ArrayList<>();
		try{
			for (OrderItemEntity item : toSave) {
				menuInventoryClient.reserve(item.getMenuId(), item.getQuantity());
				reserved.add(item);
			}
		} catch(Exception e){
			for (OrderItemEntity item : reserved) {
				try {
					menuInventoryClient.release(item.getMenuId(), item.getQuantity());
				} catch (Exception ignore) {}
			}
			throw new BusinessException(CommonCode.RESERVED_FAIL_RETRY);
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

		final UUID oid = order.getOrderId();
		final int totalAmount = total;

		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit(){
				try {
					orchestratorService.startSaga(oid, totalAmount);
					log.info("[Saga] AFTER_COMMIT -> PaymentPrepareReq published. orderId={}, amount={}", oid, totalAmount);
				} catch (Exception e) {
					log.error("[Saga] publish after-commit failed. orderId={}", oid, e);
				}
			}
		});

		log.info(CommonCode.ORDER_CREATE.getMessage());

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
	public Page<OrderSummaryRes> getMyOrders(UUID customerId, Pageable pageable) {
		return null;
	}

	@Cacheable(
			value = "myOrders",
			key = "#customerId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
	@Transactional(readOnly = true)
	public PageCache<OrderSummaryRes> getMyOrdersCache(UUID customerId, Pageable pageable) {

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

		Page<OrderSummaryRes> result = page.map(o -> {
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
				preview = items.get(0).getMenuName() + " 외  " + (count - 1) + "개";
			}

			log.info(CommonCode.ORDER_SEARCH.getMessage());

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
		return PageCache.fromPage(result);
	}

	@Override
	@Cacheable(
			value = "myOrderDetail",
			key = "#customerId + '_' + #orderId"
	)
	@Transactional(readOnly = true)
	public CustomerOrderDetailRes getMyOrderDetail(UUID customerId, UUID orderId) {
		OrderEntity order = orderRepository.findByOrderIdAndCustomerId(orderId, customerId)
			.orElseThrow(() -> new BusinessException(CommonCode.ORDER_NOT_FOUND));


		List<OrderItemEntity> items = orderItemRepository.findByOrderId_OrderId(orderId);

		String storeName = storeClient.getStoreDetail(order.getStoreId()).getStoreName();
		OffsetDateTime createdAt = orderAuditRepository.findById(orderId)
			.map(OrderAuditEntity::getCreatedAt)
			.orElse(null);

		log.info(CommonCode.ORDER_SEARCH.getMessage());

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
	public PageCache<OrderSummaryRes> getOwnerOrders(UUID ownerId, UUID storeId, Pageable pageable) {
		return getOwnerOrdersCache(ownerId, storeId, pageable);
	}

	@Cacheable(
			value = "ownerOrder",
			key = "#ownerId + '_' + #storeId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize"
	)
	@Transactional(readOnly = true)
	public PageCache<OrderSummaryRes> getOwnerOrdersCache(UUID ownerId, UUID storeId, Pageable pageable) {


		StoreLookUp store = storeClient.getStoreDetail(storeId);


		UUID ownerFromStore = store.getOwnerId();
		if (ownerFromStore != null && !ownerFromStore.equals(ownerId)) {
			throw new BusinessException(CommonCode.STORE_AUTH_FAIL);
		}


		Page<OrderEntity> page = orderRepository.findByStoreIdIn(List.of(storeId), pageable);
		List<UUID> orderIds = page.stream()
				.map(OrderEntity::getOrderId)
				.toList();


		Map<UUID, List<OrderItemEntity>> itemsByOrderId = orderItemRepository
				.findByOrderId_OrderIdIn(orderIds)
				.stream()
				.collect(Collectors.groupingBy(it -> it.getOrderId().getOrderId()));


		Map<UUID, OffsetDateTime> createdAtByOrderId = orderAuditRepository
				.findAllById(orderIds)
				.stream()
				.collect(Collectors.toMap(OrderAuditEntity::getAuditId, OrderAuditEntity::getCreatedAt));

		final String storeName = store.getStoreName();


		Page<OrderSummaryRes> mappedPage = page.map(o -> {
			List<OrderItemEntity> items = itemsByOrderId.getOrDefault(o.getOrderId(), List.of());
			String first = items.isEmpty() ? "-" : items.get(0).getMenuName();
			int extra = Math.max(0, items.size() - 1);
			String preview = items.isEmpty() ? "-" : (extra > 0 ? first + " 외 " + extra + "개" : first);

			return OrderSummaryRes.builder()
					.orderId(o.getOrderId())
					.storeId(o.getStoreId())
					.storeName(storeName)
					.orderStatus(o.getOrderStatus())
					.totalPrice(o.getTotalPrice())
					.createdAt(createdAtByOrderId.get(o.getOrderId()))
					.summaryTitle(preview)
					.itemCount(items.size())
					.build();
		});

		log.info(CommonCode.ORDER_SEARCH.getMessage());

		return PageCache.fromPage(mappedPage);
	}


	@Override
	@Cacheable(
			value = "ownerOrderDetail",
			key = "#ownerId + '_' + #storeId"
	)
	@Transactional(readOnly = true)
	public OwnerOrderDetailRes getOwnerOrderDetail(UUID orderId, UUID storeId, UUID ownerId) {

		OrderEntity order = orderRepository.findById(orderId)
			.orElseThrow(() -> new BusinessException(CommonCode.ORDER_NOT_FOUND));

		StoreLookUp store = storeClient.getStoreDetail(order.getStoreId());

		UUID ownerFromStore = store.getOwnerId();
		if (ownerFromStore != null && !ownerFromStore.equals(ownerId)) {
			throw new BusinessException(CommonCode.STORE_AUTH_FAIL);
		}

		List<OrderItemEntity> items = orderItemRepository.findByOrderId_OrderId(orderId);

		String storeName = storeClient.getStoreDetail(order.getStoreId()).getStoreName();
		OffsetDateTime createdAt = orderAuditRepository.findById(orderId)
			.map(OrderAuditEntity::getCreatedAt)
			.orElse(null);

		log.info(CommonCode.ORDER_SEARCH.getMessage());

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
			.orElseThrow(() -> new BusinessException(CommonCode.ORDER_NOT_FOUND));

		StoreLookUp store = storeClient.getStoreDetail(order.getStoreId());

		UUID ownerFromStore = store.getOwnerId();
		if (Objects.equals(ownerFromStore, ownerId)) {
			throw new BusinessException(CommonCode.STORE_AUTH_FAIL);
		}

		OrderStatus oldStatus = order.getOrderStatus();
		PaymentStatus nowPaymentStatus = order.getPaymentStatus();

		if (newStatus == OrderStatus.COOKING
			&& oldStatus == OrderStatus.CONFIRMED
			&& nowPaymentStatus == PaymentStatus.PAID) {
			var items = orderItemRepository.findByOrderId_OrderId(orderId);

			for (OrderItemEntity item : items) {
				menuInventoryClient.confirm(item.getMenuId(), item.getQuantity());
			}
		}

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

		log.info(CommonCode.ORDER_UPDATE.getMessage());

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

	public CustomerOrderDetailRes cancelMyOrder(UUID customerId, UUID orderId) {
		OrderEntity order = orderRepository.findByOrderIdAndCustomerId(orderId, customerId)
			.orElseThrow(() -> new BusinessException(CommonCode.ORDER_NOT_FOUND));

		if (order.getOrderStatus() == OrderStatus.CANCELED) {
			return getMyOrderDetail(customerId, orderId);
		}

		if ((order.getOrderStatus() == OrderStatus.CONFIRMED
			|| order.getOrderStatus() == OrderStatus.COOKING
			|| order.getOrderStatus() == OrderStatus.DELIVERING
			|| order.getOrderStatus() == OrderStatus.COMPLETED)
			&& order.getPaymentStatus() == PaymentStatus.PAID) {
			throw new BusinessException(CommonCode.ORDER_CANCEL_FAIL);
		}

		if (order.getPaymentStatus() == PaymentStatus.PAID) {
			paymentClient.cancelPayment(order.getOrderId(), customerId,"USER_CANCELED");
			order.setPaymentStatus(PaymentStatus.REFUNDED);
		}

		var items = orderItemRepository.findByOrderId_OrderId(orderId);

		for (OrderItemEntity item : items) {
			menuInventoryClient.release(item.getMenuId(), item.getQuantity());
		}

		order.setOrderStatus(OrderStatus.CANCELED);
		order.setPaymentStatus(PaymentStatus.REFUNDED);
		orderRepository.save(order);

		orderAuditRepository.findById(orderId).ifPresent(a -> {
			a.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
			a.setUpdatedBy(order.getCustomerId());
			orderAuditRepository.save(a);
		});

		log.info(CommonCode.ORDER_CANCEL.getMessage());

		return getMyOrderDetail(customerId, orderId);
	}

	public OwnerOrderDetailRes cancelStoreOrder(UUID ownerId, UUID storeId, UUID orderId) {
		OrderEntity order = orderRepository.findById(orderId)
			.orElseThrow(() -> new BusinessException(CommonCode.ORDER_NOT_FOUND));

		StoreLookUp store = storeClient.getStoreDetail(order.getStoreId());

		UUID ownerFromStore = store.getOwnerId();
		if (Objects.equals(ownerFromStore, ownerId)) {
			throw new BusinessException(CommonCode.STORE_AUTH_FAIL);
		}

		if (order.getOrderStatus() == OrderStatus.CANCELED) {
			return getOwnerOrderDetail(ownerId, storeId, orderId);
		}

		if (order.getOrderStatus() == OrderStatus.CONFIRMED) {
			throw new BusinessException(CommonCode.ORDER_CANCEL_FAIL);
		}

		if (order.getPaymentStatus() == PaymentStatus.PAID) {
			paymentClient.cancelPayment(order.getOrderId(), ownerId,"USER_CANCELED");
			order.setPaymentStatus(PaymentStatus.REFUNDED);
		}

		order.setOrderStatus(OrderStatus.CANCELED);
		order.setPaymentStatus(PaymentStatus.REFUNDED);

		orderRepository.save(order);

		orderAuditRepository.findById(orderId).ifPresent(a -> {
			a.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
			a.setUpdatedBy(ownerId);
			orderAuditRepository.save(a);
		});

		log.info(CommonCode.ORDER_CANCEL.getMessage());

		return getOwnerOrderDetail(ownerId, storeId, orderId);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<OrderEntity> findLatestPendingOrder(UUID customerId) {
		if (customerId == null) {
			throw new BusinessException(CommonCode.USER_REQUIRED);
		}
		return orderRepository.findTopByCustomerIdAndPaymentStatus(
			customerId,
			PaymentStatus.PENDING
		);
	}
}
