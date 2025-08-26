package com.example.userservice.repository;

import com.example.userservice.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    @Query("select c.customerId from Customer c where c.username = :username")
    Optional<UUID> findIdByUsername(String username);

    @Query("select c.customerId from Customer c where c.email = :email")
    Optional<UUID> findIdByEmail(String email);
}