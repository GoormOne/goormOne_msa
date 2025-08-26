package com.example.authservice.controller;

import com.example.authservice.dto.*;
import com.example.authservice.service.AuthService;
import com.example.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    // === 고객/사장 회원가입 ===
    @PostMapping("/customers/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RegisterRes> registerCustomer(@Valid @RequestBody RegisterCustomerReq req) {
        return ApiResponse.success(authService.registerCustomer(req));
    }

    @PostMapping("/owners/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RegisterRes> registerOwner(@Valid @RequestBody RegisterOwnerReq req) {
        return ApiResponse.success(authService.registerOwner(req));
    }


    // === 로그인 / 리프레시 / 로그아웃 ===
    @PostMapping("/login")
    public ApiResponse<LoginRes> login(@Valid @RequestBody LoginReq req) {
        return ApiResponse.success(authService.login(req));
    }

    @PostMapping("/token/refresh")
    public ApiResponse<RefreshRes> refresh(@Valid @RequestBody RefreshReq req) {
        return ApiResponse.success(authService.refresh(req));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody LogoutReq req) {
        authService.logout(req);
    }

//    private final AuthService authService;
//
//    // === 회원가입 ===
//    @PostMapping("/customers/register")
//    public ResponseEntity<RegisterRes> registerCustomer(@Valid @RequestBody RegisterCustomerReq req) {
//        return ResponseEntity.ok(authService.registerCustomer(req));
//    }
//
//    @PostMapping("/owners/register")
//    public ResponseEntity<RegisterRes> registerOwner(@Valid @RequestBody RegisterOwnerReq req) {
//        return ResponseEntity.ok(authService.registerOwner(req));
//    }
//
//    // === 로그인 ===
//    @PostMapping("/customers/login")
//    public ResponseEntity<LoginRes> loginCustomer(@Valid @RequestBody LoginReq req) {
//        return ResponseEntity.ok(authService.loginCustomer(req));
//    }
//
//    @PostMapping("/owners/login")
//    public ResponseEntity<LoginRes> loginOwner(@Valid @RequestBody LoginReq req) {
//        return ResponseEntity.ok(authService.loginOwner(req));
//    }
//
//    // === 로그아웃 === (게이트웨이 통해 인증 후, 헤더 기반 이중 체크)
//    @PostMapping("/logout")
//    @PreAuthorize("hasAnyRole('CUSTOMER','OWNER','ADMIN')")
//    public ResponseEntity<Void> logout(@RequestHeader(name = "X-User-Name", required = false) String username) {
//        if (StringUtils.hasText(username)) {
//            authService.logout(username);
//        }
//        return ResponseEntity.noContent().build();
//    }
//
//    // === 고객/사장 탈퇴 === (PK 기준, 컨트롤러/서비스 모두 이중 체크)
//    @DeleteMapping("/customers/me")
//    @PreAuthorize("hasRole('CUSTOMER')")
//    public ResponseEntity<DeleteRes> deleteMeCustomer(
//            @RequestHeader("X-User-Id") UUID customerId,
//            @RequestParam(name = "reason", required = false) String reason
//    ) {
//        authService.deleteMeCustomer(customerId, reason);
//        return ResponseEntity.ok(new DeleteRes(true));
//    }
//
//    @DeleteMapping("/owners/me")
//    @PreAuthorize("hasRole('OWNER')")
//    public ResponseEntity<DeleteRes> deleteMeOwner(
//            @RequestHeader("X-User-Id") UUID ownerId,
//            @RequestParam(name = "reason", required = false) String reason
//    ) {
//        authService.deleteMeOwner(ownerId, reason);
//        return ResponseEntity.ok(new DeleteRes(true));
//    }
//
//    // === 게이트웨이 내부용: username + principalType -> userId/name ===
//    @GetMapping("/internal/auth/resolve")
//    public ResponseEntity<ResolveRes> resolve(
//            @RequestParam String principalType,
//            @RequestParam String username
//    ) {
//        return ResponseEntity.ok(authService.resolve(principalType, username));
//    }
}