package com.example.gateway.filter;

import com.example.commonservice.security.HeaderNames;
import com.example.commonservice.security.PrincipalType;
import com.example.gateway.resolver.UserResolverClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

//
@Slf4j
@Component
@RequiredArgsConstructor
public class PrincipalHeaderGlobalFilter implements GlobalFilter, Ordered {

    private final UserResolverClient resolverClient;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        return exchange.getPrincipal()
                .cast(Authentication.class)
                .flatMap(principal -> {
                    if (!(principal instanceof JwtAuthenticationToken token)) {
                        return chain.filter(exchange); // 비인증 경로
                    }

                    Jwt jwt = (Jwt) token.getToken();
                    String username = jwt.getClaimAsString("cognito:username");
                    if (!StringUtils.hasText(username)) {
                        username = jwt.getClaimAsString("username");
                    }
                    String name = jwt.getClaimAsString("name");
                    List<String> groups = jwt.getClaimAsStringList("cognito:groups");
                    PrincipalType principalType = mapPrincipalType(groups);

                    return resolverClient.resolveUserId(principalType, username)
                            .defaultIfEmpty(UserResolverClient.ResolveResult.empty())
                            .flatMap(res -> {
                                ServerHttpRequest.Builder mut = exchange.getRequest().mutate()
                                        .header(HeaderNames.X_PRINCIPAL_TYPE, principalType.name())
                                        .header(HeaderNames.X_USER_ROLES, String.join(",", toSimpleRoles(groups)));

                                if (StringUtils.hasText(name)) {
                                    mut.header(HeaderNames.X_USER_NAME, name);
                                }
                                if (res.isFound() && res.getUserId() != null) {
                                    mut.header(HeaderNames.X_USER_ID, res.getUserId().toString());
                                }

                                return chain.filter(exchange.mutate().request(mut.build()).build());
                            });
                })
                .switchIfEmpty(chain.filter(exchange));
    }

    private Set<String> toSimpleRoles(List<String> groups) {
        return groups == null ? Set.of() : Set.copyOf(groups);
    }

    private PrincipalType mapPrincipalType(List<String> groups) {
        if (groups != null) {
            if (groups.contains("ADMIN")) return PrincipalType.ADMIN;
            if (groups.contains("OWNER")) return PrincipalType.OWNER;
            if (groups.contains("CUSTOMER")) return PrincipalType.CUSTOMER;
        }
        return PrincipalType.CUSTOMER;
    }

    @Override
    public int getOrder() { return -10; }
}