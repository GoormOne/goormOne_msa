package com.example.storeservice.dto;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class CreateMenuCategoryDto {
    private UUID menuCategoryId;
    private String menuCategoryName;
    private UUID menuId;
}
