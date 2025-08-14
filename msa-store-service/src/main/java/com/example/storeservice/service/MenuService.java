package com.example.storeservice.service;


import com.example.storeservice.entity.Menu;
import com.example.storeservice.repository.MenuRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuService {
    private final MenuRepository menuRepository;
    public Menu getMenu(UUID uuid) {
        return menuRepository.findById(uuid).orElse(null);
    }

    public List<Menu> getMenuList(UUID storeId) {
        List<Menu> menuList = menuRepository.findByIsPublicTrueAndStore_StoreId(storeId);
        if (menuList.isEmpty()) {throw new EntityNotFoundException("상점에 메뉴가 없습니다. " );}
        return menuList;
    }

    public List<Menu> getMenuList(UUID storeId, UUID categoryId) {
        List<Menu> menuList = menuRepository.findByIsPublicTrueAndStore_StoreIdAndMenuCategory_MenuCategoryId(storeId, categoryId);
        if (menuList.isEmpty()) {throw new EntityNotFoundException("상점에 메뉴가 없습니다. " );}
        return menuList;
    }

}
