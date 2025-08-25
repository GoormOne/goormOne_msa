package com.example.authservice.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Data
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class RegisterOwnerReq {

    @NotBlank @Size(min = 4, max = 10)
    @Pattern(
            regexp = "^[a-z0-9]+$",
            message = "username은 소문자/숫자만 허용"
    )
    private String username;

    @NotBlank @Size(min = 8, max = 15)
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).+$",
            message = "password는 대소문자/숫자/특수문자 포함"
    )
    private String password;

    @NotBlank @Size(max = 30)
    private String name;

    @NotNull
    private LocalDate birth;

    @NotBlank @Email @Size(max = 30)
    private String email;
}
