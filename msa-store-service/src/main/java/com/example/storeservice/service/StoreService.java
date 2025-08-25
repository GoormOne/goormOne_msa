package com.example.storeservice.service;


import com.example.storeservice.dto.StoreDto;
import com.example.storeservice.dto.StoreRegisterDto;
import com.example.storeservice.entity.Store;
import com.example.storeservice.entity.StoreAudit;
import com.example.storeservice.repository.StoreAuditRepository;
import com.example.storeservice.repository.StoreRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;

//    @Cacheable( value = "store", key = "#storeId")
//    public Store getStore(UUID storeId) {
//        return storeRepository.findByStoreIdAndIsDeletedFalse(storeId)
//                .orElseThrow(() -> new EntityNotFoundException("없는 상점입니다 : " + storeId)
//        );
//    }
    @Cacheable(value = "store", key = "#storeId")
    public StoreDto getStore(UUID storeId) { // 반환 타입을 StoreDto로 수정
        Store store = storeRepository.findByStoreIdAndIsDeletedFalse(storeId)
                .orElseThrow(() -> new EntityNotFoundException("없는 상점입니다 : " + storeId));

        return StoreDto.fromEntity(store);
    }


    public Store insertStore(Store store) {
        return storeRepository.save(store);
    }

    @Transactional
    @CacheEvict(value = "store" , key ="#storeId")
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
    //메서드가 성공적으로 완료되면 해당 내역도 캐시에서 삭제
    @CacheEvict(value = "store" , key ="#storeId")
    public UUID deleteStore(UUID storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new EntityNotFoundException("없는 상점입니다 : " + storeId)
                );
        store.setIsDeleted(true);

        return store.getStoreId();
    }
}


