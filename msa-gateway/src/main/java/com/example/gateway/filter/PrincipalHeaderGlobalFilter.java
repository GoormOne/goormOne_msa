package com.example.gateway.filter;

import com.example.gateway.security.JwtRoleConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class PrincipalHeaderGlobalFilter implements GlobalFilter, Ordered {

    private final JwtRoleConverter roleConverter;
    private final WebClient.Builder lbWebClientBuilder; // @LoadBalanced 필요

    private static final String USER_SERVICE_BASE = "lb://msa-user-service";
    private static final Duration RESOLVE_TIMEOUT = Duration.ofMillis(1500);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        final String path = exchange.getRequest().getURI().getPath();

        // 1) 공개 엔드포인트는 건드리지 말고 통과 (회원가입/로그인/헬스)
        if (path.startsWith("/auth/")
                || path.startsWith("/internal/auth/")
                || path.startsWith("/actuator/")
                || path.startsWith("/health/")) {
            return chain.filter(exchange);
        }

        // 2) 인증 주체가 없으면 그대로 통과 (절대 체인 중단 금지)
        return exchange.getPrincipal()
                .ofType(Authentication.class)
                .flatMap(auth -> {
                    if (!(auth instanceof AbstractAuthenticationToken token)) return chain.filter(exchange);
                    if (!(token.getPrincipal() instanceof Jwt jwt)) return chain.filter(exchange);

                    // 그룹/사용자 정보 추출
                    List<String> groups = roleConverter.extractGroups(jwt);  // ["OWNER","CUSTOMER"] 등
                    String principalType = choosePrincipalType(groups);      // OWNER/ADMIN/CUSTOMER
                    String username = coalesce(jwt.getClaimAsString("cognito:username"),
                            jwt.getClaimAsString("username"));
                    String email    = coalesce(jwt.getClaimAsString("email"));
                    String userId   = coalesce(jwt.getClaimAsString("sub_user_id")); // 없을 가능성 큼

                    // 3) userId 없으면 user-service로 resolve (동일 토큰 전달)
                    Mono<String> userIdMono = userId.isBlank() && !isBlank(username)
                            ? resolveUserId(exchange, username, email, principalType)
                            .onErrorReturn("")   // 실패해도 체인 끊지 않음
                            : Mono.just(userId);

                    return userIdMono.flatMap(uid -> {
                        ServerHttpRequest mutated = exchange.getRequest().mutate()
                                .header("X-Groups", safe(principalType))                 // OWNER | CUSTOMER | ADMIN
                                .header("X-User-Id", safe(uid))                           // UUID 문자열
                                .header("X-User-Name", safe(username))
                                .header("X-Email", safe(email))
                                .header("X-User-Roles", String.join(",", groups))         // 전체 그룹 CSV
                                .build();
                        return chain.filter(exchange.mutate().request(mutated).build());
                    });
                })
                .switchIfEmpty(chain.filter(exchange))        // principal 없어도 계속 진행
                .onErrorResume(ex -> chain.filter(exchange)); // 어떤 예외가 나도 계속 진행
    }

    /** user-service /internal/users/resolve 호출해서 UUID 받기 */
    private Mono<String> resolveUserId(ServerWebExchange exchange, String username, String email, String principalType) {
        String bearer = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        WebClient client = lbWebClientBuilder.baseUrl(USER_SERVICE_BASE).build();

        return client.get()
                .uri(uri -> uri.path("/internal/users/resolve")
                        .queryParam("username", username)
                        .queryParam("email", email)
                        .queryParam("group", principalType)
                        .build())
                // ★ user-service 보안이 authenticated()이므로 동일 토큰 전달이 필수
                .header(HttpHeaders.AUTHORIZATION, bearer == null ? "" : bearer)
                .retrieve()
                .bodyToMono(ResolveRes.class)
                .timeout(RESOLVE_TIMEOUT)
                .map(res -> res == null || isBlank(res.userId) ? "" : res.userId);
    }

    private static class ResolveRes {
        public String principalType;
        public String userId;
        public String username;
        public String email;
        public List<String> roles;
    }

    private static String choosePrincipalType(List<String> groups) {
        if (groups == null || groups.isEmpty()) return "CUSTOMER";
        boolean hasOwner    = groups.stream().filter(Objects::nonNull).anyMatch(g -> "OWNER".equalsIgnoreCase(g));
        boolean hasAdmin    = groups.stream().filter(Objects::nonNull).anyMatch(g -> "ADMIN".equalsIgnoreCase(g));
        boolean hasCustomer = groups.stream().filter(Objects::nonNull).anyMatch(g -> "CUSTOMER".equalsIgnoreCase(g));
        if (hasOwner)    return "OWNER";
        if (hasAdmin)    return "ADMIN";
        if (hasCustomer) return "CUSTOMER";
        String first = groups.get(0);
        return first == null ? "CUSTOMER" : first.toUpperCase(Locale.ROOT);
    }

    private static String coalesce(String... s) {
        if (s == null) return "";
        for (String v : s) if (v != null && !v.isBlank()) return v;
        return "";
    }
    private static boolean isBlank(String v){ return v == null || v.isBlank(); }
    private static String safe(String v) { return v == null ? "" : v; }

    @Override public int getOrder() { return -1; } // SecurityWebFilterChain 이후, 라우팅 전
}