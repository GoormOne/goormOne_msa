package com.example.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class ResolveRes {
    private boolean found;
    private UUID userId;
    private String name;
}
