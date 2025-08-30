package com.example.userservice.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UpdateOwnerReq {
    private String name;
    private String email;
    private String birth;
}
