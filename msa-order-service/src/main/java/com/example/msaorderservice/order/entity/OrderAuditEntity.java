package com.example.msaorderservice.order.entity;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "p_order_audit")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderAuditEntity {

	@Id
	@Column(name = "audit_id", nullable = false)
	private UUID auditId;

	@OneToOne
	@MapsId
	@JoinColumn(name = "audit_id")
	private OrderEntity orderId;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@Column(name = "created_by", columnDefinition = "uuid", nullable = false)
	private UUID createdBy;

	@Column(name = "updated_at")
	private OffsetDateTime updatedAt;

	@Column(name = "updated_by", columnDefinition = "uuid")
	private UUID updatedBy;

	@Column(name = "deleted_at")
	private OffsetDateTime deletedAt;

	@Column(name = "deleted_by", columnDefinition = "uuid")
	private UUID deletedBy;

	@Column(name = "deleted_rs")
	private String deletedRs;
}
