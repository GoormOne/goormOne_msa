package com.example.userservice.repository;

import com.example.userservice.entity.CustomerAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserAuditRepository extends JpaRepository<CustomerAudit, UUID> {}
