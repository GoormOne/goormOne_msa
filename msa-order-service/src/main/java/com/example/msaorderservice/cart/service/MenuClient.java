package com.example.msaorderservice.cart.service;

import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.example.common.ApiResponse;
import com.example.msaorderservice.cart.dto.MenuLookUp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MenuClient {
	private final RestTemplate restTemplate;

	private static final String STORE_BASE = "http://msa-store-service";

	public MenuLookUp getMenuDetail(UUID storeId, UUID menuId) {
		String url = STORE_BASE + "/stores/{storeId}/menu/{menuId}";
		try {
			ParameterizedTypeReference<ApiResponse<MenuLookUp>> typeRef =
				new ParameterizedTypeReference<>() {
				};
			ResponseEntity<ApiResponse<MenuLookUp>> resp =
				restTemplate.exchange(url, HttpMethod.GET, null, typeRef, storeId.toString(), menuId.toString());

			if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
				throw new IllegalStateException("Store response invalid");
			}

			ApiResponse<MenuLookUp> body = resp.getBody();
			if (body.getData() == null) {
				throw new NoSuchElementException("메뉴가 없습니다.");
			}

			return body.getData();
		} catch (HttpClientErrorException.NotFound e) {
			throw new NoSuchElementException("메뉴를 못 찾았습니다: " + menuId);
		} catch (ResourceAccessException e) {
			throw new IllegalStateException("Store service unavailable", e);
		} catch (Exception e) {
			throw e;
		}
	}
}
