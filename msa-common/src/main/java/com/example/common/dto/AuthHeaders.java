package com.example.common.dto;

public final class AuthHeaders {
    private AuthHeaders() {}

    public static final String X_USER_ID   = "X-User-Id";
    public static final String X_USER_TYPE = "X-User-Type";   // CUSTOMER | OWNER | ADMIN
    public static final String X_USERNAME  = "X-Username";
    public static final String X_EMAIL     = "X-Email";
    public static final String X_GROUPS    = "X-Groups";      // CSV (e.g. CUSTOMER,OWNER)
}
