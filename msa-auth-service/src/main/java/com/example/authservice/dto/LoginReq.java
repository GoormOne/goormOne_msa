package com.example.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class LoginReq {
    @NotBlank @Size(min = 4, max = 10)
    private String username;

    @NotBlank @Size(min = 8, max = 15)
    private String password;
}