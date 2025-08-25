package com.example.storeservice.repository;

import com.example.storeservice.dto.AiFlatRow;
import com.example.storeservice.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoreRepository extends JpaRepository<Store, UUID>, CustomStoreRepository {

    Optional<Store> findByStoreIdAndIsDeletedFalse(UUID storeId);

    List<AiFlatRow> findFlatRows(Collection<UUID> storeIds);

    List<AiFlatRow> findFlatRowsPage(int page, int size);


}
