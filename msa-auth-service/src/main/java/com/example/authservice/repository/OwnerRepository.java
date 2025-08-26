package com.example.authservice.repository;

import com.example.authservice.entity.owner.Owner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OwnerRepository extends JpaRepository<Owner, UUID> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<Owner> findByUsername(String username);
    Optional<Owner> findByEmail(String email);
    Optional<Owner> findByOwnerId(UUID id);
}