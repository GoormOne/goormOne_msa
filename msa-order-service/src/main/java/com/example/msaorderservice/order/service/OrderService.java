package com.example.msaorderservice.order.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.msaorderservice.order.dto.OrderCreateReq;
import com.example.msaorderservice.order.dto.OrderCreateRes;
import com.example.msaorderservice.order.dto.OrderRes;

public interface OrderService {
	OrderCreateRes createOrder(UUID customerId, OrderCreateReq req);

	Page<OrderRes> getMyOrders(UUID customerId, Pageable pageable);

	OrderRes getMyOrderDetail(UUID customerId, UUID orderId);
}
