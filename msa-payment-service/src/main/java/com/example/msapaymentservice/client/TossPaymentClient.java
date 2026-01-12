package com.example.msapaymentservice.client;

import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.nio.charset.StandardCharsets;
import org.springframework.web.client.HttpStatusCodeException;
import com.example.common.exception.CommonCode;
import com.example.msapaymentservice.dto.TossConfirmReq;
import com.example.msapaymentservice.dto.TossPaymentRes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TossPaymentClient {

	private final RestTemplate rest;

	public TossPaymentClient(@Qualifier("externalRestTemplate") RestTemplate rest) {
		this.rest = rest;
	}


	@Value("${toss.api-url}")
	private String baseUrl;

	@Value("${toss.secret-key}")
	private String secretKey;

	public TossPaymentRes confirmPayment(String paymentKey, UUID orderId, int amount) {
		String url = baseUrl + "/v1/payments/confirm";

		TossConfirmReq body = new TossConfirmReq(paymentKey, orderId, amount);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		String creds = secretKey + ":";
		String basic = "Basic " + Base64.getEncoder().encodeToString(creds.getBytes(StandardCharsets.UTF_8));
		headers.set(HttpHeaders.AUTHORIZATION, basic);

		headers.set("Idempotency-Key", paymentKey);

		HttpEntity<TossConfirmReq> entity = new HttpEntity<>(body, headers);

		log.info(CommonCode.PAYMENT_SUCCESS.getMessage());

		try {
			ResponseEntity<TossPaymentRes> resp =
				rest.exchange(url, HttpMethod.POST, entity, TossPaymentRes.class);

			if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
				throw new IllegalStateException("Toss confirm 응답이 비정상: " + resp.getStatusCode());
			}
			return resp.getBody();

		} catch (HttpStatusCodeException e) {
			log.warn("Toss confirm 실패 status={} body={}", e.getStatusCode(), e.getResponseBodyAsString());
			throw e;
		}
	}


	public String cancelPayment(UUID customerId, String paymentKey, String cancelReason) {
		final String url = "https://api.tosspayments.com/v1/payments/" + paymentKey + "/cancel";


		String basic = Base64.getEncoder()
			.encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set(HttpHeaders.AUTHORIZATION, "Basic " + basic);
		headers.set("Idempotency-Key", UUID.randomUUID().toString()); // 멱등키


		Map<String, Object> body = Map.of("cancelReason",
			(cancelReason == null || cancelReason.isBlank()) ? "USER_REQUEST" : cancelReason);

		try {
			ResponseEntity<String> res = rest.exchange(
				url, HttpMethod.POST, new HttpEntity<>(body, headers), String.class);
			return res.getBody();
		} catch (HttpStatusCodeException e) {

			throw new RuntimeException(
				"Toss cancel failed: status=" + e.getStatusCode()
					+ ", body=" + e.getResponseBodyAsString(), e);
		}
	}
}
