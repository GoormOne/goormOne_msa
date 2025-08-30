package com.example.storeservice.repository;

import com.example.storeservice.dto.ReviewQueryFlatRow;
import com.example.storeservice.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoreRepository extends JpaRepository<Store, UUID>, CustomStoreRepository {

    Optional<Store> findByStoreIdAndIsDeletedFalse(UUID storeId);

    List<UUID> findFlatRowsPage(int page, int size);

    List<ReviewQueryFlatRow> findQueryFlatRows(int page, int size);

}
