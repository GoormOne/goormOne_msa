package com.example.storeservice.controller;

import com.example.common.ApiResponse;
import com.example.storeservice.dto.CreateMenuDto;
import com.example.storeservice.dto.MenuResponseDto;
import com.example.storeservice.entity.Menu;
import com.example.storeservice.repository.MenuRepository;
import com.example.storeservice.service.AwsS3Service;
import com.example.storeservice.service.MenuService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/menu")
@Slf4j
@RequiredArgsConstructor
public class MenuController {
    private final MenuService menuService;
    private final AwsS3Service awsS3Service;


    @GetMapping("/internal/{menuId}")
    public ResponseEntity<ApiResponse<?>> getMenuInternal(@PathVariable String menuId){

        Menu menu = menuService.getMenu(UUID.fromString(menuId));
        if(menu == null){
             throw new EntityNotFoundException("Menu not found");
        }

        return ResponseEntity.ok(ApiResponse.success(MenuResponseDto.from(menu)));
    }

    // 스토어별, 스토어의 메뉴카테고리별 메뉴 리스트 검색
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

        List<MenuResponseDto> menuResponseDtoList = menuList.stream()
                .map(menu -> {
                    String url = String.valueOf(awsS3Service.getImageUrl(menu.getMenuPhotoUrl(), Duration.ofMinutes(30)));
                    return MenuResponseDto.from(menu, url);
                })
                .toList();

        return ResponseEntity.ok(ApiResponse.success(menuResponseDtoList));
    }

    //todo - file과 dto 단순 단건 매핑로직 -> map으로 dtoList 고려
    @PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<?>> postMenu(
            @RequestPart("file") MultipartFile file,
            @RequestBody CreateMenuDto menuDto){
        //todo - ownerId의 스토어와 스토어 id가 다를때, 없을 때, 카테고리가 없을 때 등 예외처리 추가

        Menu menu = CreateMenuDto.toEntity(menuDto);
        String fileName = awsS3Service.uploadFile(file);
        menu.setMenuPhotoUrl(fileName);

        Menu result = menuService.insertMenu(menu);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result.getMenuId()));
    }



}
