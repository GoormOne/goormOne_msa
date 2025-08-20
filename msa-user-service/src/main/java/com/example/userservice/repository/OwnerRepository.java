package com.example.userservice.repository;

import com.example.userservice.entity.Owner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OwnerRepository extends JpaRepository<Owner, UUID> {
    boolean existsByEmailAndOwnerIdNot(String email, UUID ownerId);
    Optional<Owner> findByOwnerId(UUID ownerId);
}