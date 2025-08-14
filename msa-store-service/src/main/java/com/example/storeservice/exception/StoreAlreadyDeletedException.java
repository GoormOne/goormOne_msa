package com.example.storeservice.exception;

public class StoreAlreadyDeletedException extends RuntimeException {
    public StoreAlreadyDeletedException(String message) {
        super(message);
    }
}
