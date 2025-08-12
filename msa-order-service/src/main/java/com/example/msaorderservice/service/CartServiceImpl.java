package com.example.msaorderservice.service;

import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.msaorderservice.dto.CartItemAddReq;
import com.example.msaorderservice.entity.CartEntity;
import com.example.msaorderservice.entity.CartItemEntity;
import com.example.msaorderservice.repository.CartItemRepository;
import com.example.msaorderservice.repository.CartRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

	private final CartRepository cartRepository;
	private final CartItemRepository cartItemRepository;

	@Override
	@Transactional
	public CartItemEntity addItem(CartItemAddReq req) {
		CartEntity cart = cartRepository.findByUserIdAndStoreId(req.getUserId(), req.getStoreId())
			.orElse(null);

		if (cart == null) {
			cart = cartRepository.save(
				CartEntity.builder()
					.userId(req.getUserId())
					.storeId(req.getStoreId())
					.build()
			);
		}

		cartItemRepository.findByCartIdAndMenuId(cart.getCartId(), req.getMenuId())
			.ifPresent(it -> {throw new IllegalStateException("이미 장바구니에 담긴 메뉴입니다.");});

		CartItemEntity item = CartItemEntity.builder()
			.cartId(cart.getCartId())
			.menuId(req.getMenuId())
			.quantity(req.getQuantity())
			.build();

		return cartItemRepository.save(item);
	}

}
