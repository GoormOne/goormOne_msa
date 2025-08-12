package com.example.storeservice.dto;

import com.example.storeservice.entity.Menu;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class MenuResponseDto {
    private UUID menuId;
    private String menuName;
    private Integer menuPrice;
    private String menuDescription;
    private Boolean isPublic;
    private String menuPhotoUrl;
    private Boolean isPublicPhoto;

    private UUID storeId;          // 연관 id만
    private UUID menuCategoryId;   // 연관 id만

    public static MenuResponseDto from(Menu m) {
        return MenuResponseDto.builder()
                .menuId(m.getMenuId())
                .menuName(m.getMenuName())
                .menuPrice(m.getMenuPrice())
                .menuDescription(m.getMenuDescription())
                .isPublic(m.getIsPublic())
                .menuPhotoUrl(m.getMenuPhotoUrl())
                .isPublicPhoto(m.getIsPublicPhoto())
                .storeId(m.getStore().getStoreId())
                .menuCategoryId(m.getMenuCategory().getMenuCategoryId())
                .build();
    }
}