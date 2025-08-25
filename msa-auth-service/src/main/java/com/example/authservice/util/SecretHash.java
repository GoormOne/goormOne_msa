package com.example.authservice.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SecretHash {
    public static String calculate(String username, String appClientId, String appClientSecret) {
        if (appClientSecret == null || appClientSecret.isEmpty()) return null;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec key = new SecretKeySpec(appClientSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(key);
            mac.update(username.getBytes(StandardCharsets.UTF_8));
            byte[] raw = mac.doFinal(appClientId.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(raw);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to calculate SecretHash", e);
        }
    }
}