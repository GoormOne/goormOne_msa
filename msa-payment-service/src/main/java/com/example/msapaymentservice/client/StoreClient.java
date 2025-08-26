package com.example.msapaymentservice.client;

import static com.example.msapaymentservice.client.OrderClient.*;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.common.dto.ApiResponse;
import com.example.msapaymentservice.dto.StoreClientRes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreClient {

	private final RestTemplate restTemplate;

	@Value("${store.url}")
	private String storeBase;

	public StoreClientRes getStoreDetail(UUID storeId) {
		String url = storeBase + "/stores/{storeId}";

		HttpHeaders headers = new HttpHeaders();
		headers.set("X-Internal-Call", "msa-payment-service");
		HttpEntity<Void> entity = new HttpEntity<>(headers);

		var typeRef = new ParameterizedTypeReference<ApiResponse<StoreClientRes>>() {};
		var resp = restTemplate.exchange(url, HttpMethod.GET, entity, typeRef, storeId);

		log.info("store-service raw response={}", resp);
		ApiResponse<StoreClientRes> body = resp.getBody();
		if (body == null || body.getData() == null) {
			throw new IllegalStateException("store detail 응답이 비어있음");
		}
		return body.getData();
	}
}
