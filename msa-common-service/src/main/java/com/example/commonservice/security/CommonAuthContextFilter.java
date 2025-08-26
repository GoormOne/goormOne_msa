package com.example.commonservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/*
* GW 뒤 서비스용: 헤더 -> Authentication 주입 필터
* GW가 실어준 헤더만으로 SecurityContext 구성
* 각 MS는 JWT 없이도 @PreAuthorize 사용 가능
* */
@Slf4j
public class CommonAuthContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String userId = request.getHeader(HeaderNames.X_USER_ID);
        String roles = request.getHeader(HeaderNames.X_USER_ROLES);

        if (StringUtils.hasText(userId) && StringUtils.hasText(roles)) {
            List<SimpleGrantedAuthority> authorities = Arrays.stream(roles.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            AbstractAuthenticationToken auth = new AbstractAuthenticationToken(authorities) {
                @Override
                public Object getCredentials() { return "N/A"; }

                @Override
                public Object getPrincipal() { return userId; }
            };
            auth.setAuthenticated(true);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}
