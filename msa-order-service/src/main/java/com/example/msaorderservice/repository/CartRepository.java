package com.example.msaorderservice.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.msaorderservice.entity.CartEntity;

public interface CartRepository extends JpaRepository<CartEntity, UUID> {

	Optional<CartEntity> findByUserIdAndStoreId(UUID userId, UUID storeId);
	boolean existsByUserIdAndStoreId(UUID userId, UUID storeId);
}
