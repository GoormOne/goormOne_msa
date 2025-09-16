package com.example.storeservice.stock;

/** payload 내부의 type 값 모음 */
public final class StockTypes {
    private StockTypes() {}
    // inbound
    public static final String ORDER_CREATED            = "order.created";
    public static final String PAYMENT_STATUS_CHANGED   = "payment.status.changed";
    public static final String ORDER_STATUS_CHANGED     = "order.status.changed";
    // outbound
    public static final String STOCK_RESERVATION_RESULT = "stock.reservation.result";
    public static final String STOCK_FINALIZED          = "stock.finalized";
    public static final String STOCK_SHORTAGE           = "stock.shortage";
    public static final String STOCK_RESTORED           = "stock.restored";
}