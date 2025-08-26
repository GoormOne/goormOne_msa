package com.example.userservice.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MyProfileRes {
    private String id;
    private String username;
    private String name;
    private String email;
    private String birth;
    private boolean emailVerified;
    private boolean banned;
}
