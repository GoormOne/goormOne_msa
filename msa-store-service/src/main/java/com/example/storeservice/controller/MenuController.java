package com.example.storeservice.controller;

import com.example.common.dto.ApiResponse;
import com.example.common.exception.CommonCode;
import com.example.storeservice.dto.MenuResponseDto;
import com.example.storeservice.entity.Menu;
import com.example.storeservice.entity.Store;
import com.example.storeservice.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/menu")
@Slf4j
@RequiredArgsConstructor
public class MenuController {
    private final MenuService menuService;


    //todo S3 사용 -- 이미지 파일 받아서 S3저장 후 URL 받아서 저장

    @GetMapping("/internal/{menuId}")
    public ResponseEntity<ApiResponse<?>> getMenuInternal(@PathVariable String menuId){

        Menu menu = menuService.getMenu(UUID.fromString(menuId));
        if(menu == null){
            log.error("Menu not found");
             return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail(CommonCode.STORE_NOT_FOUND));
        }

        return ResponseEntity.ok(ApiResponse.success(MenuResponseDto.from(menu)));
    }

    @GetMapping("/{storeId}/menus")
    public ResponseEntity<ApiResponse<?>> getMenu(
            @PathVariable String storeId,
            @RequestParam(required = false) String categoryId
    ){

        List<Menu> menuList;
        if (categoryId == null){
            menuList = menuService.getMenuList(UUID.fromString(storeId));
        }else {
            menuList = menuService.getMenuList(UUID.fromString(storeId), UUID.fromString(categoryId));
        }

        List<MenuResponseDto> menuResponseDtoList = menuList
                .stream()
                .map(MenuResponseDto::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(menuResponseDtoList));
    }



}
