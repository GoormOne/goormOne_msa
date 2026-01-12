package com.example.msaorderservice.cart.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.MDC;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.common.exception.BusinessException;
import com.example.common.exception.CommonCode;
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
	private final ApplicationEventPublisher publisher;
	private final CartCacheVersionService versionService;

	@Override
	@Transactional
	public CartItemEntity addItem(CartItemAddReq req) {

		CartEntity cart = cartRepository.findByCustomerIdAndStoreId(req.getCustomerId(), req.getStoreId())
			.orElseGet(() -> cartRepository.save(
					CartEntity.builder()
						.customerId(req.getCustomerId())
						.storeId(req.getStoreId())
						.build()
			));

		MDC.put("cartId", String.valueOf(cart.getCartId()));

		cartItemRepository.findByCartIdAndMenuId(cart.getCartId(), req.getMenuId())
			.ifPresent(it -> {
				throw new BusinessException(CommonCode.CART_ALREADY);
			});

		CartItemEntity item = CartItemEntity.builder()
			.cartId(cart.getCartId())
			.menuId(req.getMenuId())
			.quantity(req.getQuantity())
			.build();


		CartItemEntity saved = cartItemRepository.save(item);
		log.info(CommonCode.CART_CREATE.getMessage());

		publisher.publishEvent(new CartCacheInvalidateListener.CartChangedEvent(req.getCustomerId()));

		return saved;
	}

	@Override
	@Cacheable(
		value = "myCartItem",
		key = "#customerId + '::v' + @cartCacheVersionService.getVersion(#customerId) + '::' + #page + '::' + #size",
		unless = "#result == null || #result.items == null || #result.items.isEmpty()"
	)
	@Transactional(readOnly = true)
	public CartItemsPageRes getMyCartItemsPage(UUID customerId, Integer page, Integer size) {
		int p = (page == null || page < 0) ? 0 : page;
		int s = (size == null || size <= 0) ? 10 : Math.min(size, 100);

		CartEntity cart = cartRepository.findFirstByCustomerId(customerId)
			.orElseThrow(() -> new BusinessException(CommonCode.CART_NOT_FOUND));

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

		log.info(CommonCode.CART_SEARCH_COMPLETE.getMessage());

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
			.orElseThrow(() -> new BusinessException(CommonCode.CART_NOT_FOUND));

		cartItemRepository.deleteByCartId(cart.getCartId());

		log.info(CommonCode.CART_ITEM_CLEAR.getMessage());
		publisher.publishEvent(new CartCacheInvalidateListener.CartChangedEvent(customerId));
	}

	@Override
	@Transactional
	public void deleteCartItem(UUID customerId, UUID cartItemId) {
		CartEntity cart = cartRepository.findFirstByCustomerId(customerId)
			.orElseThrow(() -> new BusinessException(CommonCode.CART_NOT_FOUND));

		CartItemEntity item = cartItemRepository.findByCartItemId(cartItemId)
			.orElseThrow(() -> new BusinessException(CommonCode.CART_ITEM_NOT_FOUND));

		if (!item.getCartId().equals(cart.getCartId())) {
			throw new BusinessException(CommonCode.CART_ITEM_ID_FAIL);
		}

		cartItemRepository.delete(item);

		log.info(CommonCode.CART_ITEM_DELETE.getMessage());
		publisher.publishEvent(new CartCacheInvalidateListener.CartChangedEvent(customerId));
	}

	@Override
	@Transactional
	public void deleteCart(UUID customerId) {
		CartEntity cart = cartRepository.findFirstByCustomerId(customerId)
			.orElseThrow(() -> new BusinessException(CommonCode.CART_NOT_FOUND));

		cartItemRepository.deleteByCartId(cart.getCartId());
		cartRepository.delete(cart);

		log.info(CommonCode.CART_DELETE.getMessage());

		publisher.publishEvent(new CartCacheInvalidateListener.CartChangedEvent(customerId));
	}

	@Override
	@Transactional
	public void increaseQuantity(UUID customerId, UUID menuId) {
		CartEntity cart = cartRepository.findFirstByCustomerId(customerId)
			.orElseThrow(() -> new BusinessException(CommonCode.CART_NOT_FOUND));

		CartItemEntity cartItem = cartItemRepository.findByCartIdAndMenuId(cart.getCartId(), menuId)
			.orElseThrow(() -> new BusinessException(CommonCode.CART_ITEM_NOT_FOUND));
		cartItem.setQuantity(cartItem.getQuantity() + 1);
		cartItemRepository.save(cartItem);

		log.info(CommonCode.CART_ITEM_INCREASE.getMessage());

		publisher.publishEvent(new CartCacheInvalidateListener.CartChangedEvent(customerId));
	}

	@Override
	@Transactional
	public void decreaseQuantity(UUID customerId, UUID menuId) {
		CartEntity cart = cartRepository.findFirstByCustomerId(customerId)
			.orElseThrow(() -> new BusinessException(CommonCode.CART_NOT_FOUND));

		CartItemEntity cartItem = cartItemRepository.findByCartIdAndMenuId(cart.getCartId(), menuId)
			.orElseThrow(() -> new BusinessException(CommonCode.CART_ITEM_NOT_FOUND));

		if (cartItem.getQuantity() > 1) {
			cartItem.setQuantity(cartItem.getQuantity() - 1);
			cartItemRepository.save(cartItem);
		} else {
			throw new BusinessException(CommonCode.CART_ITEM_QUANTITY);
		}

		log.info(CommonCode.CART_ITEM_DECREASE.getMessage());

		publisher.publishEvent(new CartCacheInvalidateListener.CartChangedEvent(customerId));
	}
}
