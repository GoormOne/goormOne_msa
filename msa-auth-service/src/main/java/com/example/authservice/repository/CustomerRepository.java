package com.example.authservice.repository;

import com.example.authservice.entity.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<Customer> findByUsername(String username); // 내부 resolve용
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByCustomerId(UUID id);
}