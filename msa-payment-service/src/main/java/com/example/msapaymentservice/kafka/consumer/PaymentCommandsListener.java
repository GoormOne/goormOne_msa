package com.example.msapaymentservice.kafka.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.example.msapaymentservice.client.TossPaymentClient;
import com.example.msapaymentservice.dto.PaymentFailRes;
import com.example.msapaymentservice.dto.PaymentPrepareReq;
import com.example.msapaymentservice.dto.PaymentSuccessRes;
import com.example.msapaymentservice.kafka.producer.PaymentEventsPublisher;
import com.example.msapaymentservice.kafka.service.PaymentOrchestratorService;
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

	@KafkaListener(topics = "${topics.payment.commands}", groupId = "payment-svc-grp")
	public void onPaymentCommand(@Header("x-event-type") String type,
								@Header(KafkaHeaders.RECEIVED_KEY) String key,
								String body,
								Acknowledgment ack) throws Exception {
		try {
			switch (type) {
				case "PaymentPrepareReq" -> {
					PaymentPrepareReq dto = om.readValue(body, PaymentPrepareReq.class);
					try {
						paymentService.handleSuccess(null, dto.getOrderId(), dto.getPaymentKey(), dto.getAmount());

						var ok = new PaymentSuccessRes(dto.getOrderId(), dto.getPaymentKey(), dto.getAmount());
						publisher.paymentSuccess(dto.getOrderId().toString(), ok, dto.getOrderId().toString(), null);
						log.info("[payment] paymentSuccess published. orderId={}", dto.getOrderId());
					} catch (Exception ex) {
						var fail = new PaymentFailRes(dto.getOrderId(), dto.getPaymentKey(), dto.getAmount(), "결제 실패: " + ex.getMessage());
						try {
							publisher.paymentFailed(dto.getOrderId().toString(), fail, dto.getOrderId().toString(), null);
							log.warn("[payment] paymentFail published. orderId={}, reason={}", dto.getOrderId(), ex.getMessage());
						} catch (Exception pubEx) {
							log.error("[payment] Failed to publish PaymentFailed. orderId={}", dto.getOrderId(), pubEx);
						}
						throw ex;
					}
				}
				default -> log.warn("[payment] Unknown command type={}, key={}", type, key);
			}
			ack.acknowledge();
		}catch (Exception e) {
			log.error("[payment] Error processing command. type={}, key={}, body-{}", type, key, body, e);
			throw e;
		}
	}
}
