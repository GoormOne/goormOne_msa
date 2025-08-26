package com.example.userservice.controller;

import com.example.userservice.dto.*;
import com.example.userservice.service.UserService;
import com.example.common.web.AuthUser;
import com.example.common.dto.AuthHeaders;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userservice;

    // ===== CUSTOMER =====
    @GetMapping("/customers/me")
    public MyProfileRes getMyCustomer(@AuthUser AuthHeaders headers) {
        requireGroup(headers, "CUSTOMER");
        UUID id = uuid(headers.getUserId());
        return userservice.getMyCustomer(id);
    }

    @PutMapping("/customers/me")
    public MyProfileRes updateMyCustomer(@AuthUser AuthHeaders headers,
                                         @RequestBody UpdateCustomerReq req) {
        requireGroup(headers, "CUSTOMER");
        UUID id = uuid(headers.getUserId());
        return userservice.updateMyCustomer(id, req);
    }

    @PatchMapping("/customers/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeMyCustomerPassword(@AuthUser AuthHeaders headers,
                                         @RequestBody ChangePasswordReq req) {
        requireGroup(headers, "CUSTOMER");
        UUID id = uuid(headers.getUserId());
        userservice.changeCustomerPassword(id, req);
    }

    // ===== OWNER =====
    @GetMapping("/owners/me")
    public MyProfileRes getMyOwner(@AuthUser AuthHeaders headers) {
        requireGroup(headers, "OWNER");
        UUID id = uuid(headers.getUserId());
        return userservice.getMyOwner(id);
    }

    @PutMapping("/owners/me")
    public MyProfileRes updateMyOwner(@AuthUser AuthHeaders headers,
                                      @RequestBody UpdateOwnerReq req) {
        requireGroup(headers, "OWNER");
        UUID id = uuid(headers.getUserId());
        return userservice.updateMyOwner(id, req);
    }

    @PatchMapping("/owners/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeMyOwnerPassword(@AuthUser AuthHeaders headers,
                                      @RequestBody ChangePasswordReq req) {
        requireGroup(headers, "OWNER");
        UUID id = uuid(headers.getUserId());
        userservice.changeOwnerPassword(id, req);
    }

    // ===== 내부 유저 PK 해석용(게이트웨이에서 호출) =====
    //  GW가 "로컬 PK"를 알아내기 위한 내부 전용 API
    //  여기서만 username/email로 조회해서 PK 반환, 일반 비즈니스 API는 PK로만 조회
    //  실제 구현에서 CustomerRepository/OwnerRepository에 findByUsername/findByEmail 같은 읽기용 메서드를 추가해서 resolve 내부를 완성
    //  GET /internal/users/resolve?username=...&email=...&group=CUSTOMER|OWNER|ADMIN
    @GetMapping("/internal/users/resolve")
    public ResolveRes resolve(@RequestParam String username,
                              @RequestParam String email,
                              @RequestParam String group) {
        // 로컬 전략:
        // - group에 따라 해당 테이블에서 username 또는 email 로 매핑 PK를 찾고 반환
        // - "PK만 조회" 원칙에 맞추기 위해, 여기서는 내부용 예외로 username/email -> PK 변환을 허용
        String principalType = group.toUpperCase();
        String userId = switch (principalType) {
            case "OWNER" -> findOwnerIdByUsernameOrEmail(username, email);
            case "ADMIN" -> findAdminIdByUsernameOrEmail(username, email); // 필요 시 구현
            default -> findCustomerIdByUsernameOrEmail(username, email);
        };
        return new ResolveRes(principalType, userId, username, email, new String[]{group});
    }

    // === 단순 샘플 구현부 (실서비스에서는 Repository 메서드 추가/쿼리 작성) ===
    private String findCustomerIdByUsernameOrEmail(String username, String email) {
        // 예: customerRepository.findByUsername(...) 또는 findByEmail(...)
        // 여기서는 간략화를 위해 username/email -> UUID 변환 로직을 별도로 두지 않고,
        // 실제 구현 시 Repository 메서드를 추가하세요.
        throw new UnsupportedOperationException("Implement customer resolve by username/email");
    }
    private String findOwnerIdByUsernameOrEmail(String username, String email) {
        throw new UnsupportedOperationException("Implement owner resolve by username/email");
    }
    private String findAdminIdByUsernameOrEmail(String username, String email) {
        throw new UnsupportedOperationException("Implement admin resolve by username/email");
    }

    // ===== 유틸 =====
    private void requireGroup(AuthHeaders headers, String required) {
        if (!required.equalsIgnoreCase(headers.getGroups())) {
            throw new org.springframework.security.access.AccessDeniedException("FORBIDDEN_GROUP");
        }
    }
    private UUID uuid(String v) {
        try { return UUID.fromString(v); }
        catch (Exception e) { throw new IllegalArgumentException("INVALID_USER_ID"); }
    }

    // 내부 응답 DTO
    public record ResolveRes(String principalType, String userId, String username, String email, String[] roles) { }
}