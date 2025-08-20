package com.example.authservice.repository;

import com.example.authservice.entity.CustomerAuth;
import com.example.authservice.entity.OwnerAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OwnerAuthRepository extends JpaRepository<OwnerAuth, UUID> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
