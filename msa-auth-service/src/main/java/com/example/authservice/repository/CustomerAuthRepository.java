package com.example.authservice.repository;

import com.example.authservice.entity.CustomerAuth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CustomerAuthRepository extends JpaRepository<CustomerAuth, UUID> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
