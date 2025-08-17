package com.example.msaorderservice.order.service;

import java.util.UUID;

import com.example.msaorderservice.order.dto.OrderCreateReq;
import com.example.msaorderservice.order.dto.OrderCreateRes;

public interface OrderService {
	OrderCreateRes createOrder(UUID customerId, OrderCreateReq req);
}
