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
    private final MenuInventoryService menuInventoryService;

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
    public Menu deleteMenu(UUID menuId, UUID storeId){
        Menu menu =  getMenu(menuId, storeId);
        menu.setIsDeleted(true);
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
        if (dto.getInfinite() != null) menuInventoryService.setInfinite(menuId, dto.getInfinite());
        if (Boolean.FALSE.equals(dto.getInfinite()) && dto.getNewAvailableQty() != null) {
            menuInventoryService.adjust(menuId, dto.getNewAvailableQty());
        }


        return MenuDto.from(menu);
    }

    public List<Menu> getMenuList(UUID storeId) {
        List<Menu> menuList = menuRepository.findAllByStore_StoreIdAndIsDeletedFalse(storeId);
        if (menuList.isEmpty()) {throw new EntityNotFoundException("상점에 메뉴가 없습니다. " );}
        return menuList;
    }



    @Transactional
    public Menu insertMenu(Menu menu, int initialQty, boolean infinite) {
        Menu saved = menuRepository.save(menu);

        menuInventoryService.initInventory(saved.getMenuId(), initialQty, infinite);

        return saved;
    }
}
