package com.example.msaorderservice.service;

import static org.springframework.data.jpa.domain.AbstractPersistable_.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.msaorderservice.dto.CartItemAddReq;
import com.example.msaorderservice.dto.CartItemRes;
import com.example.msaorderservice.dto.CartItemsPageRes;
import com.example.msaorderservice.dto.MenuLookUp;
import com.example.msaorderservice.entity.CartEntity;
import com.example.msaorderservice.entity.CartItemEntity;
import com.example.msaorderservice.repository.CartItemRepository;
import com.example.msaorderservice.repository.CartRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

	private final CartRepository cartRepository;
	private final CartItemRepository cartItemRepository;
	private final MenuClient menuClient;

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

	@Override
	@Transactional
	public CartItemsPageRes getMyCartItemsPage(UUID userId, Integer page, Integer size) {
		int p = (page == null || page < 0) ? 0 : page;
		int s = (size == null || size <= 0) ? 10 : Math.min(size, 100);

		CartEntity cart = cartRepository.findFirstByUserId(userId)
			.orElseThrow(() -> new NoSuchElementException("cart not found for user"));

		Pageable pageable = PageRequest.of(p, s, Sort.by("cartItemId").ascending());
		Page<CartItemEntity> pageResult = cartItemRepository.findByCartId(cart.getCartId(), pageable);

		List<CartItemEntity> pageItems = pageResult.getContent();

		Map<UUID, MenuLookUp> menuMap = pageItems.parallelStream()
			.map(CartItemEntity::getMenuId)
			.distinct()
			.map(id -> {
				try {
					return menuClient.getMenuDetail(id);
				} catch (Exception e) {
					log.error("menu lookup 실패. menuId={}", id, e);
					return null;
				}
			})
			.filter(Objects::nonNull)
			.collect(Collectors.toMap(MenuLookUp::getMenuId, m -> m));

		int pageTotal = 0;
		List<CartItemRes> items = new ArrayList<>();
		for (CartItemEntity ci : pageItems) {
			MenuLookUp m = menuMap.get(ci.getMenuId());
			if (m == null)
				continue;
			int line = m.getMenuPrice() * ci.getQuantity();
			pageTotal += line;

			items.add(CartItemRes.builder()
				.menuId(ci.getMenuId())
				.menuName(m.getMenuName())
				.menuPrice(m.getMenuPrice())
				.quantity(ci.getQuantity())
				.lineTotal(line)
				.build());
		}

		return CartItemsPageRes.builder()
			.storeId(cart.getStoreId())
			.page(p)
			.size(s)
			.totalItems(pageResult.getTotalElements())
			.totalPages(pageResult.getTotalPages())
			.pageTotalPrice(pageTotal)
			.items(items)
			.build();
	}
}
