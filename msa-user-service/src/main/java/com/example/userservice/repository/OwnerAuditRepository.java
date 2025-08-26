package com.example.userservice.repository;

import com.example.userservice.entity.OwnerAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OwnerAuditRepository extends JpaRepository<OwnerAudit, UUID> { }