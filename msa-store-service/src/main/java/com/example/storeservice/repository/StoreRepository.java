package com.example.storeservice.repository;

import com.example.storeservice.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoreRepository extends JpaRepository<Store, UUID> {

    Optional<Store> findByStoreIdAndIsDeletedFalse(UUID storeId);
    Store findByOwnerId(UUID ownerId);
}
