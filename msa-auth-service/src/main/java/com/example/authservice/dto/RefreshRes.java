package com.example.authservice.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class RefreshRes {
    private String idToken;
    private String accessToken;
    private Long   expiresIn;
    private String tokenType;
}
