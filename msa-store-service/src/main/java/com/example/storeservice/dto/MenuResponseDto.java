package com.example.storeservice.dto;

import com.example.storeservice.entity.Menu;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.net.URL;
import java.util.UUID;

@Getter
@Builder
@Setter
public class MenuResponseDto {
    private UUID menuId;
    private String menuName;
    private Integer menuPrice;
    private String menuDescription;
    private Boolean isPublic;
    private String menuPhotoUrl;
    private Boolean isPublicPhoto;

    private UUID storeId;
    private UUID menuCategoryId;

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

    public static MenuResponseDto from(Menu m, String storeUrl) {
        return MenuResponseDto.builder()
                .menuId(m.getMenuId())
                .menuName(m.getMenuName())
                .menuPrice(m.getMenuPrice())
                .menuDescription(m.getMenuDescription())
                .isPublic(m.getIsPublic())
                .menuPhotoUrl(storeUrl)
                .isPublicPhoto(m.getIsPublicPhoto())
                .storeId(m.getStore().getStoreId())
                .menuCategoryId(m.getMenuCategory().getMenuCategoryId())
                .build();
    }
}