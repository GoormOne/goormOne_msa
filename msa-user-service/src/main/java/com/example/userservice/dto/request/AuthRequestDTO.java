package com.example.userservice.dto.request;

import com.example.common.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

public class AuthRequestDTO {

    @Getter
    @Setter
    public static class RegisterRequestDto {
        @NotBlank
        private String userId;
        
        @NotBlank
        private String username;
        
        @NotBlank
        private String password;
        
        @NotBlank
        private String name;
        
        private Date birth;
        
        @Email
        private String email;
        
        private Role role;
    }

    @Getter
    @Setter
    public static class LoginRequestDto {
        @NotBlank
        private String userId;
        
        @NotBlank
        private String password;
    }

    @Getter
    @Setter
    public static class PasswordChangeDto {
        @NotBlank
        private String currentPassword;
        
        @NotBlank
        private String newPassword;
    }
}