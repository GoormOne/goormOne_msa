package com.example.msapaymentservice.kafka.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.msapaymentservice.dto.PaymentFailRes;
import com.example.msapaymentservice.dto.PaymentPrepareReq;
import com.example.msapaymentservice.dto.PaymentSuccessRes;
import com.example.msapaymentservice.kafka.producer.PaymentEventsPublisher;
import com.example.msapaymentservice.service.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentOrchestratorService {

	private final PaymentEventsPublisher publisher;
	private final PaymentService paymentService;

	public void onPaymentPrepare(PaymentPrepareReq dto) throws Exception {
		final var orderId = dto.getOrderId();
		final var amount = dto.getAmount();
		final var paymentKey = dto.getPaymentKey();


	}
}
