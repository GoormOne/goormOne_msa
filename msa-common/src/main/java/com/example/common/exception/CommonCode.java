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
    STORE_AUTH_FAIL(HttpStatus.FORBIDDEN, 4202, "해당 매장의 소유자가 아닙니다."),

    // ✅ 서버 오류
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 5000, "서버 오류"),

    // ✅ 재고 관련 오류
    INVENTORY_NOT_FOUND(HttpStatus.NOT_FOUND, 4300, "재고 정보를 찾을 수 없습니다."),
    OUT_OF_STOCK(HttpStatus.BAD_REQUEST, 4301, "재고가 부족합니다."),
    RESERVED_NOT_ENOUGH(HttpStatus.BAD_REQUEST, 4302, "예약 수량이 부족합니다."),
    CONCURRENCY_CONFLICT(HttpStatus.CONFLICT, 4303, "동시에 처리 요청이 충돌했습니다. 다시 시도해주세요."),
    RESERVED_FAIL_RETRY(HttpStatus.CONFLICT, 4304, "재고 예약에 실패했습니다. 다시 시도해주세요."),

    // 장바구니 관련 오류
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, 4400, "장바구니를 찾을 수 없습니다."),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, 4401, "장바구니 항목을 찾을 수 없습니다."),
    CART_INVALID_QUANTITY(HttpStatus.BAD_REQUEST, 4402, "유효하지 않은 수량입니다."),
    CART_CONFLICT(HttpStatus.CONFLICT, 4403, "장바구니 처리 중 충돌이 발생했습니다."),
    CART_ALREADY(HttpStatus.CONFLICT, 4404, "이미 장바구니에 담긴 메뉴입니다."),
    CART_ITEM_ID_FAIL(HttpStatus.NOT_FOUND, 4405, "해당 상품ID는 고객님의 장바구니에 존재하지 않습니다."),
    CART_ITEM_QUANTITY(HttpStatus.BAD_REQUEST, 4406, "최소 수량은 1개 이상입니다."),
    CART_CREATE(HttpStatus.CREATED, 4407, "장바구니 생성 완료"),
    CART_SEARCH_COMPLETE(HttpStatus.ACCEPTED, 4408, "장바구니 조회 완료"),
    CART_ITEM_CLEAR(HttpStatus.ACCEPTED, 4409, "장바구니 비우기 완료"),
    CART_ITEM_DELETE(HttpStatus.ACCEPTED, 4410, "선택 항목 삭제 완료"),
    CART_DELETE(HttpStatus.ACCEPTED, 4411, "장바구니 삭제 완료"),
    CART_ITEM_INCREASE(HttpStatus.ACCEPTED, 4412, "상품 수량 증가"),
    CART_ITEM_DECREASE(HttpStatus.ACCEPTED, 4413, "상품 수량 감소"),

    // 주문 관련 오류
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, 4500, "주문을 찾을 수 없습니다."),
    ORDER_CANCEL_FAIL(HttpStatus.BAD_REQUEST, 4501, "현재 상태에서는 취소할 수 없습니다."),
    ORDER_CREATE(HttpStatus.ACCEPTED,4502, "주문 생성이 완료되었습니다."),
    ORDER_SEARCH(HttpStatus.ACCEPTED, 4503, "주문 조회가 완료되었습니다."),
    ORDER_UPDATE(HttpStatus.ACCEPTED, 4504, "주문 상태 업데이트 완료."),
    ORDER_CANCEL(HttpStatus.ACCEPTED, 4505, "주문 취소 완료."),

    // 결제 관련
    PAYMENT_COMPLETE(HttpStatus.ACCEPTED, 4600, "결제가 정상처리 되었습니다."),
    PAYMENT_FAILED(HttpStatus.CONFLICT, 4601, "결제를 실패하였습니다."),
    PAYMENT_SUCCESS(HttpStatus.ACCEPTED, 4602, "tossPayment 통신 정상"),
    PAYMENT_SEARCH_SUCCESS(HttpStatus.ACCEPTED, 4603, "조회 완료"),
    PAYMENT_SEARCH_FAILED(HttpStatus.CONFLICT, 4603, "조회 실패"),
    PAYMENT_CANCEL_SUCCESS(HttpStatus.ACCEPTED, 4604, "결제 취소 성공");

    private final HttpStatus httpStatus;
    private final int code;
    private final String message;

    CommonCode(HttpStatus httpStatus, int code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
