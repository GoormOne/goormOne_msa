package com.example.gateway.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.List;

// Security 설정
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ReactiveJwtDecoder jwtDecoder; // (issuer-uri 등록 시 자동 빈 생성, 없으면 제거 가능)
    private final JwtRoleConverter jwtRoleConverter;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        // 서블릿용 컨버터를 리액티브로 래핑
        JwtAuthenticationConverter servletJwtConv = new JwtAuthenticationConverter();
        servletJwtConv.setJwtGrantedAuthoritiesConverter(jwtRoleConverter);
        var reactiveJwtConverter = new ReactiveJwtAuthenticationConverterAdapter(servletJwtConv);

        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> {}) // 아래 corsConfigurationSource() 사용
                .authorizeExchange(ex -> ex
                        .pathMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers(
                                "/actuator/**",
                                "/health/**",
                                "/auth/**",
                                "/users/**",
                                "/internal/auth/**",
                                "/stores/**",
                                "/carts/**",
                                "/orders/**",
                                "/payments/**",
                                "/internal/**",
                                "/tosspayment.html/**"
                        ).permitAll()
                        .pathMatchers(
                            "/swagger-ui.html",
                            "/swagger-ui/**",
                            "/webjars/**",
                            "/v3/api-docs",
                            "/v3/api-docs/**",
                            "/v3/api-docs/swagger-config",
                            "/*/v3/api-docs",           // /orders/v3/api-docs 등
                            "/*/v3/api-docs/**"
                        ).permitAll()
                        .pathMatchers("/actuator/**", "/health/**").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> {
                            // 리액티브 컨버터 연결
                            jwt.jwtAuthenticationConverter(reactiveJwtConverter);
                            // 커스텀 디코더를 쓰고 싶으면 아래 라인 활성화
                            // jwt.jwtDecoder(jwtDecoder);
                        })
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration conf = new CorsConfiguration();
        conf.setAllowedOriginPatterns(List.of("*"));
        conf.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        conf.setAllowedHeaders(List.of(
                "Authorization", "Content-Type", "Accept",
                "X-Groups", "X-User-Id", "X-User-Name", "X-User-Roles", "X-Email"
        ));
        conf.setExposedHeaders(List.of(
                "X-Groups", "X-User-Id", "X-User-Name", "X-User-Roles", "X-Email"
        ));
        conf.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", conf);
        return src;
    }
}
