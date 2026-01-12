package com.example.msaorderservice.cart.service;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CartCacheInvalidateListener {
	private final CartCacheVersionService cartCacheVersionService;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onCartChanged(CartChangedEvent e){
		cartCacheVersionService.bupVersion(e.customerId());
	}

	public static record CartChangedEvent(UUID customerId) {}
}
