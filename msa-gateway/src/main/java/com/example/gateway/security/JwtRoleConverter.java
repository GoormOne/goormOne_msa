package com.example.gateway.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Cognito의 그룹(claim: "cognito:groups")을 ROLE_* 권한으로 변환
 * 예: ["OWNER","CUSTOMER"] -> ["ROLE_OWNER","ROLE_CUSTOMER"]
 */
@Component
public class JwtRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String[] CANDIDATE_CLAIMS = {
            "cognito:groups", "groups", "roles"
    };

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        List<String> groups = extractGroups(jwt);
        if (groups.isEmpty()) return List.of();

        return groups.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.startsWith("ROLE_") ? s : "ROLE_" + s)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableList());
    }

    @SuppressWarnings("unchecked")
    public List<String> extractGroups(Jwt jwt) {
        for (String claim : CANDIDATE_CLAIMS) {
            Object v = jwt.getClaims().get(claim);
            if (v instanceof Collection<?> col) {
                return col.stream().map(String::valueOf).toList();
            } else if (v instanceof String s && !s.isBlank()) {
                // 공백/콤마로 들어오는 변형도 방어
                return Arrays.stream(s.split("[,\\s]+"))
                        .filter(t -> !t.isBlank()).toList();
            }
        }
        return List.of();
    }
}