package com.example.storeservice.service;

import com.example.storeservice.dto.MenuDto;
import com.example.storeservice.entity.MenuCategory;
import com.example.storeservice.entity.Store;
import com.example.storeservice.repository.MenuCategoryRepository;
import com.example.storeservice.repository.StoreRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuCategoryService {
    private final MenuCategoryRepository menuCategoryRepository;

    public List<MenuCategory> getMenuCategoryByStoreId(UUID storeId) {
        List<MenuCategory> menuCategories = menuCategoryRepository.findAllByStore_StoreIdAndIsDeletedFalse(storeId);
        if (menuCategories.isEmpty()) {
            throw new EntityNotFoundException("MenuCategory not found");
        }
        return menuCategories;
    }
    public MenuCategory getMenuCategoryByStoreIdAndMenuCategoryId(UUID menuCategoryId, UUID storeId) {
        return menuCategoryRepository.findByMenuCategoryIdAndStore_StoreIdAndIsDeletedFalse(menuCategoryId, storeId)
                .orElseThrow(EntityNotFoundException::new);
    }


    public MenuCategory getMenuCategory(UUID storeId, UUID menuCategoryId) {
        MenuCategory menuCategory = menuCategoryRepository.findByMenuCategoryIdAndIsDeletedFalse(menuCategoryId)
                .orElseThrow(() -> new EntityNotFoundException("MenuCategory not found"));

        if (!menuCategory.getStore().getStoreId().equals(storeId)) {
            try {
                throw new AccessDeniedException("메뉴카테고리가 상점 정보가 일치하지 않습니다.");
            } catch (AccessDeniedException e) {
                throw new RuntimeException(e);
            }
        }
        return menuCategory;
    }


    public MenuCategory insertMenuCategory(MenuCategory menuCategory) {
        return menuCategoryRepository.save(menuCategory);
    }

    @Transactional
    public MenuCategory updateMenuCategory(UUID menuCategoryId, UUID storeId, String name) {
        MenuCategory menuCategory = getMenuCategory(storeId, menuCategoryId);

        menuCategory.setMenuCategoryName(name);

        return menuCategory;
    }
}
