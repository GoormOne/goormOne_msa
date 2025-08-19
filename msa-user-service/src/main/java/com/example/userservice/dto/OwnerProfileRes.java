package com.example.userservice.dto;

import lombok.*;

import java.util.UUID;

@Data
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class OwnerProfileRes {

    UUID ownerId;
    String username;
    String name;
    String email;
    Boolean isBanned;
    Boolean emailVerified;
}
