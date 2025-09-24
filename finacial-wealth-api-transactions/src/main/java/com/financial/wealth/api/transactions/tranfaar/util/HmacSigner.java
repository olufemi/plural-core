/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.tranfaar.util;

/**
 *
 * @author olufemioshin
 */
// HmacSigner.java
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public final class HmacSigner {

    private static final String HMAC_ALG = "HmacSHA256";

    private HmacSigner() {
    }

    public static Map<String, String> makeSignature(String secretKey, String bodyOrNull) {
        String timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        String body = (bodyOrNull == null) ? "" : bodyOrNull;
        String dataToSign = body + "|" + timestamp;

        try {
            Mac mac = Mac.getInstance(HMAC_ALG);
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_ALG));
            byte[] hmac = mac.doFinal(dataToSign.getBytes(StandardCharsets.UTF_8));
            String signatureHex = toHex(hmac);

            Map<String, String> out = new HashMap<>();
            out.put("timestamp", timestamp);
            out.put("signature", signatureHex);
            return out;
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute HMAC signature", e);
        }
    }

    public static String computeSignature(String secret, String timestamp, String rawBody) {
        try {
            String message = timestamp + "." + rawBody;
            Mac mac = Mac.getInstance(HMAC_ALG);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALG));
            byte[] hmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmac);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute HMAC", e);
        }
    }

    public static boolean secureEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private static String toHex(byte[] bytes) {
        char[] out = new char[bytes.length * 2];
        for (int i = 0, j = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            out[j++] = HEX[v >>> 4];
            out[j++] = HEX[v & 0x0F];
        }
        return new String(out);
    }
}
