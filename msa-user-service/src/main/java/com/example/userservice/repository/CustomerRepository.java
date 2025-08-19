package com.example.userservice.repository;

import com.example.userservice.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    boolean existsByEmailAndUserIdNot(String email, UUID userId);
    Optional<Customer> findByUserId(UUID userId);
}
