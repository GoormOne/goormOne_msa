package com.example.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class RegisterRes {
    private UUID userId;
    private String username;
}
