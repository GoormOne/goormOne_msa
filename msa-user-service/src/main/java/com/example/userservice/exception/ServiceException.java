package com.example.userservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ServiceException extends RuntimeException {
    private final HttpStatus status;
    private final int code;
    public ServiceException(HttpStatus status, int code, String message) {
        super(message); this.status = status; this.code = code;
    }
    public static ServiceException notFound() {
        return new ServiceException(HttpStatus.CONFLICT, 4102, "사용자를 찾을 수 없습니다.");
    }
    public static ServiceException duplicatedEmail() {
        return new ServiceException(HttpStatus.CONFLICT, 4103, "이미 사용 중인 email입니다.");
    }
}
