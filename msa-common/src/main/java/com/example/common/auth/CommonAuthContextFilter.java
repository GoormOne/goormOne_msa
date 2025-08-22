package com.example.common.auth;

import com.example.common.dto.AuthHeaders;
import com.example.common.dto.RequestUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.hasText;

/**
 * 게이트웨이 1차 검증 신뢰 + (옵션) JWT 경량 재검증 필터.
 * - 1순위: 게이트웨이가 주입한 헤더(X-User-*) 사용
 * - 2순위: Authorization: Bearer ... 를 Cognito JWKS로 decode (iss/aud/exp)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommonAuthContextFilter extends OncePerRequestFilter {

    private final JwtDecoder jwtDecoder;              // spring.security.oauth2.resourceserver.jwt.* 로 설정
    private final boolean trustGatewayHeaders = true; // 필요 시 구성으로 뺄 수 있음

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {

        try {
            RequestUser ru = null;

            if (trustGatewayHeaders) {
                ru = extractFromGatewayHeaders(req);
            }

            if (ru == null) {
                ru = extractFromJwt(req); // Authorization Bearer 경량 재검증
            }

            if (ru != null) {
                Authentication auth = new UsernamePasswordAuthenticationToken(ru, null, ru.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (JwtException e) {
            // 토큰 문제는 인증 없음으로 통과(리소스 접근은 Security 설정에 따름)
            log.debug("[Auth] JWT invalid: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("[Auth] Unexpected auth error", e);
        }

        chain.doFilter(req, res);
    }

    // ============ 1) 게이트웨이 헤더에서 추출 ============
    @Nullable
    private RequestUser extractFromGatewayHeaders(HttpServletRequest req) {
        String idStr      = req.getHeader(AuthHeaders.X_USER_ID);
        String userType   = req.getHeader(AuthHeaders.X_USER_TYPE);
        String username   = req.getHeader(AuthHeaders.X_USERNAME);
        String email      = req.getHeader(AuthHeaders.X_EMAIL);
        String groupsCsv  = req.getHeader(AuthHeaders.X_GROUPS);

        if (!hasText(idStr) || !hasText(userType)) return null;

        UUID id;
        try { id = UUID.fromString(idStr); }
        catch (IllegalArgumentException e) { return null; }

        Set<String> roles = new HashSet<>();
        if (hasText(userType)) roles.add(userType.trim().toUpperCase(Locale.ROOT));
        if (hasText(groupsCsv)) {
            roles.addAll(Arrays.stream(groupsCsv.split(","))
                    .map(s -> s.trim().toUpperCase(Locale.ROOT))
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet()));
        }

        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .distinct()
                .toList();

        return RequestUser.builder()
                .id(id)
                .username(username)
                .email(email)
                .userType(primaryType(roles))
                .authorities(authorities)
                .build();
    }

    // ============ 2) Authorization Bearer JWT에서 추출 ============
    @Nullable
    private RequestUser extractFromJwt(HttpServletRequest req) {
        String authz = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (!hasText(authz) || !authz.startsWith("Bearer ")) return null;

        String token = authz.substring(7);
        Jwt jwt = jwtDecoder.decode(token); // 서명/exp/iss 검증. aud 검증은 JwtDecoder 구성에 따름.

        // Cognito 표준 클레임 예시:
        // - sub: 전역 유저 식별자(가능하면 우리 DB의 PK와 매핑)
        // - username / email
        // - cognito:groups: ["CUSTOMER", ...]
        String sub      = jwt.getClaimAsString("sub");
        String username = firstNonNull(jwt.getClaimAsString("username"),
                jwt.getClaimAsString("cognito:username"));
        String email    = jwt.getClaimAsString("email");

        // groups: 배열 혹은 문자열 상황 모두 대응
        Set<String> roles = new HashSet<>();
        List<String> groupList = jwt.getClaimAsStringList("cognito:groups");
        if (groupList != null) {
            roles.addAll(groupList.stream()
                    .map(s -> s.trim().toUpperCase(Locale.ROOT))
                    .toList());
        } else {
            String groupsCsv = jwt.getClaimAsString("cognito:groups");
            if (hasText(groupsCsv)) {
                roles.addAll(Arrays.stream(groupsCsv.split(","))
                        .map(s -> s.trim().toUpperCase(Locale.ROOT))
                        .toList());
            }
        }

        // userType 우선순위(프로젝트 역할 3종 일치)
        String userType = primaryType(roles);

        // 우리 DB PK(계정 테이블의 id)와 sub가 1:1 매핑이라면 그대로 사용
        // 아니라면, 게이트웨이에서 X-User-Id로 내려주도록 하고 여기선 null 처리 가능
        UUID id = safeToUuid(sub);

        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .distinct()
                .toList();

        return RequestUser.builder()
                .id(id) // null일 수 있음: 이후 서비스에서 id 기준 조회 시 게이트웨이 헤더 사용 권장
                .username(username)
                .email(email)
                .userType(userType)
                .authorities(authorities)
                .build();
    }

    // ============ 유틸 ============
    private static String primaryType(Set<String> rolesUpper) {
        if (rolesUpper.contains("ADMIN"))    return "ADMIN";
        if (rolesUpper.contains("OWNER"))    return "OWNER";
        if (rolesUpper.contains("CUSTOMER")) return "CUSTOMER";
        // 알 수 없으면 CUSTOMER로 다운그레이드 or null
        return "CUSTOMER";
    }

    @Nullable
    private static UUID safeToUuid(@Nullable String v) {
        if (!hasText(v)) return null;
        try { return UUID.fromString(v); } catch (IllegalArgumentException e) { return null; }
    }

    @Nullable
    private static String firstNonNull(@Nullable String a, @Nullable String b) {
        return hasText(a) ? a : (hasText(b) ? b : null);
    }
}
