package com.example.authservice.exception;

import org.springframework.http.HttpStatus;

public class CognitoCreateException extends BusinessException {
    public CognitoCreateException(HttpStatus status, String message) {
        super(status, 11111, message);
    }
}