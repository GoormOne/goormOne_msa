package com.example.userservice.dto.response;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserUpdateResponseDto {
    private boolean isSuccess;
    private Object data;
    private String message;
    private Object error;

}
