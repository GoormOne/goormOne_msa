package com.example.userservice.controller;

import com.example.common.dto.ApiResponse;
import com.example.userservice.dto.CustomerProfileUpdateReq;
import com.example.userservice.dto.OwnerProfileUpdateReq;
import com.example.userservice.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/*
* --- user ---
* 1. 고객/사장 회원가입 (인증 X)
* 2. 고객/사장 회원탈퇴
* 3. 고객/사장 내정보 조회
* 4. 고객/사장 내정보 수정
* 5. 고객/사장 비밀번호 변경
* 6. 관리자 회원정보 조회, 수정
* */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
//    •	POST /users (프로필 생성; auth 등록 후 호출)
//    •	GET /users/me
//	  •	PUT /users/me
//	  •	DELETE /users/me (soft delete)
//	  •	GET /admin/users?query=&sort=createdAt,desc&page=0&size=10
//    •	PATCH /admin/users/{id} (role/is_banned)

    private final UserProfileService service;

    // customers
    @GetMapping("/customers/{customerId}")
    public ResponseEntity<ApiResponse<?>> getCustomer(@PathVariable UUID customerId) {
        return ResponseEntity.ok(ApiResponse.success(service.getCustomer(customerId)));
    }

    @PutMapping("/customers/{customerId}")
    public ResponseEntity<ApiResponse<?>> updateCustomer(
            @PathVariable UUID customerId,
            @Valid @RequestBody CustomerProfileUpdateReq req) {
        return ResponseEntity.ok(ApiResponse.success(service.updateCustomer(customerId, req)));
    }

    @DeleteMapping("/customers/{customerId}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(
            @PathVariable UUID customerId,
            @RequestParam(required = false, defaultValue = "user request") String reason) {
        service.deleteCustomer(customerId, reason);
        return ResponseEntity.ok(ApiResponse.success()); // 성공만 반환
    }

    // owners
    @GetMapping("/owners/{ownerId}")
    public ResponseEntity<ApiResponse<?>> getOwner(@PathVariable UUID ownerId) {
        return ResponseEntity.ok(ApiResponse.success(service.getOwner(ownerId)));
    }

    @PutMapping("/owners/{ownerId}")
    public ResponseEntity<ApiResponse<?>> updateOwner(
            @PathVariable UUID ownerId,
            @Valid @RequestBody OwnerProfileUpdateReq req) {
        return ResponseEntity.ok(ApiResponse.success(service.updateOwner(ownerId, req)));
    }

    @DeleteMapping("/owners/{ownerId}")
    public ResponseEntity<ApiResponse<Void>> deleteOwner(
            @PathVariable UUID ownerId,
            @RequestParam(required = false, defaultValue = "user request") String reason) {
        service.deleteOwner(ownerId, reason);
        return ResponseEntity.ok(ApiResponse.success());
    }
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
