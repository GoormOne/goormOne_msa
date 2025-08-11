package com.example.common;


import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CommonCode {

    // ✅ 성공 코드
    SUCCESS(HttpStatus.OK, 2000, "성공"),

    // ✅ 클라이언트 오류
    BAD_REQUEST(HttpStatus.BAD_REQUEST, 4000, "잘못된 요청"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, 4001, "인증 실패"),
    FORBIDDEN(HttpStatus.FORBIDDEN, 4003, "권한 없음"),
    NOT_FOUND(HttpStatus.NOT_FOUND, 4004, "리소스를 찾을 수 없음"),

    // ✅ 서버 오류
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 5000, "서버 오류");

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;

    CommonCode(HttpStatus httpStatus, int code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}