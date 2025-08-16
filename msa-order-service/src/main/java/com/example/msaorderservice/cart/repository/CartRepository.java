package com.example.msaorderservice.cart.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.msaorderservice.cart.entity.CartEntity;

public interface CartRepository extends JpaRepository<CartEntity, UUID> {

	Optional<CartEntity> findByUserIdAndStoreId(UUID userId, UUID storeId);

	Optional<CartEntity> findFirstByUserId(UUID userId);

	boolean existsByCartIdAndUserId(UUID cartId, UUID userId);
}
