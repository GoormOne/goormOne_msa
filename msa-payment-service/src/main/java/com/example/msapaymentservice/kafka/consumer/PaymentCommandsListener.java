package com.example.msapaymentservice.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.example.msapaymentservice.client.TossPaymentClient;
import com.example.msapaymentservice.dto.PaymentFailRes;
import com.example.msapaymentservice.dto.PaymentPrepareCommand;
import com.example.msapaymentservice.dto.PaymentPrepareReq;
import com.example.msapaymentservice.dto.PaymentSuccessRes;
import com.example.msapaymentservice.kafka.producer.PaymentEventsPublisher;
import com.example.msapaymentservice.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCommandsListener {


	private final PaymentService paymentService;
	private final PaymentEventsPublisher publisher;
	private final ObjectMapper om;

	@KafkaListener(topics = "${topics.payment.inbound}", groupId = "payment-svc-grp")
	public void onPaymentCommand(@Header("x-event-type") String type,
		@Header(KafkaHeaders.RECEIVED_KEY) String key,
		@Header(name="x-event-id", required=false) String eventId,           // 👈 커맨드의 event-id
		@Header(name="x-correlation-id", required=false) String correlationId,
		@Header(name="x-causation-id", required=false) String causationId,   // (있으면 전달됨)
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
			String cause = (eventId != null && !eventId.isBlank()) ? eventId : causationId; // 커맨드 event-id를 원인으로

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
