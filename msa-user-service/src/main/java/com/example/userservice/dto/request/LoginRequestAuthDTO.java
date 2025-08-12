package com.example.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestAuthDTO {
    @NotBlank
    private String userId;
    
    @NotBlank
    private String password;
}