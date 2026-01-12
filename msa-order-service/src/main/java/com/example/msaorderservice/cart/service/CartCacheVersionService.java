package com.example.msaorderservice.cart.service;

import java.util.UUID;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class CartCacheVersionService {

	@Cacheable(value = "myCartItemVer", key = "#customerId")
	public int getVersion(UUID customerId) {
		return 0;
	}

	@CachePut(value = "myCartItemVer", key = "#customerId")
	public int bupVersion(UUID customerId) {
		return (int)(System.nanoTime() & 0x7FFFFFFF);
	}
}
