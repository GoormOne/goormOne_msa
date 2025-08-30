package com.example.storeservice.dto;

import com.example.storeservice.entity.Menu;
import com.example.storeservice.entity.MenuCategory;
import com.example.storeservice.entity.Store;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMenuDto {
    private UUID menuCategoryId;
    @NotNull
    private String menuName;
    @NotNull
    private String menuDescription;
    @NotNull
    @Positive
    private Integer menuPrice;
    private Boolean isPublic = true;
    private Boolean isPublicPhoto = true;

    @NotNull
    @Positive
    private Integer initialQty;
    private Boolean infinite = false;

    public static Menu toEntity(CreateMenuDto m, UUID storeId) {

        return Menu.builder()
                .menuName(m.getMenuName())
                .menuPrice(m.getMenuPrice())
                .menuDescription(m.getMenuDescription())
                .isPublic(m.getIsPublic())
                .isPublicPhoto(m.getIsPublicPhoto())
                .store(new Store(storeId))
                .menuCategory(new MenuCategory(m.getMenuCategoryId()))
                .isDeleted(false)
                .build();
    }
}
