package com.example.msaorderservice.order.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.msaorderservice.order.dto.OrderCreateReq;
import com.example.msaorderservice.order.dto.OrderCreateRes;
import com.example.msaorderservice.order.dto.CustomerOrderDetailRes;
import com.example.msaorderservice.order.dto.OrderSummaryRes;
import com.example.msaorderservice.order.dto.OwnerOrderDetailRes;
import com.example.msaorderservice.order.entity.OrderStatus;

public interface OrderService {
	OrderCreateRes createOrder(UUID customerId, OrderCreateReq req);

	Page<OrderSummaryRes> getMyOrders(UUID customerId, Pageable pageable);

	CustomerOrderDetailRes getMyOrderDetail(UUID customerId, UUID orderId);

	Page<OrderSummaryRes> getOwnerOrders(UUID ownerId, Pageable pageable);

	OwnerOrderDetailRes getOwnerOrderDetail(UUID orderId, UUID ownerId);

	OwnerOrderDetailRes updateOrderStatusByOwner(UUID ownerId, UUID orderId, OrderStatus newStatus);
}
