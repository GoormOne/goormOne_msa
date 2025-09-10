package com.example.msaorderservice.order.repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.common.entity.PaymentStatus;
import com.example.msaorderservice.order.entity.OrderEntity;
import com.example.msaorderservice.order.entity.OrderStatus;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {
	Page<OrderEntity> findByCustomerId(UUID customerId, Pageable pageable);

	Optional<OrderEntity> findByOrderIdAndCustomerId(UUID orderId, UUID customerId);

	Page<OrderEntity> findByStoreIdIn(Collection<UUID> storeIds, Pageable pageable);

	Optional<OrderEntity> findByOrderIdAndStoreId(UUID orderId, UUID storeId);

	Optional<OrderEntity> findTopByCustomerIdAndPaymentStatus(UUID customerId, PaymentStatus paymentStatus);
}
