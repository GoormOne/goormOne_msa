package com.example.storeservice.dto;

import com.example.storeservice.entity.Menu;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Builder
@Setter
public class MenuDto {
    private UUID menuId;
    @NotNull
    private String menuName;
    @NotNull
    @Positive
    private Integer menuPrice;
    private String menuDescription;
    private String menuPhotoUrl;
    private Boolean isPublic = true;
    private Boolean isPublicPhoto = true;

    private UUID storeId;
    private UUID menuCategoryId;

    @Min(0)
    private Integer newAvailableQty;
    private Boolean infinite;

    public static MenuDto from(Menu m) {
        return MenuDto.builder()
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

    public static MenuDto from(Menu m, String storeUrl) {
        return MenuDto.builder()
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
