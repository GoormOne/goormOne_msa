package com.example.storeservice.global.exception;

public class StoreAlreadyDeletedException extends RuntimeException {
    public StoreAlreadyDeletedException(String message) {
        super(message);
    }
}
