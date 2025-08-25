package com.example.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginRes {
    private String idToken;
    private String accessToken;
    private String refreshToken;
    private Integer expiresIn;
    private String tokenType;
}
