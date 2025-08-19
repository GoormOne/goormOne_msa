package com.example.authservice.dto;

import lombok.*;

import java.util.UUID;

@Data
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class CustomerRegisterRes {

    private UUID customerId;
    private String username;
}
