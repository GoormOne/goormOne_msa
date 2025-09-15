package com.example.msaorderservice.order.kafka.consumer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.example.msaorderservice.order.dto.PaymentSuccessRes;
import com.example.msaorderservice.order.dto.PaymentFailRes;
import com.example.msaorderservice.order.kafka.producer.OrderCommandPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrchestratorListeners {

	private final ObjectMapper om = new ObjectMapper();
	private final OrderCommandPublisher publisher;

	@KafkaListener(topics = "${topics.stock.events}")
	public void stockReservationResult(@Header("x-event-type") String type,
		@Header(KafkaHeaders.RECEIVED_KEY) String key,
		@Header(name="x-correlation-id", required=false) String corr,
		@Header(name = "x-causation-id", required = false)  String causationId,
		String body,
		Acknowledgment ack) throws Exception {

		if (!"stock.reservation".equals(type)) return;

		try {
			var json = om.readTree(body);
			String orderId = json.get("orderId").asText();

			Map<String, Object> payEnvelope = new HashMap<>();
			payEnvelope.put("orderId", orderId);
			payEnvelope.put("amount", json.get("amount").asInt());

			String correlationId = (corr == null || corr.isBlank())
				? UUID.randomUUID().toString()
				: corr;

			publisher.paymentPrepare(orderId, payEnvelope, correlationId, causationId);

			log.info("[Saga] stock.reservation -> payment.prepare.command 발행. corr={}, orderId={}", corr, orderId);
			ack.acknowledge();
		} catch (Exception e) {
			log.error("[Saga] stock.reservation 처리 실패, key={}, body={}", key, body, e);
			throw e;
		}
	}

	// @KafkaListener(topics = "${topics.payment.events}")
	// public void onPaymentEvent(@Header("x-event-type") String type,
	// 	@Header(KafkaHeaders.RECEIVED_KEY) String key,
	// 	String body,
	// 	Acknowledgment ack) throws Exception {
	//
	// 	try {
	// 		switch (type) {
	// 			case "PaymentAuthorizedRes" -> {
	// 				PaymentSuccessRes dto = om.readValue(body, PaymentSuccessRes.class);
	// 				publisher.onPaymentSuccess(dto);
	// 			}
	// 			case "PaymentFailedRes" -> {
	// 				PaymentFailRes dto = om.readValue(body, PaymentFailRes.class);
	// 				publisher.onPaymentFailed(dto);
	// 			}
	//
	// 			default -> log.warn("[Saga] Unknown paymetn event type={}, key={}", type, key);
	// 		}
	// 		ack.acknowledge();
	// 	} catch (Exception e) {
	// 		log.error("[Saga] Error while processing payment-event, type={}, key={}, body={}", type, key, body, e);
	// 		throw e;
	// 	}
	// }
}
