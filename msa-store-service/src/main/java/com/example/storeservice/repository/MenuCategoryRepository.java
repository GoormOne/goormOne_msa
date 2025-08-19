package com.example.storeservice.repository;

import com.example.storeservice.entity.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MenuCategoryRepository extends JpaRepository<MenuCategory, UUID> {
    List<MenuCategory> findAllByStore_StoreIdAndIsDeletedFalse(UUID storeId);
    Optional<MenuCategory> findByMenuCategoryIdAndIsDeletedFalse(UUID menuCategoryId);
    Optional<MenuCategory> findByMenuCategoryIdAndStore_StoreIdAndIsDeletedFalse(UUID menuCategoryId, UUID storeStoreId);

}
