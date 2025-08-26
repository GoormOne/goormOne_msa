package com.example.authservice.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LoginRes {
    private String idToken;
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private String tokenType;
    private String[] groups;
    private String username;
}
