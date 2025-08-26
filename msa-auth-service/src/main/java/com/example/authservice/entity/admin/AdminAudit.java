package com.example.authservice.entity.admin;

import com.example.common.entity.AuditBaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "p_admin_audit")
public class AdminAudit extends AuditBaseEntity {

    @Id
    @Column(name = "audit_id", columnDefinition = "uuid")
    private UUID auditId;

    @OneToOne(optional = false)
    @MapsId
    @JoinColumn(name = "audit_id", referencedColumnName = "admin_id")
    private Admin admin;
}

