package com.example.storeservice.controller;

import com.example.common.dto.ApiResponse;
import com.example.storeservice.dto.CreateMenuDto;
import com.example.storeservice.dto.MenuDto;
import com.example.storeservice.entity.Menu;
import com.example.storeservice.entity.StoreAudit;
import com.example.storeservice.global.interceptor.RequireStoreOwner;
import com.example.storeservice.service.AwsS3Service;
import com.example.storeservice.service.MenuService;
import com.example.storeservice.service.StoreAuditService;
import jakarta.validation.Valid;
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

//공통 로직 : 1.상점과 주인 일치 확인 2.메뉴와 상점 일치 확인
@RestController
@RequestMapping("/stores/{storeId}/menu")
@Slf4j
@RequiredArgsConstructor
public class MenuController {
    private final MenuService menuService;
    private final AwsS3Service awsS3Service;
    private final StoreAuditService storeAuditService;

    @GetMapping("/{menuId}")
    public ResponseEntity<ApiResponse<?>> getMenu(
            @PathVariable UUID menuId,
            @PathVariable UUID storeId){

        Menu menu = menuService.getMenu(menuId,storeId);

        return ResponseEntity.ok(ApiResponse.success(MenuDto.from(menu)));
    }

    // 스토어별 메뉴만 반환
    @GetMapping("/")
    public ResponseEntity<ApiResponse<?>> getMenuByStore(
            @PathVariable UUID storeId
    ){

        List<Menu> menuList = menuService.getMenuList(storeId);

        List<MenuDto> menuDtoList = menuList.stream()
                .map(menu -> {
                    String url = String.valueOf(awsS3Service.getImageUrl(menu.getMenuPhotoUrl(), Duration.ofMinutes(30)));
                    return MenuDto.from(menu, url);
                })
                .toList();
        return ResponseEntity.ok(ApiResponse.success(menuDtoList));
    }

    //TODO -- 메뉴 + 카테고리 반환 필요
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<?>> getAllMenu(
            @PathVariable UUID storeId
    ){

        return ResponseEntity.ok(ApiResponse.success());
    }


    //todo - file과 dto 단순 단건 매핑로직 -> map으로 dtoList 고려
    @PostMapping(value = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequireStoreOwner
    public ResponseEntity<ApiResponse<?>> postMenu(
            @PathVariable UUID storeId,
            @RequestPart(value = "photo", required = false) MultipartFile file,
            @Valid @RequestPart("menuDto") CreateMenuDto menuDto){

        //TODO -- 시큐리티컨텍스트에서 파싱
        UUID ownerId = UUID.fromString("ac1adfde-f740-4c27-8c9a-2f0acfc4a0f4");

        Menu menu = CreateMenuDto.toEntity(menuDto,storeId);

        if (file != null){
            String fileName = awsS3Service.uploadFile(file);
            menu.setMenuPhotoUrl(fileName);
        }

        UUID pk = UUID.randomUUID();
        storeAuditService.insertStoreAudit(new StoreAudit(ownerId,pk));
        menu.setMenuId(pk);

        Menu result = menuService.insertMenu(menu);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(result.getMenuId()));
    }

    @DeleteMapping("/{menuId}")
    @RequireStoreOwner
    public ResponseEntity<ApiResponse<?>> deleteMenu(
            @PathVariable UUID menuId,
            @PathVariable UUID storeId
    ){

        // todo - 요청자 ownerId 파싱
        String ownerId = "a23b2047-a11e-4ec4-a16b-e82a5ff70636";
        UUID ownerUUID = UUID.fromString(ownerId);

        // service에서 메뉴와 상점 일치 여부 확인함
        Menu menu = menuService.deleteMenu(menuId, storeId);

        StoreAudit storeAudit = storeAuditService.deleteAudit(menu.getMenuId(), ownerUUID);

        return ResponseEntity.ok(ApiResponse.success(storeAudit.getAuditId()));
    }


    @PutMapping(path = "/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequireStoreOwner
    public ResponseEntity<ApiResponse<?>> updateMenuPhoto(
            @PathVariable UUID storeId,
            @Valid @RequestBody MenuDto menuDto,
            @RequestPart("file") MultipartFile file
            ){
        String ownerId = "a23b2047-a11e-4ec4-a16b-e82a5ff70636";
        UUID ownerUUID = UUID.fromString(ownerId);

        Menu menu = menuService.getMenu(menuDto.getMenuId(), storeId);

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
        storeAuditService.updateAudit(menu.getMenuId(), ownerUUID);

        return ResponseEntity.ok(ApiResponse.success(resultDto));
    }



}
