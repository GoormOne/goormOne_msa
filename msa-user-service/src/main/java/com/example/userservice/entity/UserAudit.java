package com.example.userservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_user_audit")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserAudit {

    @Id
    @Column(name = "audit_id", updatable = false, nullable = false)
    private UUID auditId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "updated_at") private LocalDateTime updatedAt;
    @Column(name = "updated_by") private UUID updatedBy;
    @Column(name = "deleted_at") private LocalDateTime deletedAt;
    @Column(name = "deleted_by") private UUID deletedBy;
    @Column(name = "deleted_rs", length = 255)
    private String deletedReason;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
