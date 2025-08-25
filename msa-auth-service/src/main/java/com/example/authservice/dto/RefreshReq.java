package com.example.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class RefreshReq {
    @NotBlank private String username;
    @NotBlank private String refreshToken;
}
