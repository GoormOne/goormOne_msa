package com.example.msapaymentservice.client;

import java.util.Base64;
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

import com.example.msapaymentservice.dto.TossConfirmReq;
import com.example.msapaymentservice.dto.TossConfirmRes;

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

	public TossConfirmRes confirmPayment(String paymentKey, UUID orderId, int amount) {
		String url = baseUrl + "/v1/payments/confirm";

		TossConfirmReq body = new TossConfirmReq(paymentKey, orderId, amount);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		String creds = secretKey + ":";
		String basic = "Basic " + Base64.getEncoder().encodeToString(creds.getBytes(StandardCharsets.UTF_8));
		headers.set(HttpHeaders.AUTHORIZATION, basic);

		headers.set("Idempotency-Key", paymentKey);

		HttpEntity<TossConfirmReq> entity = new HttpEntity<>(body, headers);

		try {
			ResponseEntity<TossConfirmRes> resp =
				rest.exchange(url, HttpMethod.POST, entity, TossConfirmRes.class);

			if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
				throw new IllegalStateException("Toss confirm 응답이 비정상: " + resp.getStatusCode());
			}
			return resp.getBody();

		} catch (HttpStatusCodeException e) {
			log.warn("Toss confirm 실패 status={} body={}", e.getStatusCode(), e.getResponseBodyAsString());
			throw e;
		}
	}
}
