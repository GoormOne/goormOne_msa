package com.example.storeservice.controller;


import com.example.storeservice.dto.StoreDto;
import com.example.storeservice.service.StoreService;
import com.example.storeservice.entity.Store;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
@Slf4j
public class StoreController {
    private final StoreService storeService;

    @GetMapping("/{storeId}")
    public ResponseEntity<?> getStore(@PathVariable String storeId) {

        log.info("ezra Get store by id {}", storeId);
        Optional<Store> dto = storeService.getStore(UUID.fromString(storeId));
        if (dto.isPresent()) {
            return ResponseEntity.ok(StoreDto.fromEntity(dto.get()));
        }else {
            return ResponseEntity.notFound().build();
        }

    }

}

