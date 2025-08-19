package com.example.authservice.dto;

import lombok.*;

import java.util.UUID;

@Data
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class OwnerRegisterRes {

    private UUID ownerId;
    private String username;
}
