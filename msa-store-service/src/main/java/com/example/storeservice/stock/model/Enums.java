package com.example.storeservice.stock.model;

public final class Enums {
    private Enums() {}
    public enum PaymentStatus { PENDING, PAID, FAILED, REFUNDED }
    public enum OrderStatus   { PENDING, CONFIRMED, CANCELED }
}
