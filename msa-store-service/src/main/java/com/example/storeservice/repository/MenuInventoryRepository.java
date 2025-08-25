package com.example.storeservice.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.storeservice.entity.MenuInventory;

public interface MenuInventoryRepository extends JpaRepository<MenuInventory, UUID> {
}
