package com.example.msaorderservice.order.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.example.msaorderservice.order.dto.PaymentSuccessRes;
import com.example.msaorderservice.order.dto.PaymentFailRes;
import com.example.msaorderservice.order.kafka.service.OrchestratorService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrchestratorListeners {

	private final ObjectMapper om = new ObjectMapper();
	private final OrchestratorService orchestratorService;

	@KafkaListener(topics = "${topics.payment.events}", groupId = "order-svc-grp")
	public void onPaymentEvent(@Header("x-event-type") String type,
		@Header(KafkaHeaders.RECEIVED_KEY) String key,
		String body,
		Acknowledgment ack) throws Exception {

		try {
			switch (type) {
				case "PaymentAuthorizedRes" -> {
					PaymentSuccessRes dto = om.readValue(body, PaymentSuccessRes.class);
					orchestratorService.onPaymentSuccess(dto);
				}
				case "PaymentFailedRes" -> {
					PaymentFailRes dto = om.readValue(body, PaymentFailRes.class);
					orchestratorService.onPaymentFailed(dto);
				}

				default -> log.warn("[Saga] Unknown paymetn event type={}, key={}", type, key);
			}
			ack.acknowledge();
		} catch (Exception e) {
			log.error("[Saga] Error while processing payment-event, type={}, key={}, body={}", type, key, body, e);
			throw e;
		}
	}
}
