package com.example.storeservice.controller;


import com.example.common.ApiResponse;
import com.example.common.CommonCode;
import com.example.storeservice.dto.StoreRegisterDto;
import com.example.storeservice.entity.StoreAudit;
import com.example.storeservice.service.StoreAuditService;
import com.example.storeservice.service.StoreService;
import com.example.storeservice.entity.Store;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
@Slf4j
public class StoreController {
    private final StoreService storeService;
    private final StoreAuditService storeAuditService;


    // 스토어 상세조회 todo - 메뉴카테고리, 메뉴, 리전, 오너까지 반환 추가
    @GetMapping("/{storeId}")
    public ResponseEntity<ApiResponse<?>> getStore(@PathVariable String storeId) {
        Store store = storeService.getStore(UUID.fromString(storeId))
                .orElse(null);

        if (store == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.fail(CommonCode.STORE_NOT_FOUND));
        }


        StoreAudit storeAudit = storeAuditService.getAudit(store.getStoreId());
        if (storeAudit.getDeletedAt() != null) {
            return ResponseEntity
                    .status(HttpStatus.GONE)
                    .body(ApiResponse.fail(CommonCode.STORE_DELETED));
        }


        return ResponseEntity.ok(ApiResponse.success(store));


    }

    @PostMapping()
    public ResponseEntity<ApiResponse<?>> saveStore(
            @RequestBody StoreRegisterDto storeRegisterDto
    ) {
        // todo - JWT 파싱 오너 ID와 post owerId와 같은지 확인
//        String owerId = null;
//        if(owerId != storeRegisterDto.getOwerId()){}

        //audit생성 후 PK 받아오기
        UUID storePk = storeAuditService.insertAudit(UUID.fromString(storeRegisterDto.getOwerId()));
        if (storePk == null) {
            return ResponseEntity.notFound().build();
        }

        //store 저장
        Store store = storeService.insertStore(storeRegisterDto.toEntity(storePk));
        if (store == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(ApiResponse.success(store.getStoreId()));
    }

    @DeleteMapping("/{storeId}")
    public ResponseEntity<ApiResponse<?>> deleteStore(
            @PathVariable("storeId") String storeId) {

        // todo - JWT 파싱 오너 ID와 delete 대사 store의 owerId와 같은지 확인
//        String owerId = null;

//        if(owerId != storeRegisterDto.getOwerId()){}

        return ResponseEntity.ok().body(ApiResponse.success());

    }
}

