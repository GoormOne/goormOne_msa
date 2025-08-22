package com.example.msaorderservice.order.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.common.dto.ApiResponse;
import com.example.msaorderservice.order.dto.StoreLookUp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreClient {
	private final RestTemplate restTemplate;
	@Value("${store.service.url}")
	private String STORE_BASE;

	public StoreLookUp getStoreDetail(UUID storeId) {
		String url = STORE_BASE + "/stores/{storeId}";
		ParameterizedTypeReference<ApiResponse<StoreLookUp>> typeRef = new ParameterizedTypeReference<>() {};
		ResponseEntity<ApiResponse<StoreLookUp>> resp =
			restTemplate.exchange(url, HttpMethod.GET, null, typeRef, storeId.toString());

		log.info("store-service raw response={}", resp);
		log.info("store-service body={}", resp.getBody());

		if (!resp.getStatusCode().is2xxSuccessful() ||
		resp.getBody() == null ||
		resp.getBody().getData() == null) {
			throw new IllegalStateException("Store Response invalid");
		}
		return resp.getBody().getData();
	}
}
