package com.example.gateway.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collection;
import java.util.function.Function;

// Security 설정
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${cognito.groups-claim:cognito:groups}")
    private String groupsClaim;

    @Value("${cognito.expected-audience}")
    private String expectedAudience;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        Function<Jwt, Collection<GrantedAuthority>> authoritiesConverter =
                new JwtRoleConverter(groupsClaim)::convert;

        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(reg -> reg
                        // 회원가입/로그인 등 auth-service의 일부 공개 엔드포인트는 게이트웨이에서도 열어둠
                        .pathMatchers(
                                "/auth/customers/**",
                                "/auth/owners/**",
                                "/auth/health/**"
                        ).permitAll()
                        // 내부 조회 API는 인증 필요(게이트웨이 자체에서 호출하므로 통과됨)
                        .pathMatchers("/internal/**").authenticated()
                        // 그 외는 모두 인증
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(new ReactiveJwtAuthenticationConverterAdapter(
                                        jwtToken -> {
                                            Jwt j = (Jwt) jwtToken;
                                            if (!j.getAudience().contains(expectedAudience)) {
                                                throw new JwtValidationException("Invalid audience", null);
                                            }
                                            return (AbstractAuthenticationToken) authoritiesConverter.apply(j);
                                        }
                                ))
                        )
                );

        return http.build();
    }
}
