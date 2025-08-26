package com.example.common.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

@Getter
@Builder
public class RequestUser {
    private final UUID id;                         // PK 우선
    private final String username;                 // 참고용
    private final String email;                    // 참고용
    private final String userType;                 // CUSTOMER | OWNER | ADMIN
    private final Collection<? extends GrantedAuthority> authorities;
}
