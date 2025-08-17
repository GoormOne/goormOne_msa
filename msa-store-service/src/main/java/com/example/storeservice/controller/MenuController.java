package com.example.storeservice.controller;

import com.example.common.ApiResponse;
import com.example.storeservice.dto.CreateMenuDto;
import com.example.storeservice.dto.MenuDto;
import com.example.storeservice.entity.Menu;
import com.example.storeservice.entity.Store;
import com.example.storeservice.entity.StoreAudit;
import com.example.storeservice.interceptor.RequireStoreOwner;
import com.example.storeservice.service.AwsS3Service;
import com.example.storeservice.service.MenuService;
import com.example.storeservice.service.StoreAuditService;
import com.example.storeservice.service.StoreService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.nio.file.AccessDeniedException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

//공통 로직 : 1.상점과 주인 일치 확인 2.메뉴와 상점 일치 확인
@RestController
@RequestMapping("/menu")
@Slf4j
@RequiredArgsConstructor
public class MenuController {
    private final MenuService menuService;
    private final AwsS3Service awsS3Service;
    private final StoreService storeService;
    private final StoreAuditService storeAuditService;

    @GetMapping("/internal/{menuId}")
    public ResponseEntity<ApiResponse<?>> getMenuInternal(@PathVariable String menuId){

        Menu menu = menuService.getMenu(UUID.fromString(menuId));

        return ResponseEntity.ok(ApiResponse.success(MenuDto.from(menu)));
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

        List<MenuDto> menuDtoList = menuList.stream()
                .map(menu -> {
                    String url = String.valueOf(awsS3Service.getImageUrl(menu.getMenuPhotoUrl(), Duration.ofMinutes(30)));
                    return MenuDto.from(menu, url);
                })
                .toList();

        return ResponseEntity.ok(ApiResponse.success(menuDtoList));
    }

    //todo - file과 dto 단순 단건 매핑로직 -> map으로 dtoList 고려
    @PostMapping(value = "/{storeId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequireStoreOwner
    public ResponseEntity<ApiResponse<?>> postMenu(
            @PathVariable String storeId,
            @RequestPart("file") MultipartFile file,
            @RequestBody CreateMenuDto menuDto){

        Menu menu = CreateMenuDto.toEntity(menuDto,storeId);
        String fileName = awsS3Service.uploadFile(file);
        menu.setMenuPhotoUrl(fileName);

        Menu result = menuService.insertMenu(menu);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result.getMenuId()));
    }

    @DeleteMapping("/{menuId}/store/{storeId}")
    @RequireStoreOwner
    public ResponseEntity<ApiResponse<?>> deleteMenu(
            @PathVariable String menuId,
            @PathVariable String storeId
    ){

        // todo - 요청자 ownerId 파싱
        String ownerId = "a23b2047-a11e-4ec4-a16b-e82a5ff70636";
        UUID ownerUUID = UUID.fromString(ownerId);
        UUID storeUUID =  UUID.fromString(storeId);

        // service에서 메뉴와 상점 일치 여부 확인함
        Menu menu = menuService.getMenu(UUID.fromString(menuId), storeUUID);

        StoreAudit storeAudit = storeAuditService.deleteAudit(menu.getMenuId(), ownerUUID);

        return ResponseEntity.ok(ApiResponse.success(storeAudit.getAuditId()));
    }

    @PutMapping(path = "/{storeId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequireStoreOwner
    public ResponseEntity<ApiResponse<?>> updateMenuPhoto(
            @PathVariable String storeId,
            @RequestBody MenuDto menuDto,
            @RequestPart("file") MultipartFile file
            ){

        UUID storeUUID = UUID.fromString(storeId);

        Menu menu = menuService.getMenu(menuDto.getMenuId(), storeUUID);

        if (!file.isEmpty()){
            String fileName = awsS3Service.uploadFile(file);
            menuDto.setMenuPhotoUrl(fileName);
        }else{
            menuDto.setMenuPhotoUrl(null);
        }
        MenuDto resultDto = menuService.updateMenu(menu.getMenuId(), menuDto);

        if (resultDto.getMenuPhotoUrl() != null){
            URL photoUrl = awsS3Service.getImageUrl(resultDto.getMenuPhotoUrl(), Duration.ofMinutes(10));
            resultDto.setMenuPhotoUrl(photoUrl.toString());
        }

        return ResponseEntity.ok(ApiResponse.success(resultDto));
    }



}
