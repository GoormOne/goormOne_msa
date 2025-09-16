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
	private final ObjectMapper om;

	@Value("${topics.payment.events}")
	private String payment;

	public void paymentPrepareAccepted(String orderId, Object envelope, String correlationId, String causationId) throws Exception {
		String payload = om.writeValueAsString(envelope);
		var msg = MessageBuilder.withPayload(payload)
			.setHeader(KafkaHeaders.TOPIC, payment)
			.setHeader(KafkaHeaders.KEY, orderId)
			.setHeader("x-event-type", "payment.prepare.accepted")
			.setHeader("x-event-version", "1")
			.setHeader("x-correlation-id", correlationId)
			.setHeader("x-causation-id", causationId)
			.setHeader("x-producer", "payment-service")
			.build();
		kafkaTemplate.send(msg);
	}

	public void paymentPrepared(String orderId, Object envelope, String correlationId, String causationId) throws Exception {
		String payload = om.writeValueAsString(envelope);
		var msg = MessageBuilder.withPayload(payload)
			.setHeader(KafkaHeaders.TOPIC, payment)
			.setHeader(KafkaHeaders.KEY, orderId)
			.setHeader("x-event-type", "payment.prepare")
			.setHeader("x-event-version", "1")
			.setHeader("x-correlation-id", correlationId)
			.setHeader("x-causation-id", causationId)
			.setHeader("x-producer", "payment-service")
			.build();
		kafkaTemplate.send(msg);
	}

	public void paymentResult(String orderId, Object envelope, String correlationId, String causationId) throws Exception {
		String payload = om.writeValueAsString(envelope);
		var msg = MessageBuilder.withPayload(payload)
			.setHeader(KafkaHeaders.TOPIC, payment)
			.setHeader(KafkaHeaders.KEY, orderId)
			.setHeader("x-event-type", "payment.result")
			.setHeader("x-event-version", "1")
			.setHeader("x-correlation-id", correlationId)
			.setHeader("x-causation-id", causationId)
			.setHeader("x-producer", "payment-service")
			.build();
		kafkaTemplate.send(msg);
	}

	public void paymentSuccess(String orderId, Object envelope, String correlationId, String causationId) throws Exception {
		String payload = om.writeValueAsString(envelope);
		var msg = MessageBuilder.withPayload(payload)
			.setHeader(KafkaHeaders.TOPIC, payment)
			.setHeader(KafkaHeaders.KEY, orderId)
			.setHeader("x-event-type", "payment.success")
			.setHeader("x-event-version", "1")
			.setHeader("x-correlation-id", correlationId)
			.setHeader("x-causation-id", causationId)
			.setHeader("x-producer", "payment-service")
			.build();
		kafkaTemplate.send(msg);
	}

	public void paymentFailed(String orderId, Object envelope, String correlationId, String causationId) throws Exception {
		String payload = om.writeValueAsString(envelope);
		var msg = MessageBuilder.withPayload(payload)
			.setHeader(KafkaHeaders.TOPIC, payment)
			.setHeader(KafkaHeaders.KEY, orderId)
			.setHeader("x-event-type", "payment.failed")
			.setHeader("x-event-version", "1")
			.setHeader("x-correlation-id", correlationId)
			.setHeader("x-causation-id", causationId)
			.setHeader("x-producer", "payment-service")
			.build();
		kafkaTemplate.send(msg);
	}
}
