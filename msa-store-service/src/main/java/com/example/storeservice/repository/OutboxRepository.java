package com.example.storeservice.repository;

import com.example.storeservice.entity.OutboxEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEntity, UUID> {
}
