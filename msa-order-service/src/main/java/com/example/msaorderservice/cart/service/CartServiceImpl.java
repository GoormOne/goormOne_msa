package com.example.msaorderservice.cart.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.msaorderservice.cart.dto.CartItemAddReq;
import com.example.msaorderservice.cart.dto.CartItemRes;
import com.example.msaorderservice.cart.dto.CartItemsPageRes;
import com.example.msaorderservice.cart.dto.MenuLookUp;
import com.example.msaorderservice.cart.entity.CartEntity;
import com.example.msaorderservice.cart.entity.CartItemEntity;
import com.example.msaorderservice.cart.repository.CartItemRepository;
import com.example.msaorderservice.cart.repository.CartRepository;

import jakarta.persistence.EntityNotFoundException;
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
		CartEntity cart = cartRepository.findByCustomerIdAndStoreId(req.getCustomerId(), req.getStoreId())
			.orElse(null);

		if (cart == null) {
			cart = cartRepository.save(
				CartEntity.builder()
					.customerId(req.getCustomerId())
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
	@Cacheable(
			value = "myCartItem",
			key = "#customerId + '::' + #page + '::' + #size"
	)
	@Transactional(readOnly = true)
	public CartItemsPageRes getMyCartItemsPage(UUID customerId, Integer page, Integer size) {
		int p = (page == null || page < 0) ? 0 : page;
		int s = (size == null || size <= 0) ? 10 : Math.min(size, 100);

		CartEntity cart = cartRepository.findFirstByCustomerId(customerId)
			.orElseThrow(() -> new NoSuchElementException("cart not found for user"));

		UUID storeId = cart.getStoreId();

		Pageable pageable = PageRequest.of(p, s, Sort.by("cartItemId").ascending());
		Page<CartItemEntity> pageResult = cartItemRepository.findByCartId(cart.getCartId(), pageable);

		List<CartItemEntity> pageItems = pageResult.getContent();

		Map<UUID, MenuLookUp> menuMap = pageItems.parallelStream()
			.map(CartItemEntity::getMenuId)
			.distinct()
			.map(id -> {
				try {
					return menuClient.getMenuDetail(storeId, id);
				} catch (Exception e) {
					log.error("menu lookup 실패. storeId={}, menuId={}", storeId, id, e);
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

	@Override
	@Transactional
	public void clearCartItems(UUID customerId) {
		CartEntity cart = cartRepository.findFirstByCustomerId(customerId)
			.orElseThrow(() -> new EntityNotFoundException("Cart not found for user: " + customerId));

		cartItemRepository.deleteByCartId(cart.getCartId());
	}

	@Override
	@Transactional
	public void deleteCartItem(UUID customerId, UUID cartItemId) {
		CartEntity cart = cartRepository.findFirstByCustomerId(customerId)
			.orElseThrow(() -> new EntityNotFoundException("Cart not found for user: " + customerId));

		CartItemEntity item = cartItemRepository.findByCartItemId(cartItemId)
			.orElseThrow(() -> new EntityNotFoundException("CartItem not found: " + cartItemId));

		if (!item.getCartId().equals(cart.getCartId())) {
			throw new IllegalStateException("CartItem does not belong to user's cart.");
		}

		cartItemRepository.delete(item);
	}

	@Override
	@Transactional
	public void deleteCart(UUID customerId) {
		CartEntity cart = cartRepository.findFirstByCustomerId(customerId)
			.orElseThrow(() -> new EntityNotFoundException("Cart not found for user: " + customerId));

		cartItemRepository.deleteByCartId(cart.getCartId());
		cartRepository.delete(cart);
	}

	@Override
	@Transactional
	public void increaseQuantity(UUID customerId, UUID menuId) {
		CartEntity cart = cartRepository.findFirstByCustomerId(customerId)
			.orElseThrow(() -> new IllegalArgumentException("Cart not found for user"));

		CartItemEntity cartItem = cartItemRepository.findByCartIdAndMenuId(cart.getCartId(), menuId)
			.orElseThrow(() -> new IllegalArgumentException("Cart item not found"));
		cartItem.setQuantity(cartItem.getQuantity() + 1);
		cartItemRepository.save(cartItem);
	}

	@Override
	@Transactional
	public void decreaseQuantity(UUID customerId, UUID menuId) {
		CartEntity cart = cartRepository.findFirstByCustomerId(customerId)
			.orElseThrow(() -> new IllegalArgumentException("Cart not found for user"));

		CartItemEntity cartItem = cartItemRepository.findByCartIdAndMenuId(cart.getCartId(), menuId)
			.orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

		if (cartItem.getQuantity() > 1) {
			cartItem.setQuantity(cartItem.getQuantity() - 1);
			cartItemRepository.save(cartItem);
		} else {
			throw new IllegalArgumentException("Minimum order quantity is 1");
		}
	}
}
