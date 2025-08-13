package com.example.common;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;

import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass
public abstract class AuditBaseEntity {

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private UUID createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "updated_by_type")
    private Role updatedByType;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private UUID deletedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "deleted_by_type")
    private Role deletedByType;

    @Column(name = "deleted_rs")
    private String deletedReason;
}
