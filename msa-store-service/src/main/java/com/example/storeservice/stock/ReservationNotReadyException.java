package com.example.storeservice.stock;

/** order.created 미처리/순서역전 등으로 예약 장부가 비어있는 경우 재시도를 유도하기 위한 예외 */
public class ReservationNotReadyException extends RuntimeException {
    public ReservationNotReadyException(String msg) { super(msg); }
}
