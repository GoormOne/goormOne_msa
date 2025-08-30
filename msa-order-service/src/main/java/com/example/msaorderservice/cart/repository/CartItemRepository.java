package com.example.msaorderservice.cart.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.msaorderservice.cart.entity.CartItemEntity;

public interface CartItemRepository extends JpaRepository<CartItemEntity, UUID> {
	Optional<CartItemEntity> findByCartIdAndMenuId(UUID cartId, UUID menuId);

	boolean existsByCartIdAndMenuId(UUID cartId, UUID menuId);

	Optional<CartItemEntity> findByCartItemId(UUID cartItemId);

	long deleteByCartId(UUID cartId);

	Page<CartItemEntity> findByCartId(UUID cartId, Pageable pageable);
}
