package com.example.authservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ServiceException extends RuntimeException {
    private final HttpStatus status;
    private final int code;
    public ServiceException(HttpStatus status, int code, String message) {
        super(message); this.status = status; this.code = code;
    }
    public static ServiceException duplicatedUsername() {
        return new ServiceException(HttpStatus.CONFLICT, 4100, "이미 사용 중인 username입니다.");
    }
    public static ServiceException duplicatedEmail() {
        return new ServiceException(HttpStatus.CONFLICT, 4101, "이미 사용 중인 email입니다.");
    }
}
