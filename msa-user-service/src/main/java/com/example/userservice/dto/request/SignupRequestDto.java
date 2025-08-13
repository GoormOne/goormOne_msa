package com.example.userservice.dto.request;

import com.example.userservice.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SignupRequestDto {

    @NotBlank private String username;
    @NotBlank private String password;
    @NotBlank private String name;
    @NotNull @Past private LocalDate birth;
    @NotBlank @Email private String email;

    public User toEntity() {
        return User.builder()
                // id는 넣지 않음 (JPA/DB가 생성)
                .username(this.username)
                .password(this.password) // pw encoding은 Service에서
                .name(this.name)
                .birth(this.birth)
                .email(this.email)
                .isBanned(false)
                .build();
    }
}