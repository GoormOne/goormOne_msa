package com.example.msapaymentservice.repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.common.entity.PaymentStatus;
import com.example.msapaymentservice.entity.PaymentEntity;

public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {
	boolean existsByPaymentKey(String paymentKey);

	Optional<PaymentEntity> findTopByOrderIdAndStatusOrderByApprovedAtDesc(UUID orderId, PaymentStatus status);

	Optional<PaymentEntity> findByOrderId(UUID orderId);

	PaymentEntity findByPaymentKey(String paymentKey);

	Page<PaymentEntity> findByOrderIdIn(Collection<UUID> orderIds, Pageable pageable);
}
