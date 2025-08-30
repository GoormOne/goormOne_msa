package com.example.msaorderservice.order.client;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.common.dto.ApiResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentClient {
	private final RestTemplate restTemplate;

	@Value("${payment.url}")
	private String PAYMENT_BASE;

	public void cancelPayment(UUID orderId, UUID customerId, String reason) {
		String url = PAYMENT_BASE + "/internal/payments/{orderId}/cancel?reason={reason}";

		var headers = new org.springframework.http.HttpHeaders();
		headers.set("X-User-Id", customerId.toString());

		var entity = new org.springframework.http.HttpEntity<Void>(headers);

		var typeRef = new org.springframework.core.ParameterizedTypeReference<com.example.common.dto.ApiResponse<Void>>() {};
		var resp = restTemplate.exchange(
			url,
			org.springframework.http.HttpMethod.POST,
			entity,
			typeRef,
			orderId,
			reason == null ? "user cancel" : reason
		);

		if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody()==null || !resp.getBody().isSuccess()) {
			throw new IllegalStateException("결제 취소 실패");
		}
	}
}
