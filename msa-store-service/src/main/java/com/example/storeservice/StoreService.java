package com.example.storeservice;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreService {



    @GetMapping("/{storeId}")
    public ResponseEntity<?> getStore(
            @PathVariable String storeId,
            HttpServletRequest request) {

        return null;

    }

}
