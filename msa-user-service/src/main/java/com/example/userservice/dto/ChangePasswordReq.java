package com.example.userservice.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ChangePasswordReq {
    private String currentPassword;
    private String newPassword;
}
