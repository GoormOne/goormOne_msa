package com.example.msaorderservice.order.kafka.producer;

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

	public void orderCreated(String orderId, Object envelope, String correlationId, String causationId) throws Exception {
		String payload = om.writeValueAsString(envelope);
		var msg = MessageBuilder.withPayload(payload)
			.setHeader(KafkaHeaders.TOPIC, "order-events")
			.setHeader(KafkaHeaders.KEY, orderId)
			.setHeader("x-event-type", "order.created")
			.setHeader("x-event-version", "1")
			.setHeader("x-correlation-id", correlationId)
			.setHeader("x-causation-id", causationId)
			.setHeader("x-producer", "order-service")
			.build();
		kafkaTemplate.send(msg);
	}

	public void paymentPrepare(String orderId, Object envelope, String correlationId, String causationId) throws Exception {
		String payload = om.writeValueAsString(envelope);
		var msg = MessageBuilder.withPayload(payload)
			.setHeader(KafkaHeaders.TOPIC, "payment-commands")
			.setHeader(KafkaHeaders.KEY, orderId)
			.setHeader("x-event-type", "orderCreated")
			.setHeader("x-event-version", "1")
			.setHeader("x-correlation-id", correlationId)
			.setHeader("x-causation-id", causationId)
			.setHeader("x-producer", "order-service")
			.build();
		kafkaTemplate.send(msg);
	}

	public void sendInventoryReserve(String orderId, Object envelope, String correlationId, String causationId) throws Exception {
		String payload = om.writeValueAsString(envelope);
		var msg = MessageBuilder.withPayload(payload)
			.setHeader(KafkaHeaders.TOPIC, "inventory-commands")
			.setHeader(KafkaHeaders.KEY, orderId)
			.setHeader("x-event-type", "InventoryReserveCommand")
			.setHeader("x-event-version", "1")
			.setHeader("x-correlation-id", correlationId)
			.setHeader("x-causation-id", causationId)
			.setHeader("x-producer", "order-service")
			.build();
		kafkaTemplate.send(msg);
	}

	public void publishOrderCompleted(String orderId, Object envelope) throws Exception {
		String payload = om.writeValueAsString(envelope);
		kafkaTemplate.send("order-events", orderId, payload);
	}

	public void publishOrderFailed(String orderId, Object envelope) throws Exception {
		String payload = om.writeValueAsString(envelope);
		kafkaTemplate.send("order-events", orderId, payload);
	}
}
