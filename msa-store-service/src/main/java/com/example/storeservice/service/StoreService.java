package com.example.storeservice.service;


import com.example.storeservice.dto.StoreRegisterDto;
import com.example.storeservice.entity.Store;
import com.example.storeservice.entity.StoreAudit;
import com.example.storeservice.repository.StoreAuditRepository;
import com.example.storeservice.repository.StoreRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;

    public Store getStore(UUID storeId) {
        return storeRepository.findByStoreIdAndIsDeletedFalse(storeId)
                .orElseThrow(() -> new EntityNotFoundException("없는 상점입니다 : " + storeId)
        );
    }


    public Store insertStore(Store store) {
        return storeRepository.save(store);
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

        return store; // 더티체킹으로 UPDATE
    }

    @Transactional
    public UUID deleteStore(UUID storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("없는 상점입니다 : " + storeId)
                );
        store.setIsDeleted(true);

        return store.getStoreId();
    }
}


