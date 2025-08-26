package com.example.authservice.repository;

import com.example.authservice.entity.customer.CustomerAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CustomerAuditRepository extends JpaRepository<CustomerAudit, UUID> {}