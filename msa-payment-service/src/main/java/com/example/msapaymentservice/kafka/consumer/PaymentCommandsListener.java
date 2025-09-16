package com.example.msapaymentservice.kafka.consumer;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.boot.autoconfigure.jms.AcknowledgeMode;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.example.common.exception.BusinessException;
import com.example.common.exception.CommonCode;
import com.example.msapaymentservice.client.TossPaymentClient;
import com.example.msapaymentservice.dto.PaymentPrepareCommand;
import com.example.msapaymentservice.entity.PaymentEntity;
import com.example.msapaymentservice.kafka.producer.PaymentEventsPublisher;
import com.example.msapaymentservice.repository.PaymentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCommandsListener {


	private final PaymentEventsPublisher publisher;
	private final PaymentRepository paymentRepository;
	private final TossPaymentClient tossPaymentClient;
	private final ObjectMapper om;

	@KafkaListener(topics = "${topics.payment.events}", groupId = "payment-service-group")
	public void onPaymentCommand(@Header("x-event-type") String type,
		@Header(KafkaHeaders.RECEIVED_KEY) String key,
		String body,
		Acknowledgment ack) throws Exception {
		try {
			if (!"payment.prepare".equals(type)) {
				log.warn("[payment] Unknown command type={}, key={}", type, key);
				ack.acknowledge();
				return;
			}

			PaymentPrepareCommand dto = om.readValue(body, PaymentPrepareCommand.class);

			java.util.Map<String, Object> accepted = new java.util.HashMap<>();
			accepted.put("orderId", dto.getOrderId());
			accepted.put("customerId", dto.getCustomerId() != null ? dto.getCustomerId() : null);
			accepted.put("amount", dto.getAmount());
			accepted.put("occurredAt", Instant.now());


			publisher.paymentPrepareAccepted(dto.getOrderId().toString(), accepted);

			log.info("[payment] payment.prepare.accepted published. orderId={}",
				dto.getOrderId());
			ack.acknowledge();

		} catch (Exception e) {
			log.error("[payment] Error processing command. type={}, key={}, body={}", type, key, body, e);
			throw e;
		}
	}

	@KafkaListener(topics = "${topics.payment.events}", groupId = "payment-service-group")
	public void onPaymentCancelRequested(@Header("x-event-type") String type,
		@Header(KafkaHeaders.RECEIVED_KEY) String key,
		String body,
		Acknowledgment ack) throws Exception {
		if ("payment.cancel.requested".equals(type)) {
			ack.acknowledge();
			return;
		}

		try {
			JsonNode json = om.readTree(body);
			UUID orderId = UUID.fromString(json.get("orderId").asText());
			UUID customerId = UUID.fromString(json.get("customerId").asText());

			var payment = paymentRepository.findByOrderId(orderId)
				.orElseThrow(() -> new BusinessException(CommonCode.PAYMENT_SEARCH_FAILED));

			String canceled = tossPaymentClient.cancelPayment(customerId, payment.getPaymentKey(), "REQUEST_CANCEL");

			Map<String, Object> envelope = new HashMap<>();
			envelope.put("eventId", UUID.randomUUID());
			envelope.put("orderId", orderId);
			envelope.put("customerId", customerId);
			envelope.put("status", canceled);
			envelope.put("occurredAt", Instant.now());

			publisher.paymentCancelResult(orderId.toString(), envelope);

			log.info("[Payment] payment.cancel.result 발행 완료. orderId={}, result={}", orderId, canceled);

			ack.acknowledge();
		} catch (Exception e) {
			try {
				JsonNode json = om.readTree(body);
				String orderId = json.path("orderId").asText();
				Map<String, Object> envelope = new HashMap<>();
				envelope.put("eventId", UUID.randomUUID());
				envelope.put("orderId", orderId);
				envelope.put("statusRaw", "ERROR:" + e.getMessage());
				envelope.put("occurredAt", Instant.now().toString());
				publisher.paymentCancelResult(orderId, envelope);
				log.warn("[Payment] payment.cancel.result(실패) 발행. orderId={}, error={}", orderId, e.toString());
			} catch (Exception ignore) {
			}
			log.error("[Payment] 결제 취소 요청 처리 실패. key={}, body={}", key, body, e);
			throw e;
		}
	}
}
