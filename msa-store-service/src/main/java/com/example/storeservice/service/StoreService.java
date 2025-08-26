package com.example.storeservice.service;


import com.example.storeservice.dto.StoreRegisterDto;
import com.example.storeservice.entity.Store;
import com.example.storeservice.entity.StoreAudit;
import com.example.storeservice.global.exception.StoreAlreadyDeletedException;
import com.example.storeservice.global.EventAction;
import com.example.storeservice.repository.StoreAuditRepository;
import com.example.storeservice.repository.StoreRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
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




}


