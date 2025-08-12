package com.example.storeservice.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_store_audit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreAudit {

    @Id
    @Column(name = "audit_id", columnDefinition = "UUID", updatable = false, nullable = false)
    private UUID auditId;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT now()")
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false, length = 25)
    private String createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 25)
    private String updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by", length = 25)
    private String deletedBy;

    @Column(name = "deleted_rs", length = 255)
    private String deletedReason;
}