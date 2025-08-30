package com.example.storeservice.repository;

import com.example.storeservice.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MenuRepository extends JpaRepository<Menu, UUID> {

    List<Menu> findAllByStore_StoreIdAndIsDeletedFalse(UUID storeId);
    Optional<Menu> findByMenuIdAndIsDeletedFalse(UUID menuId);

	UUID menuId(UUID menuId);
}
