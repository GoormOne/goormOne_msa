package com.example.userservice.dto;

import com.example.userservice.entity.Role;

public record UserInfoDto(String username, Role role, String name, String email) {}