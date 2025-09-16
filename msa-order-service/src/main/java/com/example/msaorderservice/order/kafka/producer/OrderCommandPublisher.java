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


	@Value("${topics.order.inbound}")
	private String orderTopic;

	@Value("${topics.payment.outbound}")
	private String PaymentOut;

	@Value("${topics.payment.inbound}")
	private String paymentIn;

	@Value("${topics.stock.outbound}")
	private String stockOut;

	@Value("${topics.stock.inbound}")
	private String stockIn;

	public void orderCreated(String orderId, Object envelope, String correlationId, String causationId) throws Exception {
		String payload = om.writeValueAsString(envelope);
		var msg = MessageBuilder.withPayload(payload)
			.setHeader(KafkaHeaders.TOPIC, orderTopic)
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
			.setHeader(KafkaHeaders.TOPIC, PaymentOut)
			.setHeader(KafkaHeaders.KEY, orderId)
			.setHeader("x-event-type", "payment.prepare")
			.setHeader("x-event-version", "1")
			.setHeader("x-correlation-id", correlationId)
			.setHeader("x-causation-id", causationId)
			.setHeader("x-producer", "order-service")
			.build();
		kafkaTemplate.send(msg);
	}

	public void paymentStatusChanged(String orderId, Object envelope, String correlationId, String causationId) throws Exception {
		String payload = om.writeValueAsString(envelope);
		var msg = MessageBuilder.withPayload(payload)
			.setHeader(KafkaHeaders.TOPIC, orderTopic)
			.setHeader(KafkaHeaders.KEY, orderId)
			.setHeader("x-event-type", "payment.status.changed")
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
			.setHeader(KafkaHeaders.TOPIC, stockOut)
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
		kafkaTemplate.send(orderTopic, orderId, payload);
	}

	public void publishOrderFailed(String orderId, Object envelope) throws Exception {
		String payload = om.writeValueAsString(envelope);
		kafkaTemplate.send(orderTopic, orderId, payload);
	}
}
