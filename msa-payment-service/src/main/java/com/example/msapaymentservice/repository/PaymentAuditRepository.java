package com.example.msapaymentservice.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.msapaymentservice.entity.PaymentAuditEntity;

public interface PaymentAuditRepository extends JpaRepository<PaymentAuditEntity, UUID> {

	Optional<PaymentAuditEntity> findByAuditId(UUID auditId);

	List<PaymentAuditEntity> findByAuditIdIn(Collection<UUID> ids);
}
