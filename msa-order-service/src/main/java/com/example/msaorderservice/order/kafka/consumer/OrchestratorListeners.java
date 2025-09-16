package com.example.msaorderservice.order.kafka.consumer;

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
import com.example.msaorderservice.order.kafka.producer.OrderCommandPublisher;
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

	@KafkaListener(topics = "${topics.stock.events}")
	public void stockReservationResult(@Header("x-event-type") String type,
		@Header(KafkaHeaders.RECEIVED_KEY) String key,
		@Header(name = "x-correlation-id", required = false) String corr,
		@Header(name = "x-causation-id", required = false) String causationId,
		String body,
		Acknowledgment ack) throws Exception {

		if (!"stock.reservation".equals(type))
			return;

		try {
			var json = om.readTree(body);
			String orderId = json.get("orderId").asText();

			Map<String, Object> payEnvelope = new HashMap<>();
			payEnvelope.put("orderId", orderId);
			payEnvelope.put("amount", json.get("amount").asInt());

			String correlationId = (corr == null || corr.isBlank())
				? UUID.randomUUID().toString()
				: corr;

			publisher.paymentPrepare(orderId, payEnvelope, correlationId, causationId);

			log.info("[Saga] stock.reservation -> payment.prepare.command 발행. corr={}, orderId={}", corr, orderId);
			ack.acknowledge();
		} catch (Exception e) {
			log.error("[Saga] stock.reservation 처리 실패, key={}, body={}", key, body, e);
			throw e;
		}
	}

	@KafkaListener(topics = "${topics.payment.events}", groupId = "order-service-group")
	public void paymentResult(@Header("x-event-type") String type,
		@Header(KafkaHeaders.RECEIVED_KEY) String key,
		@Header(name = "x-correlation-id", required = false) String corr,
		@Header(name = "x-causation-id", required = false) String causationId,
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

			PaymentStatus status = "SUCCESS".equals(result) ? PaymentStatus.PAID : PaymentStatus.FAILED;

			String cor = (corr == null || corr.isBlank())
				? UUID.randomUUID().toString() : corr;

			String cause = (causationId == null || causationId.isBlank())
				? corr : causationId;

			publisher.paymentStatusChanged(orderId.toString(),
				Map.of(
					"orderId", orderId.toString(),
					"status", status.name(),
					"changedAt", OffsetDateTime.now(ZoneOffset.UTC).toString()
				),
				corr,
				cause
			);
			log.info(
				"[order] payment.result consumed => payment.status.changed published. orderId={}, status={}, corr={}, cause={}",
				orderId, status, corr, cause);

		} catch (Exception e) {
			log.error("[order] Error while processing payment.result, type={}, key={}, body={}", type, key, body, e);
			throw e;
		}
	}

	@KafkaListener(topics = "${topics.stock.events}", groupId = "order-service-group")
	public void stockResult(@Header("x-event-type") String type,
		@Header(KafkaHeaders.RECEIVED_KEY) String key,
		@Header(name = "x-correlation-id", required = false) String corr,
		@Header(name = "x-causation-id", required = false) String causationId,
		String body,
		Acknowledgment ack) throws Exception {

		final String correlationId = (corr == null || corr.isBlank() ? UUID.randomUUID().toString() : corr);

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
}
