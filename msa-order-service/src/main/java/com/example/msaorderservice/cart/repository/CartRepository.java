package com.example.msaorderservice.cart.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.msaorderservice.cart.entity.CartEntity;

public interface CartRepository extends JpaRepository<CartEntity, UUID> {

	Optional<CartEntity> findByCustomerIdAndStoreId(UUID customerId, UUID storeId);

	Optional<CartEntity> findFirstByCustomerId(UUID customerId);

	boolean existsByCartIdAndCustomerId(UUID cartId, UUID customerId);
}
