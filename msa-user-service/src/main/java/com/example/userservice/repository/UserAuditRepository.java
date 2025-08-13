package com.example.userservice.repository;

import com.example.userservice.entity.UserAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserAuditRepository extends JpaRepository<UserAudit, UUID> {}
