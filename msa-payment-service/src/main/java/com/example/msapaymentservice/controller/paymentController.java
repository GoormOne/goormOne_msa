package com.example.msapaymentservice.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.common.dto.OrderCheckoutView;
import com.example.msapaymentservice.service.PaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class paymentController {

	private final PaymentService paymentService;

	@GetMapping("/checkout")
	public ResponseEntity<OrderCheckoutView> checkout(
		@RequestHeader(value = "X-User-Id", required = false) UUID customerIdHeader,
		@RequestParam(value = "customerId", required = false) UUID customerIdParam,
		@RequestParam UUID orderId
	) {
		UUID customerId = (customerIdHeader != null) ? customerIdHeader : customerIdParam;
		if (customerId == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "customerId is required");
		}
		return ResponseEntity.ok(paymentService.getCheckout(customerId, orderId));
	}

	@GetMapping("/success")
	public ResponseEntity<Void> success(
		@RequestHeader(value = "X-User-Id", required = false) UUID userId,
		@RequestParam(required = false) UUID customerId,
		@RequestParam String paymentKey,
		@RequestParam UUID orderId,
		@RequestParam int amount
	) {
		UUID targetCustomerId = (userId != null) ? userId : customerId;
		paymentService.handleSuccess(targetCustomerId, orderId, paymentKey, amount);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/fail")
	public ResponseEntity<Void> fail(
		@RequestHeader(value = "X-User-Id", required = false) UUID userId,
		@RequestParam(required = false) UUID customerId,
		@RequestParam UUID orderId,
		@RequestParam String code,
		@RequestParam String message
	) {
		UUID targetCustomerId = (userId != null) ? userId : customerId;
		paymentService.handleFail(targetCustomerId, orderId, code, message);
		return ResponseEntity.noContent().build();
	}
}
