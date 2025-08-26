package com.example.msapaymentservice.client;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.example.common.entity.PaymentStatus;
import com.example.common.dto.OrderCheckoutView;
import com.example.msapaymentservice.dto.PaymentStatusUpdateReq;

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
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-User-Id", customerId.toString());

		PaymentStatusUpdateReq body = new PaymentStatusUpdateReq();
		body.setPaymentStatus(status);

		HttpEntity<PaymentStatusUpdateReq> entity = new HttpEntity<>(body, headers);
		restTemplate.exchange(url, HttpMethod.PATCH, entity, Void.class);
	}

	public List<UUID> findOrderIdsByCustomer(UUID customerId) {
		String url = STORE_BASE + "/internal/orders/my";

		HttpHeaders headers = new HttpHeaders();
		headers.set("X-User-Id", customerId.toString());
		HttpEntity<Void> entity = new HttpEntity<>(headers);

		ParameterizedTypeReference<List<UUID>> typeRef = new ParameterizedTypeReference<>() {};
		var resp = restTemplate.exchange(url, HttpMethod.GET, entity, typeRef);

		if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
			throw new IllegalStateException("order ids 조회 실패(고객)");
		}
		return resp.getBody();
	}

	public List<UUID> findOrderIdsByStore(UUID ownerId, UUID storeId) {
		String url = STORE_BASE + "/internal/orders/{storeId}/owner";

		HttpHeaders headers = new HttpHeaders();
		headers.set("X-User-Id", ownerId.toString());
		HttpEntity<Void> entity = new HttpEntity<>(headers);

		ParameterizedTypeReference<List<UUID>> typeRef = new ParameterizedTypeReference<>() {
		};
		var resp = restTemplate.exchange(url, HttpMethod.GET, entity, typeRef, storeId.toString());

		if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
			throw new IllegalStateException("order ids 조회 실패(매장)");
		}
		return resp.getBody();
	}
}
