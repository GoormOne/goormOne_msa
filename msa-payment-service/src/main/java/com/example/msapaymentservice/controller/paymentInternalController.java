package com.example.msapaymentservice.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.common.dto.ApiResponse;
import com.example.msapaymentservice.repository.PaymentRepository;
import com.example.msapaymentservice.service.PaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/internal/payments")
@RequiredArgsConstructor
public class paymentInternalController {

	private final PaymentService paymentService;
	private final PaymentRepository paymentRepository;

	@PostMapping("/{orderId}/cancel")
	public ApiResponse<Void> cancelByOrderId(@RequestHeader("X-User-Id") UUID customerId,
		@PathVariable UUID orderId,
		@RequestParam(defaultValue = "ORDER_CANCELED") String reason) {
		var payment = paymentRepository.findByOrderId(orderId)
			.orElseThrow(() -> new IllegalArgumentException("해당 주문의 결제내역이 없습니다."));
		paymentService.cancelPayment(customerId, payment.getPaymentKey(), reason);
		return ApiResponse.success();
	}
}
