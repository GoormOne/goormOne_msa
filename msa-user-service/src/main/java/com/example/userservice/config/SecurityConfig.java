package com.example.userservice.config;

import com.example.common.auth.CommonAuthContextFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.Assert;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // 공통 모듈의 필터(이미 @Component 등록되어 있다고 가정)
    private final CommonAuthContextFilter commonAuthContextFilter;

    // application.yml에 정의된 값(예: issuer-uri, audience)을 주입받아 사용하려면 @Value로 받아도 됨
    private final String issuerUri   = "https://cognito-idp.<region>.amazonaws.com/<userPoolId>";
    private final String audience    = "<your_app_client_id>"; // Cognito App Client ID

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 헬스/문서 등 공개
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // 회원가입/로그인 등(개발 단계에서 필요하면 열어둠. 실제 인증/로그인은 auth 서비스 담당)
                        .requestMatchers(HttpMethod.POST, "/users/signup/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()

                        // 나머지는 인증 필수
                        .anyRequest().authenticated()
                )
                // 리소스 서버(JWT) 구성 — 게이트웨이 헤더 신뢰하되, 직접 호출 대비 방어차원으로 유지
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.decoder(jwtDecoder()))
                )
                // 공통 인증 컨텍스트 필터를 UsernamePasswordAuthenticationFilter 앞에 배치
                .addFilterBefore(commonAuthContextFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Cognito JWKS 기반 JwtDecoder + audience 검증 추가
     */
    @Bean
    JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = JwtDecoders.fromIssuerLocation(issuerUri);

        OAuth2TokenValidator<Jwt> withIssuer   = JwtValidators.createDefaultWithIssuer(issuerUri);
        OAuth2TokenValidator<Jwt> withAudience = jwt -> {
            // aud 클레임에 our audience가 포함되어야 함
            if (jwt.getAudience() != null && jwt.getAudience().contains(audience)) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "The required audience is missing", null)
            );
        };
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, withAudience));
        return decoder;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}