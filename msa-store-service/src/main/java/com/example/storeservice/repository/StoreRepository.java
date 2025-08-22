package com.example.storeservice.repository;

import com.example.storeservice.dto.AiFlatRow;
import com.example.storeservice.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoreRepository extends JpaRepository<Store, UUID> {

    Optional<Store> findByStoreIdAndIsDeletedFalse(UUID storeId);

    @Query("select s.storeId from Store s where s.isDeleted = false order by s.storeId")
    Page<UUID> findStoreIdPage(Pageable pageable);

    Store findByOwnerId(UUID ownerId);
}
