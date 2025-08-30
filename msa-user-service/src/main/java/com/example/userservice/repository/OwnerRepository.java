package com.example.userservice.repository;

import com.example.userservice.entity.Owner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OwnerRepository extends JpaRepository<Owner, UUID> {

    @Query("select o.ownerId from Owner o where o.username = :username")
    Optional<UUID> findIdByUsername(String username);

    @Query("select o.ownerId from Owner o where o.email = :email")
    Optional<UUID> findIdByEmail(String email);
}