package com.example.msapaymentservice.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.example.msapaymentservice.dto.PaymentPrepareCommand;
import com.example.msapaymentservice.kafka.producer.PaymentEventsPublisher;
import com.example.msapaymentservice.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCommandsListener {


	private final PaymentEventsPublisher publisher;
	private final ObjectMapper om;

	@KafkaListener(topics = "${topics.payment.events}", groupId = "payment-service-group")
	public void onPaymentCommand(@Header("x-event-type") String type,
		@Header(KafkaHeaders.RECEIVED_KEY) String key,
		@Header(name="x-event-id", required=false) String eventId,
		@Header(name="x-correlation-id", required=false) String correlationId,
		@Header(name="x-causation-id", required=false) String causationId,
		String body,
		Acknowledgment ack) throws Exception {
		try {
			if (!"payment.prepare.command".equals(type)) {
				log.warn("[payment] Unknown command type={}, key={}", type, key);
				ack.acknowledge();
				return;
			}

			PaymentPrepareCommand dto = om.readValue(body, PaymentPrepareCommand.class);

			java.util.Map<String, Object> accepted = new java.util.HashMap<>();
			accepted.put("orderId", dto.getOrderId().toString());
			accepted.put("customerId", dto.getCustomerId() != null ? dto.getCustomerId().toString() : null);
			accepted.put("amount", dto.getAmount());
			accepted.put("acceptedAt", java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC).toString());

			String corr = (correlationId == null || correlationId.isBlank())
				? java.util.UUID.randomUUID().toString() : correlationId;
			String cause = (eventId != null && !eventId.isBlank()) ? eventId : causationId;

			publisher.paymentPrepareAccepted(dto.getOrderId().toString(), accepted, corr, cause);

			log.info("[payment] payment.prepare.accepted published. orderId={}, corr={}, cause={}",
				dto.getOrderId(), corr, cause);
			ack.acknowledge();

		} catch (Exception e) {
			log.error("[payment] Error processing command. type={}, key={}, body={}", type, key, body, e);
			throw e;
		}
	}

}
