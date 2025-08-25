package com.example.msapaymentservice.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.example.common.dto.OrderCheckoutView;

public interface PaymentService {

	OrderCheckoutView getCheckout(UUID customerId, UUID orderId);

	ResponseEntity<String> cancelPayment(UUID customerId, String paymentKey, String cancelReason);

	void handleSuccess(UUID customerId, UUID orderId, String paymentKey, int amount);

	void handleFail(UUID customerId, UUID orderId, String errorCode, String errorMsg);
}
