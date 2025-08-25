package com.example.authservice.config;

import com.example.commonservice.security.CommonAuthContextFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

import java.nio.charset.StandardCharsets;

// Security 설정(+ @PreAuthorize 활성화) & 공통 헤더 필터 등록
@Configuration
@EnableMethodSecurity // @PreAuthorize 활성화
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new CommonAuthContextFilter(), AnonymousAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/health/**",
                                "/auth/customers/**",
                                "/auth/owners/**",
                                "/actuator/health")
                        .permitAll()
                        .requestMatchers("/internal/auth/**").permitAll() // 게이트웨이 내부용 조회. (필요 시 추가 보안 적용)
                        .anyRequest().permitAll())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(json401())
                        .accessDeniedHandler(json403())
                );

        return http.build();
    }

    private AuthenticationEntryPoint json401() {
        return (HttpServletRequest req, HttpServletResponse res, AuthenticationException ex) -> {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setCharacterEncoding(StandardCharsets.UTF_8.name());
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.getWriter().write("{\"success\":false,\"code\":4010,\"message\":\"Unauthorized\",\"data\":null}");
        };
    }

    private AccessDeniedHandler json403() {
        return (HttpServletRequest req, HttpServletResponse res, AccessDeniedException ex) -> {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            res.setCharacterEncoding(StandardCharsets.UTF_8.name());
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.getWriter().write("{\"success\":false,\"code\":4030,\"message\":\"Forbidden\",\"data\":null}");
        };
    }
}