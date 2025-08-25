package com.example.common.exception;


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
    INVALID_UUID(HttpStatus.BAD_REQUEST, 4005, "잘못된 UUID 형식입니다."), // ✅ 추가

    // 유저 관련 오류
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, 4100, "없는 유저입니다."),
    USER_DELETED(HttpStatus.GONE, 4101, "지워진 유저입니다."),
    USER_DUPLICATED(HttpStatus.CONFLICT, 4102, "이미 존재하는 유저입니다."),
    EMAIL_DUPLICATED(HttpStatus.CONFLICT, 4103, "이미 존재하는 이메일입니다."),

    // ✅ 상점 관련 오류
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, 4200, "없는 상점입니다."),
    STORE_DELETED(HttpStatus.GONE, 4201, "지워진 상점입니다."),

    // ✅ 서버 오류
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 5000, "서버 오류"),

    // ✅ 재고 관련 오류
    INVENTORY_NOT_FOUND(HttpStatus.NOT_FOUND, 4300, "재고 정보를 찾을 수 없습니다."),
    OUT_OF_STOCK(HttpStatus.BAD_REQUEST, 4301, "재고가 부족합니다."),
    RESERVED_NOT_ENOUGH(HttpStatus.BAD_REQUEST, 4302, "예약 수량이 부족합니다."),
    CONCURRENCY_CONFLICT(HttpStatus.CONFLICT, 4303, "동시에 처리 요청이 충돌했습니다. 다시 시도해주세요.");

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;

    CommonCode(HttpStatus httpStatus, int code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
