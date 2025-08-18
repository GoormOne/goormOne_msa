package com.example.storeservice.controller;


import com.example.common.dto.ApiResponse;
import com.example.common.exception.CommonCode;
import com.example.storeservice.dto.StoreDto;
import com.example.storeservice.dto.StoreRegisterDto;
import com.example.storeservice.entity.StoreAudit;
import com.example.storeservice.exception.StoreAlreadyDeletedException;
import com.example.storeservice.interceptor.RequireStoreOwner;
import com.example.storeservice.service.StoreAuditService;
import com.example.storeservice.service.StoreService;
import com.example.storeservice.entity.Store;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.UUID;

/*
* ---store 생성 단계---
* 1.owner생성
* 2.store생성(스토어 카테고리 선택/ 배달 가능 리전 선택)
* => 스토어 카테고리, 배달 리전은 Get만. 생성 수정은 admin 서비스로 확장 염두
* 3.menuCategory생성
* 4.메뉴 생성
* ---인증이 필요한 엔드포인트들 ---
* 상점 수정/삭제, 메뉴 등록/수정/삭제, 메뉴카테고리 등록/수정/삭제, 상점 리전 등록/수정/삭제
* */
@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
@Slf4j
public class StoreController {
    private final StoreService storeService;
    private final StoreAuditService storeAuditService;


    // todo - 스토어 상세조회  : 메뉴카테고리, 메뉴, 리전, 오너까지 반환 추가
    @GetMapping("/{storeId}")
    public ResponseEntity<ApiResponse<?>> getStore(@PathVariable String storeId) {
        // todo - internal 통신시 인증 절차 추가
        Store store = storeService.getStore(UUID.fromString(storeId));

        StoreAudit storeAudit = storeAuditService.getAudit(store.getStoreId());
        if (storeAudit.getDeletedAt() != null) {
            throw new StoreAlreadyDeletedException("이미 삭제된 상점입니다: " + storeId);
        }

        StoreDto storeDto = StoreDto.fromEntity(store);

        return ResponseEntity.ok(ApiResponse.success(storeDto));

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
    @RequireStoreOwner
    public ResponseEntity<ApiResponse<?>> deleteStore(
            @PathVariable("storeId") String storeId){

        log.info("Store {} has been deleted", storeId);
        // TODO: JWT에서 ownerId 추출
        String ownerId = "a23b2047-a11e-4ec4-a16b-e82a5ff70636";

        StoreAudit storeAudit = storeAuditService.deleteAudit(UUID.fromString(storeId), UUID.fromString(ownerId));

        return ResponseEntity.ok(ApiResponse.success(storeAudit.getAuditId()));

    }


    @PutMapping("/{storeId}")
    @RequireStoreOwner
    public ResponseEntity<ApiResponse<?>> updateStore(
            @PathVariable("storeId") String storeId,
            @RequestBody StoreRegisterDto storeRegisterDto
    ){
        // TODO: JWT에서 ownerId 추출 == 요청자
        UUID storeUuid = UUID.fromString(storeId);
        UUID ownerUuid = UUID.fromString(storeRegisterDto.getOwerId());

        log.info("Store {} has been updated", storeId);

        StoreAudit storeAudit = storeAuditService.getAudit(UUID.fromString(storeId));
        if (storeAudit.getDeletedAt() != null) {
            throw new StoreAlreadyDeletedException("이미 삭제된 상점입니다: " + storeId);
        }

        Store updatedStore = storeService.updateStore(storeUuid, storeRegisterDto, ownerUuid);

        storeAuditService.updateAudit(storeAudit,ownerUuid);

        return ResponseEntity.ok(ApiResponse.success(updatedStore.getStoreId()));
    }

}

