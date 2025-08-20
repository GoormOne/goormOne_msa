package com.example.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Component
class UserHeaderForwardingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return exchange.getPrincipal()
                .cast(Authentication.class)
                .defaultIfEmpty(null)
                .flatMap(auth -> {
                    var reqMut = exchange.getRequest().mutate();

                    if (auth instanceof JwtAuthenticationToken jwtAuth) {
                        Jwt jwt = jwtAuth.getToken();

                        // 표준 subject(고유 ID)
                        String userId = jwt.getSubject();

                        // Cognito 기준: cognito:username (환경에 맞게 변경)
                        String username = jwt.getClaimAsString("cognito:username");
                        if (username == null) username = jwt.getClaimAsString("username");

                        // 권한(ROLE_*)
                        String roles = jwtAuth.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.joining(","));

                        if (userId != null)   reqMut.header("X-User-Id", userId);
                        if (username != null) reqMut.header("X-Username", username);
                        if (!roles.isEmpty()) reqMut.header("X-Roles", roles);
                    }

                    var mutated = exchange.mutate().request(reqMut.build()).build();
                    return chain.filter(mutated);
                });
    }

    // 보안 필터 이후에 실행되도록 낮은 우선순위
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
