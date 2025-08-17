package com.example.msaorderservice.order.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.msaorderservice.order.entity.OrderEntity;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {
}
