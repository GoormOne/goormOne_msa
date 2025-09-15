package com.example.msapaymentservice.kafka.producer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentEventsPublisher {

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper om = new ObjectMapper();

	@Value("${topics.payment.events}")
	private String paymentEventsTopic;

	public void paymentSuccess(String orderId, Object envelope, String correlationId, String causationId) throws Exception {
		String payload = om.writeValueAsString(envelope);
		var msg = MessageBuilder.withPayload(payload)
			.setHeader(KafkaHeaders.TOPIC, paymentEventsTopic)
			.setHeader(KafkaHeaders.KEY, orderId)
			.setHeader("x-event-type", "PaymentSuccess")
			.setHeader("x-event-version", "1")
			.setHeader("x-correlation-Id", correlationId)
			.setHeader("x-causation-Id", causationId)
			.setHeader("x-producer", "payment-service")
			.build();
		kafkaTemplate.send(msg);
	}

	public void paymentFailed(String orderId, Object envelope, String correlationId, String causationId) throws Exception {
		String payload = om.writeValueAsString(envelope);
		var msg = MessageBuilder.withPayload(payload)
			.setHeader(KafkaHeaders.TOPIC, paymentEventsTopic)
			.setHeader(KafkaHeaders.KEY, orderId)
			.setHeader("x-event-type", "PaymentFailed")
			.setHeader("x-event-version", "1")
			.setHeader("x-correlation-Id", correlationId)
			.setHeader("x-causation-Id", causationId)
			.setHeader("x-producer", "payment-service")
			.build();
		kafkaTemplate.send(msg);
	}
}
