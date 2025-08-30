package com.example.storeservice.dto;


import com.example.storeservice.entity.MenuCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class MenuCategoryDto {
    private UUID menuCategoryId;
    @NotBlank(message = "카테고리 이름은 필수입니다.")
    private String menuCategoryName;
    private UUID menuId;

    public MenuCategoryDto(MenuCategory menuCategory) {
        this.menuCategoryId = menuCategory.getMenuCategoryId();
        this.menuCategoryName = menuCategory.getMenuCategoryName();
        this.menuId = menuCategory.getMenuCategoryId();
    }


}
