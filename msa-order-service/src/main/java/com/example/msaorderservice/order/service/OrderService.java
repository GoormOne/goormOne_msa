package com.example.msaorderservice.order.service;

import java.util.Optional;
import java.util.UUID;

import com.example.msaorderservice.order.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.msaorderservice.order.entity.OrderEntity;
import com.example.msaorderservice.order.entity.OrderStatus;
import org.springframework.hateoas.PagedModel;

public interface OrderService {
	OrderCreateRes createOrder(UUID customerId, OrderCreateReq req);

	Page<OrderSummaryRes> getMyOrders(UUID customerId, Pageable pageable);
//	PagedModel<OrderSummaryRes> getMyOrders(UUID customerId, Pageable pageable);
	PageCache<OrderSummaryRes> getMyOrdersCache(UUID customerId, Pageable pageable);
	CustomerOrderDetailRes getMyOrderDetail(UUID customerId, UUID orderId);

	PageCache<OrderSummaryRes> getOwnerOrders(UUID ownerId, UUID storeId, Pageable pageable);
	PageCache<OrderSummaryRes> getOwnerOrdersCache(UUID ownerId, UUID storeId, Pageable pageable);
	OwnerOrderDetailRes getOwnerOrderDetail(UUID orderId, UUID storeId, UUID ownerId);

	OwnerOrderDetailRes updateOrderStatusByOwner(UUID ownerId, UUID orderId, OrderStatus newStatus);

	CustomerOrderDetailRes cancelMyOrder(UUID customerId, UUID orderId);

	OwnerOrderDetailRes cancelStoreOrder(UUID ownerId, UUID storeId, UUID orderId);

	Optional<OrderEntity> findLatestPendingOrder(UUID customerId);

	void cancelDueToOutOfStock(UUID orderId);
}
