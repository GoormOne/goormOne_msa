package com.example.authservice.entity.owner;

import com.example.common.entity.AuditBaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "p_owner_audit")
public class OwnerAudit extends AuditBaseEntity {

    @Id
    @Column(name = "audit_id", columnDefinition = "uuid")
    private UUID auditId; // = owner_id (공유 PK)

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // auditId = owner.ownerId
    @JoinColumn(name = "audit_id")
    private Owner owner;
}