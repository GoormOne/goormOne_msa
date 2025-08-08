package com.example.storeservice;


import com.example.commonlibrary.ApiResponse;
import com.example.storeservice.dto.StoreDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/store")
@RequiredArgsConstructor
public class StoreController {
    private final StoreService storeService;


    @GetMapping("/{storeId}")
    public ResponseEntity<ApiResponse<?>> getStore(
            @PathVariable String storeId,
            HttpServletRequest request
    ){



        return null;
    }
}
