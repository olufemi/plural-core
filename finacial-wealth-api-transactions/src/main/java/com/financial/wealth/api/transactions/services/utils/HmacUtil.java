/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.services.utils;

/**
 *
 * @author olufemioshin
 */
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class HmacUtil {

    private HmacUtil() {
    }

    public static String hmacSha256Base64(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] out = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new IllegalStateException("HMAC failure", e);
        }
    }

    /**
     * Constant-time string compare
     */
    public static boolean secureEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        byte[] x = a.getBytes(StandardCharsets.US_ASCII);
        byte[] y = b.getBytes(StandardCharsets.US_ASCII);
        if (x.length != y.length) {
            return false;
        }
        int r = 0;
        for (int i = 0; i < x.length; i++) {
            r |= x[i] ^ y[i];
        }
        return r == 0;
    }
}
