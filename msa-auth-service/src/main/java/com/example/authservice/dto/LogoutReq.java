package com.example.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LogoutReq {
    @NotBlank private String username; // AdminUserGlobalSignOut 용
}
