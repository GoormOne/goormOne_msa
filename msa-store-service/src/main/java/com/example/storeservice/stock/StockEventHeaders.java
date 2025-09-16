package com.example.storeservice.stock;

public final class StockEventHeaders {
    private StockEventHeaders() {}
    public static final String X_EVENT_TYPE     = "x-event-type";
    public static final String X_EVENT_VERSION  = "x-event-version";
    public static final String X_CORRELATION_ID = "x-correlation-id";
    public static final String X_CAUSATION_ID   = "x-causation-id";
    public static final String X_PRODUCER       = "x-producer";
}