package com.example.authservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {
    private final HttpStatus status;
    private final int code;
    public BusinessException(HttpStatus status, int code, String message) {
        super(message); this.status = status; this.code = code;
    }
}
