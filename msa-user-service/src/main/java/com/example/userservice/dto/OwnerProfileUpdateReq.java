package com.example.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class OwnerProfileUpdateReq {

    @NotBlank String name;
    @NotBlank @Email String email;
}
