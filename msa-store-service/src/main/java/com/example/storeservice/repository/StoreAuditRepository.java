package com.example.storeservice.repository;

import com.example.storeservice.entity.StoreAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StoreAuditRepository extends JpaRepository<StoreAudit, UUID> {

    @Query(
            value = "INSERT INTO p_store_audit (created_by) VALUES (:createdBy) RETURNING audit_id",
            nativeQuery = true
    )
    UUID insertAuditReturningId(@Param("createdBy") UUID createdBy);

}
