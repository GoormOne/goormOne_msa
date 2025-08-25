package com.example.authservice.entity.customer;

import com.example.common.entity.AuditBaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "p_customer_audit")
public class CustomerAudit extends AuditBaseEntity {

    @Id
    @Column(name = "audit_id", columnDefinition = "uuid")
    private UUID auditId; // = customer_id (공유 PK)

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // auditId = customer.customerId
    @JoinColumn(name = "audit_id")
    private Customer customer;
}