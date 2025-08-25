package com.example.userservice.repository;

import com.example.userservice.entity.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, UUID> {
    List<CustomerAddress> findByCustomerId(UUID customerId);
    Optional<CustomerAddress> findByCustomerIdAndIsDefaultTrue(UUID customerId);
    boolean existsByCustomerIdAndIsDefaultTrue(UUID customerId);
}
