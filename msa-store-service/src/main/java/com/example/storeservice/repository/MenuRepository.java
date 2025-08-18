package com.example.storeservice.repository;

import com.example.storeservice.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MenuRepository extends JpaRepository<Menu, UUID> {

    List<Menu> findByIsPublicTrueAndStore_StoreId(UUID storeId);
    List<Menu> findByIsPublicTrueAndStore_StoreIdAndMenuCategory_MenuCategoryId(UUID storeId, UUID categoryId);

}
