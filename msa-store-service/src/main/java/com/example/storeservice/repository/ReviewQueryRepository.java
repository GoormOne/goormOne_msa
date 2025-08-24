package com.example.storeservice.repository;

import com.example.storeservice.entity.ReviewQuery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReviewQueryRepository extends JpaRepository<ReviewQuery, UUID> {
}
