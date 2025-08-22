package com.example.gateway.security;

import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;
import java.util.stream.Collectors;

// JWT -> ROLE 변환
@RequiredArgsConstructor
public class JwtRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final String groupsClaim;

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Object raw = jwt.getClaim(groupsClaim);
        if (raw instanceof Collection<?> col) {
            return col.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .map(g -> g.startsWith("ROLE_") ? g : "ROLE_" + g)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet());
        }
        return List.of();
    }
}
