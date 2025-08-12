package com.example.userservice.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.profect.delivery.global.entity.Role;
import com.profect.delivery.global.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data//getter setter
@NoArgsConstructor//유효성 검사 해야함

public class UserResponseDto {

    private String username;
    private String password;
    private String name;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private Date birth;
    private String email;
    private Role role;
    private boolean is_public;

    public static UserResponseDto fromEntity(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.username=user.getUsername();
        dto.password=user.getPassword();
        dto.name=user.getName();
        dto.birth=user.getBirth();
        dto.email=user.getEmail();
        dto.role=user.getRole();
        dto.is_public=user.isPublic();
        return dto;
    }


}