package com.example.msaorderservice.order.kafka.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.msaorderservice.order.dto.PaymentSuccessRes;
import com.example.msaorderservice.order.dto.PaymentPrepareReq;
import com.example.msaorderservice.order.dto.PaymentFailRes;
import com.example.msaorderservice.order.kafka.producer.OrderCommandPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrchestratorService {

	private final OrderCommandPublisher orderCommandPublisher;

	public void startSaga(UUID orderId, int amount) throws Exception {
		var req = PaymentPrepareReq.builder()
			.orderId(orderId)
			.amount(amount)
			.build();

		orderCommandPublisher.sendPaymentAuthorize(
			orderId.toString(),
			req,
			orderId.toString(),
			null
		);
		log.debug("[Saga] startSagaWithoutKey -> PaymentPrepareReq sent. orderId={}", orderId);
	}

	public void onPaymentSuccess(PaymentSuccessRes res) throws Exception {
		UUID orderId = res.getOrderId();

		orderCommandPublisher.publishOrderCompleted(orderId.toString(), res);

		log.info("[Saga] onPaymentSuccess -> orderCompleted published. orderId={}", orderId);
	}

	public void onPaymentFailed(PaymentFailRes res) throws Exception {
		UUID orderId = res.getOrderId();

		orderCommandPublisher.publishOrderFailed(orderId.toString(), res);

		log.info("[Saga] onPaymentFailed -> orderFailed published. orderId={}, reason={}", orderId, res.getReason());
	}
}
