package com.example.storeservice.dto;

import com.example.storeservice.entity.Menu;
import com.example.storeservice.entity.MenuCategory;
import com.example.storeservice.entity.Store;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMenuDto {
    private String menuCategoryId;
    private String menuName;
    private String menuDescription;
    private Integer menuPrice;
    private Boolean isPublic;
    private Boolean isPublicPhoto;

    public static Menu toEntity(CreateMenuDto m, String storeId) {

        return Menu.builder()
                .menuName(m.getMenuName())
                .menuPrice(m.getMenuPrice())
                .menuDescription(m.getMenuDescription())
                .isPublic(m.getIsPublic())
                .isPublicPhoto(m.getIsPublicPhoto())
                .store(new Store(UUID.fromString(storeId)))
                .menuCategory(new MenuCategory(UUID.fromString(m.getMenuCategoryId())))
                .build();
    }
}
