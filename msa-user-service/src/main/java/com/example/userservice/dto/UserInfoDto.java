package com.example.userservice.dto;

import com.profect.delivery.global.entity.Role;

public record UserInfoDto(String username, Role role, String name, String email) {}