package com.example.msapaymentservice.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.example.common.dto.OrderCheckoutView;
import com.example.msapaymentservice.dto.PaymentSearchRes;


public interface PaymentService {

	Page<PaymentSearchRes> getMyPayments(UUID customerId, Pageable pageable);

	Page<PaymentSearchRes> getOwnerStorePayments(UUID ownerId, UUID storeId, Pageable pageable);

	OrderCheckoutView getCheckout(UUID customerId, UUID orderId);

	ResponseEntity<String> cancelPayment(UUID customerId, String paymentKey, String cancelReason);

	void handleSuccess(UUID customerId, UUID orderId, String paymentKey, int amount);

	void handleFail(UUID customerId, UUID orderId, String errorCode, String errorMsg);

	ResponseEntity<Void> redirectToCheckout(UUID customerId);
}
