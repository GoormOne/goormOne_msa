package com.example.storeservice.service;


import com.example.storeservice.dto.AiFlatRow;
import com.example.storeservice.dto.StoreRegisterDto;
import com.example.storeservice.mongoDB.AiDocumentEntity;
import com.example.storeservice.entity.Store;
import com.example.storeservice.entity.StoreAudit;
import com.example.storeservice.exception.StoreAlreadyDeletedException;
import com.example.storeservice.global.EventAction;
import com.example.storeservice.repository.StoreAuditRepository;
import com.example.storeservice.repository.StoreRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final StoreAuditRepository storeAuditRepository;
    private final OutboxService outboxService;

    public Store getStore(UUID storeId) {
        return storeRepository.findByStoreIdAndIsDeletedFalse(storeId)
                .orElseThrow(() -> new EntityNotFoundException("없는 상점입니다 : " + storeId)
        );
    }

    @Transactional
    public Store insertStore(Store store) {
        Store savedStore = storeRepository.save(store);
        storeAuditRepository.save(new StoreAudit(store.getOwnerId(),store.getStoreId()));
        long version = System.currentTimeMillis();
        outboxService.insertOutbox(store, version, EventAction.CREATED);
        return savedStore;
    }


    @Transactional
    public Store updateStore(UUID storeId, StoreRegisterDto dto) {
        Store store = storeRepository.findByStoreIdAndIsDeletedFalse(storeId)
                .orElseThrow(() -> new EntityNotFoundException("없는 상점입니다 : " + storeId));

        if (dto.getStoreName() != null) store.setStoreName(dto.getStoreName());
        if (dto.getStoreDescription() != null) store.setStoreDescription(dto.getStoreDescription());
        if (dto.getCategory() != null) store.setStoresCategoryId(UUID.fromString(dto.getCategory()));
        if (dto.getAddress1() != null) store.setAddress1(dto.getAddress1());
        if (dto.getAddress2() != null) store.setAddress2(dto.getAddress2());
        if (dto.getZipCd() != null) store.setZipCd(dto.getZipCd());
        if (dto.getStorePhone() != null) store.setStorePhone(dto.getStorePhone());
        if (dto.getStoreLatitude() != null) store.setStoreLatitude(dto.getStoreLatitude());
        if (dto.getStoreLongitude() != null) store.setStoreLongitude(dto.getStoreLongitude());
        if (dto.getOpenTime() != null) store.setOpenTime(dto.getOpenTime());
        if (dto.getCloseTime() != null) store.setCloseTime(dto.getCloseTime());

        StoreAudit storeAudit = storeAuditRepository.findById(storeId).orElseThrow(
                () -> new EntityNotFoundException("감사내역이 없습니다 : " + storeId));
        if (storeAudit.getDeletedAt() != null) {
            throw new StoreAlreadyDeletedException("이미 삭제된 상점입니다. ");
        }
        storeAudit.setDeletedBy(dto.getOwnerId());
        storeAudit.setDeletedAt(LocalDateTime.now());

        long version = System.currentTimeMillis();
        outboxService.insertOutbox(store, version, EventAction.UPDATED);

        return store; // 더티체킹으로 UPDATE
    }

    @Transactional
    public UUID deleteStore(UUID storeId, UUID deleterId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("없는 상점입니다 : " + storeId)
                );
        store.setIsDeleted(true);


        StoreAudit storeAudit = storeAuditRepository.findById(storeId).orElseThrow(
                () -> new EntityNotFoundException("감사내역이 없습니다 : " + storeId));
        if (storeAudit.getDeletedAt() != null) {
            throw new StoreAlreadyDeletedException("이미 삭제된 상점입니다. ");
        }

        storeAudit.setDeletedBy(deleterId);
        storeAudit.setDeletedAt(LocalDateTime.now());

        long version = System.currentTimeMillis();
        outboxService.insertOutbox(store, version, EventAction.DELETED);


        return store.getStoreId();
    }

    public Page<UUID> findAllStoreIds(Pageable pageable) {
        return storeRepository.findStoreIdPage(pageable);
    }

    public List<AiDocumentEntity> toDocuments(List<AiFlatRow> rows) {
        List<AiDocumentEntity> result = new ArrayList<>();
        if (rows == null || rows.isEmpty()) return result;

        // storeId -> 완성 중인 문서
        Map<UUID, AiDocumentEntity> storeMap = new LinkedHashMap<>();
        // storeId -> (menuId -> 메뉴)
        Map<UUID, Map<UUID, AiDocumentEntity.Menus>> menuMapByStore = new LinkedHashMap<>();

        for (AiFlatRow r : rows) {
            UUID storeId  = r.getStoreId();
            UUID menuId   = r.getMenuId();
            UUID reviewId = r.getReviewId();

            // 1) 스토어 문서 확보/생성
            AiDocumentEntity storeDoc = storeMap.get(storeId);
            if (storeDoc == null) {
                storeDoc = AiDocumentEntity.builder()
                        .storeId(storeId)
                        .storeName(r.getStoreName())
                        .menus(new ArrayList<>())
                        .updateAt(LocalDateTime.now())
                        .build();
                storeMap.put(storeId, storeDoc);
                menuMapByStore.put(storeId, new LinkedHashMap<>());
            }

            // 2) 메뉴 확보/생성 + 스토어에 연결
            Map<UUID, AiDocumentEntity.Menus> menusOfStore = menuMapByStore.get(storeId);
            AiDocumentEntity.Menus menuDoc = menusOfStore.get(menuId);
            if (menuDoc == null) {
                menuDoc = AiDocumentEntity.Menus.builder()
                        .menuId(menuId)
                        .menuName(r.getMenuName())
                        .reviews(new ArrayList<>())
                        .build();
                menusOfStore.put(menuId, menuDoc);
                storeDoc.getMenus().add(menuDoc);
            }

            // 3) 리뷰 추가
            AiDocumentEntity.Reviews reviewDoc = AiDocumentEntity.Reviews.builder()
                    .reviewId(reviewId)
                    .text(r.getComment())
                    .createAt(r.getCreatedAt())
                    .build();

            menuDoc.getReviews().add(reviewDoc);
        }

        // 최종 문서 리스트 반환
        return new ArrayList<>(storeMap.values());
    }


}


