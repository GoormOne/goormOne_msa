package com.example.gateway;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
class GatewayFallbackController {
    @RequestMapping("/__fallback/store")
    public ResponseEntity<Map<String,Object>> storeFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("service","store","message","temporarily unavailable"));
    }
}
