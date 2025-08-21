package com.example.userservice.dto;

import lombok.*;

import java.util.UUID;

@Data
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class CustomerProfileRes {

    UUID customerId;
    String username;
    String name;
    String email;
    Boolean isBanned;
    Boolean emailVerified;
}
