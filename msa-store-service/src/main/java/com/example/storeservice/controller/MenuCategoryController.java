package com.example.storeservice.controller;


import com.example.common.ApiResponse;
import com.example.storeservice.dto.CreateMenuCategoryDto;
import com.example.storeservice.entity.MenuCategory;
import com.example.storeservice.interceptor.RequireStoreOwner;
import com.example.storeservice.service.MenuCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/menuCategory")
@RequiredArgsConstructor
public class MenuCategoryController {
    private final MenuCategoryService menuCategoryService;

    @GetMapping("/{storeId}")
    public ResponseEntity<?> getMenuCategory(@PathVariable("storeId") String storeId) {

        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/{storeId}")
    @RequireStoreOwner
    public ResponseEntity<?> addMenuCategory(
            @PathVariable("storeId") String storeId,
            @RequestBody CreateMenuCategoryDto mCategoryDto) {
        // todo - 요청자 ownerId 파싱
        String ownerId = "a23b2047-a11e-4ec4-a16b-e82a5ff70636";
        UUID ownerUUID = UUID.fromString(ownerId);


        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/{menuId}")
    @RequireStoreOwner
    public ResponseEntity<?> deleteMenuCategory(
            @PathVariable("menuId") String menuId) {
        // todo - 요청자 ownerId 파싱
        String ownerId = "a23b2047-a11e-4ec4-a16b-e82a5ff70636";
        UUID ownerUUID = UUID.fromString(ownerId);


        return ResponseEntity.ok(ApiResponse.success());
    }

    @PutMapping("/{menuId}")
    @RequireStoreOwner
    public ResponseEntity<?> updateMenuCategory(
            @PathVariable String menuId,
            @RequestBody CreateMenuCategoryDto mCategoryDto
    ){

        return ResponseEntity.ok(ApiResponse.success());
    }

}
