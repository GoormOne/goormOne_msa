package com.example.storeservice.global;

public enum EventAction {
    CREATED("Created"),
    UPDATED("Updated"),
    DELETED("Deleted");

    private final String suffix;
    EventAction(String suffix) { this.suffix = suffix; }
    public String suffix() { return suffix; }
}