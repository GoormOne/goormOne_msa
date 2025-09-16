package com.example.msaorderservice.order.kafka.producer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderCommandPublisher {

	private final KafkaTemplate<String, String> kafkaTemplate;
	private final ObjectMapper om;


	@Value("${topics.order.events}")
	private String order;

	@Value("${topics.payment.commands}")
	private String payment;

	// @Value("${topics.payment.events}")
	// private String payment;

	@Value("${topics.stock.events}")
	private String stock;

	public void orderCreated(String orderId, Object envelope) throws Exception {
		String payload = om.writeValueAsString(envelope);
		var msg = MessageBuilder.withPayload(payload)
			.setHeader(KafkaHeaders.TOPIC, order)
			.setHeader(KafkaHeaders.KEY, orderId)
			.setHeader("x-event-type", "order.created")
			.setHeader("x-event-version", "1")
			.setHeader("x-producer", "order-service")
			.build();
		kafkaTemplate.send(msg);
	}

	public void paymentPrepare(String orderId, Object envelope) throws Exception {
		String payload = om.writeValueAsString(envelope);
		var msg = MessageBuilder.withPayload(payload)
			.setHeader(KafkaHeaders.TOPIC, payment)
			.setHeader(KafkaHeaders.KEY, orderId)
			.setHeader("x-event-type", "payment.prepare")
			.setHeader("x-event-version", "1")
			.setHeader("x-producer", "order-service")
			.build();
		kafkaTemplate.send(msg);
	}

	public void paymentStatusChanged(String orderId, Object envelope) throws Exception {
		String payload = om.writeValueAsString(envelope);
		var msg = MessageBuilder.withPayload(payload)
			.setHeader(KafkaHeaders.TOPIC, order)
			.setHeader(KafkaHeaders.KEY, orderId)
			.setHeader("x-event-type", "payment.status.changed")
			.setHeader("x-event-version", "1")
			.setHeader("x-producer", "order-service")
			.build();
		kafkaTemplate.send(msg);
	}

	public void orderStatusChanged(String orderId, Object envelope) throws Exception {
		String payload = om.writeValueAsString(envelope);
		var msg = MessageBuilder.withPayload(payload)
			.setHeader(KafkaHeaders.TOPIC, order)
			.setHeader(KafkaHeaders.KEY, orderId)
			.setHeader("x-event-type", "order.status.changed")
			.setHeader("x-event-version", "1")
			.setHeader("x-producer", "order-service")
			.build();
		kafkaTemplate.send(msg);
	}

	public void paymentCancelRequested(String orderId, Object envelope) throws Exception {
		String payload = om.writeValueAsString(envelope);
		var msg = MessageBuilder.withPayload(payload)
			.setHeader(KafkaHeaders.TOPIC, payment)
			.setHeader(KafkaHeaders.KEY, orderId)
			.setHeader("x-event-type", "payment.cancel.requested")
			.setHeader("x-event-version", "1")
			.setHeader("x-producer", "order-service")
			.build();
		kafkaTemplate.send(msg);
	}
}
