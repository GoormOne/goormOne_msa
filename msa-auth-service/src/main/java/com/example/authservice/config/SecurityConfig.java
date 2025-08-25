package com.example.authservice.config;

import com.example.commonservice.security.CommonAuthContextFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

// Security 설정(+ @PreAuthorize 활성화) & 공통 헤더 필터 등록
@Configuration
@EnableMethodSecurity // @PreAuthorize 활성화
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable());

        http.addFilterBefore(new CommonAuthContextFilter(), org.springframework.security.web.authentication.AnonymousAuthenticationFilter.class);

        http.authorizeHttpRequests(reg -> reg
                .requestMatchers(
                        "/auth/customers/register",
                        "/auth/owners/register",
                        "/auth/customers/login",
                        "/auth/owners/login",
                        "/auth/health/",
                        "/actuator/health"
                ).permitAll()
                .requestMatchers("/internal/auth/**").permitAll() // 게이트웨이 내부용 조회. (필요 시 추가 보안 적용)
                .anyRequest().permitAll()
        );

        return http.build();
    }
}