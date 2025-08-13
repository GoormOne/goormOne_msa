package com.example.storeservice.controller;

import com.example.common.ApiResponse;
import com.example.common.CommonCode;
import com.example.storeservice.dto.MenuResponseDto;
import com.example.storeservice.entity.Menu;
import com.example.storeservice.service.MenuService;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/menu")
@Slf4j
@RequiredArgsConstructor
public class MenuController {
    private final MenuService menuService;



    @GetMapping("/internal/{menuId}")
    public ResponseEntity<ApiResponse<?>> getMenuInternal(@PathVariable String menuId){

        log.info("ezra ::: MenuController.getMenuInternal");
        Menu menu = menuService.getMenu(UUID.fromString(menuId));
        if(menu == null){
            log.error("Menu not found");
             return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail(CommonCode.STORE_NOT_FOUND));
        }

        return ResponseEntity.ok(ApiResponse.success(MenuResponseDto.from(menu)));
    }
}
