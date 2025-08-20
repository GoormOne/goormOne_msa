package com.example.userservice.entity;

import com.example.common.entity.AuditBaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "p_customer_address_audit")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CustomerAddressAudit extends AuditBaseEntity {

    @Id
    @Column(name = "audit_id", updatable = false, nullable = false)
    private UUID auditId;

    @OneToOne
    @JoinColumn(name = "audit_id", insertable = false, updatable = false)
    private CustomerAddress customerAddress;
}
