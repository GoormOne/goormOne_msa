package com.example.msaorderservice.order.repository;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.msaorderservice.order.entity.OrderAuditEntity;

public interface OrderAuditRepository extends JpaRepository<OrderAuditEntity, UUID> {

	Optional<OrderAuditEntity> findByAuditId(UUID auditId);

	List<OrderAuditEntity> findByAuditIdIn(Collection<UUID> ids);
}
