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
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class HmacSigner {

    private static final String HMAC_ALG = "HmacSHA256";

    private HmacSigner() {
    }

    public static void testSignatureVariants(String secretKey, String apiKey, String body, String timestamp, String expected) throws Exception {
        List<String> variants = Arrays.asList(
                body + "|" + timestamp,
                timestamp + "|" + body,
                body + timestamp,
                timestamp + body,
                apiKey + "|" + body + "|" + timestamp,
                body + "|" + timestamp + "|" + apiKey,
                apiKey + body + timestamp,
                timestamp + "|" + apiKey + "|" + body
        );

        System.out.println("BEGINS ::::: ::::: ::::: ");

        for (String dataToSign : variants) {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hmac = mac.doFinal(dataToSign.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hmac) {
                sb.append(String.format("%02x", b));
            }

            String sig = sb.toString();

            System.out.println("DATA_TO_SIGN ::: " + dataToSign);
            System.out.println("SIG          ::: " + sig);
            System.out.println("MATCH        ::: " + expected.equals(sig));
            System.out.println("-------------------------------------------");
        }

        System.out.println("ENDS ::::: ::::: ::::: ");
    }

    public static Map<String, String> makeSignature(String secretKey, String bodyOrNull, String timestamp) {
        String body = (bodyOrNull == null) ? "" : bodyOrNull;
        String dataToSign = body + "|" + timestamp;
        System.out.println("DATA_TO_SIGN ::::: " + dataToSign);

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

    public static String makeSignatureOnly(String secretKey, String body, String timestamp) {
        try {
            String dataToSign = body + "|" + timestamp; // try current assumption first
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hmac = mac.doFinal(dataToSign.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hmac) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, String> makeSignature(String secretKey, String bodyOrNull) {
        String timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        return makeSignature(secretKey, bodyOrNull, timestamp);
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
