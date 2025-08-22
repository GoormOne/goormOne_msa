package com.example.storeservice.controller;


import com.example.common.dto.ApiResponse;
import com.example.common.exception.CommonCode;
import com.example.storeservice.dto.AiFlatRow;
import com.example.storeservice.dto.StoreDto;
import com.example.storeservice.dto.StoreRegisterDto;
import com.example.storeservice.entity.AiDocumentEntity;
import com.example.storeservice.entity.StoreAudit;
import com.example.storeservice.exception.StoreAlreadyDeletedException;
import com.example.storeservice.interceptor.RequireStoreOwner;
import com.example.storeservice.repository.StoreBatchQueryRepository;
import com.example.storeservice.service.OutboxService;
import com.example.storeservice.service.StoreAuditService;
import com.example.storeservice.service.StoreService;
import com.example.storeservice.entity.Store;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;

/*
* ---store 생성 단계---
* 1.owner생성
* 2.store생성(스토어 카테고리 선택/ 배달 가능 리전 선택)
* => 스토어 카테고리, 배달 리전은 Get만. => 생성 수정은 admin 서비스로 확장 염두
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
    private final StoreBatchQueryRepository storeBatchQueryRepository;

    @GetMapping("/test")
    public ResponseEntity<ApiResponse> test() {

        Pageable pageable = PageRequest.of(0, 100);
        Page<UUID> idPage = storeService.findAllStoreIds(pageable);
        List<UUID> storeIds = idPage.getContent();
        List<AiFlatRow> aiFlatRowPage = storeBatchQueryRepository.findFlatRows(storeIds);
        List<AiDocumentEntity> aiDocumentEntities = storeService.toDocuments(aiFlatRowPage);

        return ResponseEntity.ok(ApiResponse.success(aiDocumentEntities));
    }

    // todo - 스토어 상세조회  : 메뉴카테고리, 메뉴, 리전, 오너까지 반환 추가
    @GetMapping("/{storeId}")
    public ResponseEntity<ApiResponse<?>> getStore(@PathVariable UUID storeId) {
        // todo - internal 통신시 인증 절차 추가
        Store store = storeService.getStore(storeId);

        StoreDto storeDto = StoreDto.fromEntity(store);

        return ResponseEntity.ok(ApiResponse.success(storeDto));

    }

    @PostMapping("/")
    public ResponseEntity<ApiResponse<?>> saveStore(
            @Valid @RequestBody StoreRegisterDto storeRegisterDto
    ) {
        // todo - JWT 파싱 오너 ID와 post owerId와 같은지 확인
//        String owerId = null;
//        if(owerId != storeRegisterDto.getOwerId()){}

        log.info("StoreController.saveStore - storeRegisterDto:{}", storeRegisterDto);
        //store 저장
        UUID pk = UUID.randomUUID();

        log.info("StoreController.saveStore - pk:{}", storeRegisterDto.toEntity(pk).toString());
        Store store = storeService.insertStore(storeRegisterDto.toEntity(pk));

        log.info("Store {} has been inserted", store.toString());
        return ResponseEntity.ok(ApiResponse.success(store.getStoreId()));
    }

    @DeleteMapping("/{storeId}")
    @RequireStoreOwner
    public ResponseEntity<ApiResponse<?>> deleteStore(
            @PathVariable("storeId") UUID storeId){

        // TODO: JWT에서 ownerId 추출
        String ownerId = "a23b2047-a11e-4ec4-a16b-e82a5ff70636";
        UUID ownerUuid = UUID.fromString(ownerId);

        UUID deletedId = storeService.deleteStore(storeId, ownerUuid);

        return ResponseEntity.ok(ApiResponse.success(deletedId));

    }


    @PutMapping("/{storeId}")
    @RequireStoreOwner
    public ResponseEntity<ApiResponse<?>> updateStore(
            @PathVariable("storeId") UUID storeId,
            @Valid @RequestBody StoreRegisterDto storeRegisterDto
    ){
        // TODO: JWT에서 ownerId 추출 == 요청자
        UUID ownerUuid = storeRegisterDto.getOwnerId();

        Store updatedStore = storeService.updateStore(storeId, storeRegisterDto);


        return ResponseEntity.ok(ApiResponse.success(updatedStore.getStoreId()));
    }

}

