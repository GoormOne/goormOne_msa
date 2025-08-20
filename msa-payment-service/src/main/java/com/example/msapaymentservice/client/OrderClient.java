package com.example.msapaymentservice.client;

import java.util.UUID;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.example.common.entity.PaymentStatus;
import com.example.common.dto.OrderCheckoutView;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderClient {

	private final RestTemplate restTemplate;
	private static final String STORE_BASE = "http://msa-order-service";

	public OrderCheckoutView getCheckout(UUID orderId, UUID customerId) {
		String url = STORE_BASE + "internal/orders" + orderId + "/checkout";
		HttpHeaders headers = new HttpHeaders();
		headers.set("X-User-Id", customerId.toString());
		HttpEntity<Void> entity = new HttpEntity<>(headers);
		return restTemplate.exchange(url, HttpMethod.GET, entity, OrderCheckoutView.class).getBody();
	}

	public void updateOrderStatus(UUID orderId, UUID customerId, PaymentStatus status) {
		String url = STORE_BASE + "internal/orders" + orderId + "/status";

	}
}
