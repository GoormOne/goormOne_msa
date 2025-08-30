package com.example.authservice.repository;

import com.example.authservice.entity.owner.OwnerAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OwnerAuditRepository extends JpaRepository<OwnerAudit, UUID> { }