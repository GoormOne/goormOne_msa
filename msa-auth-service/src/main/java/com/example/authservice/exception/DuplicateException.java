package com.example.authservice.exception;

import org.springframework.http.HttpStatus;

public class DuplicateException extends BusinessException {
    public DuplicateException(HttpStatus status, String message) {
        super(status, 111112, message);
    }
}
