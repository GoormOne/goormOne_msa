package com.example.storeservice.repository;

import com.example.storeservice.entity.StoreAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StoreAuditRepository extends JpaRepository<StoreAudit, UUID> {

}
