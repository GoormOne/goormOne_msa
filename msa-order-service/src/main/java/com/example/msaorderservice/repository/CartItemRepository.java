package com.example.msaorderservice.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.msaorderservice.entity.CartEntity;
import com.example.msaorderservice.entity.CartItemEntity;

public interface CartItemRepository extends JpaRepository<CartItemEntity, UUID> {
	Optional<CartItemEntity> findByCartIdAndMenuId(UUID cartId, UUID menuId);

	boolean existsByCartIdAndMenuId(UUID cartId, UUID menuId);
}
