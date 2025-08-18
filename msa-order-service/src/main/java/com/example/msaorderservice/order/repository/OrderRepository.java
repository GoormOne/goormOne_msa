package com.example.msaorderservice.order.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.msaorderservice.order.entity.OrderEntity;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {
	Page<OrderEntity> findByCustomerIdOrderByOrderIdDesc(UUID customerId, Pageable pageable);

	Optional<OrderEntity> findByOrderIdAndCustomerId(UUID orderId, UUID customerId);
}
