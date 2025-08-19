package com.example.storeservice.service;


import com.example.storeservice.dto.MenuDto;
import com.example.storeservice.entity.Menu;
import com.example.storeservice.repository.MenuRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuService {
    private final MenuRepository menuRepository;

    public Menu getMenu(UUID menuId, UUID storeId){
        Menu menu =  menuRepository.findByMenuIdAndIsDeletedFalse(menuId).orElseThrow(()-> new EntityNotFoundException("없는 메뉴입니다."));
        if (!menu.getStore().getStoreId().equals(storeId)){
            try {
                throw new AccessDeniedException("메뉴와 상점 정보가 일치하지 않습니다.");
            } catch (AccessDeniedException e) {
                throw new RuntimeException(e);
            }
        }
        return menu;
    }

    @Transactional
    public MenuDto updateMenu(UUID menuId, MenuDto dto){
        Menu menu = menuRepository.findByMenuIdAndIsDeletedFalse(menuId)
                .orElseThrow(() -> new IllegalArgumentException("menu not found: " + menuId));

        if (dto.getMenuName() != null) menu.setMenuName(dto.getMenuName());
        if (dto.getMenuPrice() != null) menu.setMenuPrice(dto.getMenuPrice());
        if (dto.getMenuDescription() != null) menu.setMenuDescription(dto.getMenuDescription());
        if (dto.getIsPublic() != null) menu.setIsPublic(dto.getIsPublic());
        if (dto.getIsPublicPhoto() != null) menu.setIsPublicPhoto(dto.getIsPublicPhoto());
        if (dto.getMenuPhotoUrl() != null) menu.setMenuPhotoUrl(dto.getMenuPhotoUrl());

        //todo -- 카테고리 ID 검증 후에 변경 포토 변경 로직 추가
//        if (dto.getMenuCategoryId() != null) {
//            var categoryRef = menuCategoryRepository.getReferenceById(dto.getMenuCategoryId());
//            menu.changeCategory(categoryRef);
//        }

        return MenuDto.from(menu);
    }

    public List<Menu> getMenuList(UUID storeId) {
        List<Menu> menuList = menuRepository.findAllByStore_StoreIdAndIsDeletedFalse(storeId);
        if (menuList.isEmpty()) {throw new EntityNotFoundException("상점에 메뉴가 없습니다. " );}
        return menuList;
    }

    public Menu getMenuList(UUID storeId, UUID menuId) {
        Menu menu = menuRepository.findByMenuIdAndIsDeletedFalse(menuId)
                .orElseThrow(() -> new EntityNotFoundException("메뉴를 찾을 수 없습니다."));

        if (!menu.getStore().getStoreId().equals(storeId)) {
            try {
                throw new AccessDeniedException("메뉴가 상점 정보가 일치하지 않습니다.");
            } catch (AccessDeniedException e) {
                throw new RuntimeException(e);
            }
        }
        return menu;
    }

    public Menu insertMenu(Menu menu) {
        return menuRepository.save(menu);
    }
}
