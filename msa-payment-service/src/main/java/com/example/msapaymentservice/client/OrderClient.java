package com.example.msapaymentservice.client;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.example.common.dto.PaymentStatusUpdatedReq;
import com.example.common.entity.PaymentStatus;
import com.example.common.dto.OrderCheckoutView;


@Component
public class OrderClient {

	private final @Qualifier("restTemplate") RestTemplate restTemplate;
	public OrderClient(@Qualifier("restTemplate") RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	private static final String STORE_BASE = "http://msa-order-service";

	public OrderCheckoutView getCheckout(UUID orderId, UUID customerId) {
		String url = STORE_BASE + "/internal/orders/" + orderId + "/checkout";

		HttpHeaders headers = new HttpHeaders();
		headers.set("X-User-Id", customerId.toString());
		HttpEntity<Void> entity = new HttpEntity<>(headers);

		var resp = restTemplate.exchange(url, HttpMethod.GET, entity, OrderCheckoutView.class);
		if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
			throw new IllegalStateException("checkout 조회 실패: " + resp.getStatusCode());
		}

		return resp.getBody();
	}

	public void updateOrderStatus(UUID orderId, UUID customerId, PaymentStatus status) {
		updateOrderStatus(orderId, customerId, status, null);
	}

	public void updateOrderStatus(UUID orderId, UUID customerId, PaymentStatus status, String reason) {
		String url = STORE_BASE + "/internal/orders/" + orderId + "/status";

		HttpHeaders headers = new HttpHeaders();

		if (customerId != null) {
			headers.set("X-User-Id", customerId.toString());
		}else {
			headers.set("X-Internal-Call", "msa-payment-service");
		}

		headers.setContentType(MediaType.APPLICATION_JSON);

		PaymentStatusUpdatedReq req = new PaymentStatusUpdatedReq(status, reason);
		HttpEntity<PaymentStatusUpdatedReq> entity = new HttpEntity<>(req, headers);

		restTemplate.exchange(url, HttpMethod.PATCH, entity, Void.class);
	}
}
