package com.example.userservice.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

public class AuthController {
    private final AuthService authService;

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(
            @Valid @RequestBody AuthRequestDTO.LoginRequestDto request) {

        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 로그아웃

    // 토큰 검증

    // 토큰 갱신
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> refreshToken(
            @RequestHeader("Authorization") String refreshToken) {

        String token = refreshToken.startsWith("Bearer ") ? refreshToken.substring(7) : refreshToken;
        LoginResponseDTO response = authService.refreshToken(token);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
