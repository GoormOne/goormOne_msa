package com.example.storeservice.controller;


import com.example.common.dto.ApiResponse;
import com.example.storeservice.dto.MenuCategoryDto;
import com.example.storeservice.entity.MenuCategory;
import com.example.storeservice.entity.StoreAudit;
import com.example.storeservice.global.interceptor.RequireStoreOwner;
import com.example.storeservice.service.MenuCategoryService;
import com.example.storeservice.service.MenuService;
import com.example.storeservice.service.StoreAuditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/stores/{storeId}/menuCategory")
@RequiredArgsConstructor
public class MenuCategoryController {
    private final MenuCategoryService menuCategoryService;
    private final MenuService menuService;
    private final StoreAuditService storeAuditService;

    //스토어에 어떤 메뉴카테고리들이 있는지
    @GetMapping("/")
    public ResponseEntity<ApiResponse<?>> getMenuCategoryByStore(@PathVariable("storeId") UUID storeId) {

        List<MenuCategory> menuCategories = menuCategoryService.getMenuCategoryByStoreId(storeId);
        List<MenuCategoryDto> menuCategoryDtoList = new ArrayList<>();
        menuCategories.forEach(menuCategory -> {
            menuCategoryDtoList.add(new MenuCategoryDto(menuCategory));
        });

        return ResponseEntity.ok(ApiResponse.success(menuCategoryDtoList));
    }

    //특정 스토어에 특정 카테고리
    @GetMapping("/{menuCategoryId}")
    public ResponseEntity<ApiResponse<?>> getMenuCategory(
            @PathVariable("storeId") UUID storeId,
            @PathVariable("menuCategoryId") UUID menuCategoryId
    ){
        MenuCategory menuCategory = menuCategoryService.getMenuCategory(storeId, menuCategoryId);

        return ResponseEntity.ok(ApiResponse.success(new MenuCategoryDto(menuCategory)));
    }

    @PostMapping("/")
    @RequireStoreOwner
    public ResponseEntity<?> addMenuCategory(
            @PathVariable("storeId") UUID storeId,
            @Valid  @RequestBody MenuCategoryDto mCategoryDto) {
        // todo - 요청자 ownerId 파싱
        String ownerId = "a23b2047-a11e-4ec4-a16b-e82a5ff70636";
        UUID ownerUUID = UUID.fromString(ownerId);


        UUID pk = UUID.randomUUID();
        MenuCategory menuCategory = menuCategoryService.insertMenuCategory(
                new MenuCategory(pk, storeId, mCategoryDto.getMenuCategoryName()));

        //store audit 등록
        StoreAudit audit = storeAuditService.insertStoreAudit(new StoreAudit( ownerUUID,pk));

        mCategoryDto.setMenuCategoryId(pk);


        return ResponseEntity.ok(ApiResponse.success(mCategoryDto));
    }

    @DeleteMapping("/{menuCategoryId}")
    @RequireStoreOwner
    public ResponseEntity<?> deleteMenuCategory(
            @PathVariable("menuCategoryId") UUID menuCategoryId,
            @PathVariable("storeId") UUID storeId) {
        // todo - 요청자 ownerId 파싱
        String ownerId = "a23b2047-a11e-4ec4-a16b-e82a5ff70636";
        UUID ownerUUID = UUID.fromString(ownerId);

        //스토어-메뉴카테고리 관계 확인
        menuCategoryService.getMenuCategoryByStoreIdAndMenuCategoryId(menuCategoryId, storeId);

        // todo -- 메뉴 카테고리 삭제 전 카테고리 하위 메뉴가 있는지 확인



        return ResponseEntity.ok(ApiResponse.success());
    }

    //path파라미터는 안전한 타입인 영문,숫자를 추천 -> 한글 입력값은 쿼리 파라미 추천
    @PutMapping("/{menuCategoryId}/name")
    @RequireStoreOwner
    public ResponseEntity<?> updateMenuCategory(
            @PathVariable("menuCategoryId") UUID menuCategoryId,
            @PathVariable("storeId") UUID storeId,
            @RequestParam("value") String name
    ){
        MenuCategory menuCategory = menuCategoryService.updateMenuCategory(menuCategoryId, storeId, name);

        return ResponseEntity.ok(ApiResponse.success(new MenuCategoryDto(menuCategory)));
    }

}
