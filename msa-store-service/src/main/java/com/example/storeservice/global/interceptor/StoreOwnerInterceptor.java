package com.example.storeservice.global.interceptor;

import com.example.storeservice.service.StoreAuthorizationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoreOwnerInterceptor implements HandlerInterceptor {
    private final StoreAuthorizationService authz;

    @Override
    @SuppressWarnings("unchecked")
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
        // 컨트롤러 핸들러가 아니면 통과 => 리소스 핸들러는 거르기
        if (!(handler instanceof HandlerMethod hm)) return true;

        // 보호 대상 메서드만 검사
        RequireStoreOwner anno = hm.getMethodAnnotation(RequireStoreOwner.class);
        if (anno == null) return true;

        // PathVariable에서 storeId 추출
        var uriVars = (Map<String, String>) req.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        String storeIdStr = (uriVars != null) ? uriVars.get(anno.storeIdParam()) : null;
        if (storeIdStr == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "missing path variable: " + anno.storeIdParam());
        }

        final UUID storeId;
        try {
            storeId = UUID.fromString(storeIdStr);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid UUID: " + storeIdStr);
        }

        //TODO- jwt 검증 로직 추가 - admin 기능 추가
        // 현재 사용자 식별 (임시: 헤더 모킹 또는 운영: JWT)
        UUID userId = UUID.fromString("2ae528d0-1414-4cf3-ac1d-e8ff642c9056"); // jwt 구현 시 적용

        // 관리자 우회 옵션 적용
//        if (anno.allowAdmin() && current.hasRole("ADMIN")) {
//            return true; // ADMIN이면 바로 통과
//        }

        // 상점 주인 검증 and 상점 존재 확인
        boolean ok = authz.isOwner(storeId, userId);
        if (!ok) {
            // 상점은 있는데 소유자가 아님 → 403
            log.info("Store {} has been denied", storeId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "not owner");
        }
        log.info("Store 인증완료 {} ", storeId);


        return true;
    }

    // jwt 구현시
//    private boolean hasRole(String role) {
//        var auth = SecurityContextHolder.getContext().getAuthentication();
//        return auth != null && auth.getAuthorities().stream()
//                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
//    }
}