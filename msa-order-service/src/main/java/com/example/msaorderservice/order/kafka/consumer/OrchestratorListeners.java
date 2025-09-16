package com.example.msaorderservice.order.kafka.consumer;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.example.common.entity.PaymentStatus;
import com.example.common.exception.BusinessException;
import com.example.common.exception.CommonCode;
import com.example.msaorderservice.order.entity.OrderAuditEntity;
import com.example.msaorderservice.order.entity.OrderEntity;
import com.example.msaorderservice.order.entity.OrderStatus;
import com.example.msaorderservice.order.kafka.producer.OrderCommandPublisher;
import com.example.msaorderservice.order.repository.OrderAuditRepository;
import com.example.msaorderservice.order.repository.OrderRepository;
import com.example.msaorderservice.order.service.OrderService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrchestratorListeners {

	private final ObjectMapper om = new ObjectMapper();
	private final OrderCommandPublisher publisher;
	private final OrderService orderService;
	private final OrderAuditRepository orderAuditRepository;
	private final OrderRepository orderRepository;

	@KafkaListener(topics = "${topics.stock.events}")
	public void stockReservationResult(@Header("x-event-type") String type,
		@Header(KafkaHeaders.RECEIVED_KEY) String key,
		String body,
		Acknowledgment ack) throws Exception {

		if (!"stock.reservation".equals(type))
			return;

		try {
			var json = om.readTree(body);
			String orderId = json.get("orderId").asText();

			Map<String, Object> payEnvelope = new HashMap<>();
			payEnvelope.put("orderId", orderId);
			payEnvelope.put("customerId", json.get("customerId").asText());
			payEnvelope.put("amount", json.get("amount").asInt());

			publisher.paymentPrepare(orderId, payEnvelope);

			log.info("[Saga] stock.reservation -> payment.prepare.command 발행. orderId={}", orderId);
			ack.acknowledge();
		} catch (Exception e) {
			log.error("[Saga] stock.reservation 처리 실패, key={}, body={}", key, body, e);
			throw e;
		}
	}

	@KafkaListener(topics = "${topics.payment.events}", groupId = "order-service-group")
	public void paymentResult(@Header("x-event-type") String type,
		@Header(KafkaHeaders.RECEIVED_KEY) String key,
		String body,
		Acknowledgment ack) throws Exception {
		try {
			if (!"payment.result".equals(type)) {
				log.warn("[order] Unknown payment event type={}, key={}", type, key);
				ack.acknowledge();
				return;
			}
			var json = om.readTree(body);
			UUID orderId = UUID.fromString(json.get("orderId").asText());
			String result = json.get("status").asText();

			PaymentStatus status = "DONE".equals(result) ? PaymentStatus.PAID : PaymentStatus.FAILED;

			OffsetDateTime changedAt = orderAuditRepository.findById(orderId)
				.map(audit -> audit.getUpdatedAt())
				.orElse(OffsetDateTime.now(ZoneOffset.UTC));

			Map<String, Object> envelope = new HashMap<>();
			envelope.put("orderId", orderId);
			envelope.put("status", status);
			envelope.put("occurredAt", Instant.now());
			envelope.put("changedAt", changedAt.toInstant());

			publisher.paymentStatusChanged(orderId.toString(), envelope);
			log.info(
				"[order] payment.result consumed => payment.status.changed published. orderId={}, status={}",
				orderId, status);

		} catch (Exception e) {
			log.error("[order] Error while processing payment.result, type={}, key={}, body={}", type, key, body, e);
			throw e;
		}
	}

	@KafkaListener(topics = "${topics.stock.events}", groupId = "order-service-group")
	public void stockEvents(@Header("x-event-type") String type,
		@Header(KafkaHeaders.RECEIVED_KEY) String key,
		String body,
		Acknowledgment ack) throws Exception {
		try {
			switch (type) {
				case "stock.restored" -> {
					JsonNode json = om.readTree(body);
					String orderId = json.get("orderId").asText();
					UUID eventId = UUID.fromString(json.get("eventId").asText());

					log.info("[Order] stock.restored 수신 → 재고 복원 완료. orderId={}, eventId={}", orderId, eventId);

					OrderEntity order = orderRepository.findById(UUID.fromString(orderId))
						.orElseThrow(() -> new BusinessException(CommonCode.ORDER_NOT_FOUND));

					Map<String, Object> envelope = new HashMap<>();
					envelope.put("eventId", UUID.randomUUID().toString());
					envelope.put("orderId", orderId);
					envelope.put("customerId", order.getCustomerId());
					envelope.put("occurredAt", Instant.now());

					publisher.paymentCancelRequested(orderId, envelope);

					log.info("[Order] payment.cancel.result 발행 완료. orderId={}, eventId={}", orderId, eventId);
					ack.acknowledge();
				}

				default -> {
					log.warn("[Order] 알 수 없는 stock 이벤트 type={}, key={}", type, key);
					ack.acknowledge();
				}
			}
		} catch (Exception e) {
			log.error("[Order] stock 이벤트 처리 실패, type={}, key={}, body={}", type, key, body, e);
			throw e;
		}
	}

	@KafkaListener(topics = "${topics.stock.events}", groupId = "order-service-group")
	public void stockResult(@Header("x-event-type") String type,
		@Header(KafkaHeaders.RECEIVED_KEY) String key,
		String body,
		Acknowledgment ack) throws Exception {

		try {
			switch (type) {
				case "stock.finalized" -> {
					JsonNode json = om.readTree(body);
					String orderId = json.get("orderId").asText();
					log.info("[Order] stock.finalized 수신 - orderId={}", orderId);

					ack.acknowledge();
				}

				case "stock.shortage" -> {
					JsonNode json = om.readTree(body);
					String orderId = json.get("orderId").asText();

					orderService.cancelDueToOutOfStock(UUID.fromString(orderId));

					log.info("[Order] stock.shortage 처리 — orderId={} -> CANCELLED(OUT_OF_STOCK) & order.status.changed 발행", orderId);
					ack.acknowledge();
				}
				default -> {
					log.warn("[Order] 알 수 없는 stock 이벤트 type={}, key={}", type, key);
					ack.acknowledge();
				}
			}
		} catch (Exception e) {
			log.error("[Order] stock 이벤트 처리 실패, type={}, key={}, body={}", type, key, body, e);
			throw e;
		}
	}

	@KafkaListener(topics = "${topics.payment.events}", groupId = "order-service-group")
	public void onPaymentCancelResult(
		@Header("x-event-type") String type,
		@Header(KafkaHeaders.RECEIVED_KEY) String key,
		String body,
		Acknowledgment ack
	) throws Exception {
		try {
			if (!"payment.cancel.result".equals(type)) {
				ack.acknowledge();
				return;
			}
			var json = om.readTree(body);
			UUID orderId    = UUID.fromString(json.get("orderId").asText());
			String statusRaw = json.path("status").asText("");

			OrderEntity order = orderRepository.findById(orderId)
				.orElseThrow(() -> new BusinessException(CommonCode.ORDER_NOT_FOUND));

			boolean success = statusRaw.toUpperCase().contains("SUCCESS");

			if (success) {
				order.setPaymentStatus(PaymentStatus.REFUNDED);
				orderRepository.save(order);

				log.info("[Order] payment.cancel.result 성공 → order CANCELED, payment REFUNDED. orderId={}", orderId);
			} else {
				// 결제 취소 실패 → 되돌림
				order.setPaymentStatus(PaymentStatus.PAID);
				orderRepository.save(order);
				log.warn("[Order] payment.cancel.result 실패 → paymentStatus 되돌림(PAID). orderId={}, statusRaw={}", orderId, statusRaw);
			}

			ack.acknowledge();
		} catch (Exception e) {
			log.error("[Order] payment.cancel.result 처리 실패. key={}, body={}", key, body, e);
			throw e;
		}
	}
}
