package com.example.userservice.controller;

import com.example.common.dto.ApiResponse;
import com.example.userservice.dto.request.SignupRequestDto;
import com.example.userservice.service.UserAuditService;
import com.example.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;
    private final UserAuditService userAuditService;

    // 고객 회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<?>> signup(
            @Valid @RequestBody SignupRequestDto dto
    ) {
        UUID userId = userService.signup(dto);
        return ResponseEntity.ok(ApiResponse.success(userId));
    }

//    // 사장 회원가입
//    @PostMapping("/signup/owner")
//    public ResponseEntity<ApiResponse<String>> signupOwner(@Valid @RequestBody SignupRequestDto request) {
//        String result = userService.createOwner(request);
//        return ResponseEntity.ok(ApiResponse.success(result));
//    }
}

// 기존 코드 주석처리
/*
import com.example.userservice.dto.request.AuthRequestDTO;
import com.example.userservice.dto.response.AuthResponseDTO;
import com.example.userservice.entity.Role;
import com.example.userservice.service.AuthService;
import com.example.userservice.service.UserAddressService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import java.util.List;

private final UserAddressService userAddressService;
private final AuthService authService;

// 회원 가입
@PostMapping("/signup/{role:customer|owner}")
public ResponseEntity<ApiResponse<AuthResponseDTO.AuthRegisterResponseDTO>> signup(
        @PathVariable String role,
        @Valid @RequestBody AuthRequestDTO.RegisterRequestDto request) {
    Role r = "owner".equalsIgnoreCase(role) ? Role.OWNER : Role.CUSTOMER;
    AuthResponseDTO.AuthRegisterResponseDTO res = userService.createUser(request, r);
}

@PostMapping("/signup/customer")
public ResponseEntity<ApiResponse<String>> signup(@RequestBody SignupRequestDto request) {
    String result = userService.createUser(request);
    return ResponseEntity.ok(ApiResponse.success(result));
}
@PostMapping("/register/customer")
public ResponseEntity<ApiResponse<AuthResponseDTO.AuthRegisterResponseDTO>> registerCustomer(
        @Valid @RequestBody AuthRequestDTO.RegisterRequestDto request) {

    AuthResponseDTO.AuthRegisterResponseDTO response = authService.registerCustomer(request);
    return ResponseEntity.ok(ApiResponse.success(response));
}
@PostMapping("/register/owner")
public ResponseEntity<ApiResponse<AuthResponseDTO.AuthRegisterResponseDTO>> registerOwner(
        @Valid @RequestBody AuthRequestDTO.RegisterRequestDto request) {

    AuthResponseDTO.AuthRegisterResponseDTO response = authService.registerOwner(request);
    return ResponseEntity.ok(ApiResponse.success(response));
}

// 회원 탈퇴
@DeleteMapping("/withdraw")
public ResponseEntity<ApiResponse<String>> withdraw(Authentication authentication) {
    String username = authentication.getName();
    String result = userService.softDeleteUser(username);
    return ResponseEntity.ok(ApiResponse.success(result));
}
*/
