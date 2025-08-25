package com.example.msaorderservice.order.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.common.dto.ApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MenuInventoryClient {

	private final RestTemplate restTemplate;

	@Value("${menu.inventory.url}")
	private String INVENTORY_BASE;

	public void reserve(UUID menuId, int qty) {
		String url = INVENTORY_BASE + "/internal/inventory/{menuId}/reserve?qty={qty}";
		var typeRef = new ParameterizedTypeReference<ApiResponse<Void>>() {};
		ResponseEntity<ApiResponse<Void>> resp =
			restTemplate.exchange(url, HttpMethod.POST, null, typeRef, menuId.toString(), qty);

		if (!resp.getStatusCode().is2xxSuccessful()
			|| resp.getBody() == null
			|| !resp.getBody().isSuccess()) {
			throw new IllegalStateException("재고 예약 실패");
		}
	}

	public void confirm(UUID menuId, int qty) {
		String url = INVENTORY_BASE + "/internal/inventory/{menuId}/confirm?qty={qty}";
		restTemplate.postForEntity(url, null, Void.class, menuId.toString(), qty);
	}

	public void release(UUID menuId, int qty) {
		String url = INVENTORY_BASE + "/internal/inventory/{menuId}/release?qty={qty}";
		ResponseEntity<Void> resp = restTemplate.postForEntity(url, null, Void.class, menuId.toString(), qty);
	}

}
