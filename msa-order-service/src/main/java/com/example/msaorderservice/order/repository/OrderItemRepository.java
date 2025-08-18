package com.example.msaorderservice.order.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.msaorderservice.order.entity.OrderItemEntity;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, UUID> {
	List<OrderItemEntity> findByOrderId_OrderId(UUID orderId);

	List<OrderItemEntity> findByOrderId_OrderIdIn(Collection<UUID> orderIds);
}
