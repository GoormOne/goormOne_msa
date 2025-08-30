package com.example.storeservice.service;

import com.example.storeservice.entity.Store;
import com.example.storeservice.repository.MenuRepository;
import com.example.storeservice.repository.StoreRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.AccessDeniedException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreAuthorizationService {
    private final StoreRepository storeRepository;

    @Transactional(readOnly = true)
    public boolean isOwner(UUID storeId, UUID ownerId){
        Store store = storeRepository.findById(storeId)
                .orElseThrow(()-> new EntityNotFoundException("없는 상점 입니다")
                );
        if (!store.getOwnerId().equals(ownerId)){
            return false;
        }
        return true;
    }




}
