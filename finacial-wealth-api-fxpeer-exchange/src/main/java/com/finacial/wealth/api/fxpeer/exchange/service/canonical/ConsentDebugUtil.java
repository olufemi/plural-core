/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.service.canonical;

/**
 *
 * @author olufemioshin
 */
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
public final class ConsentDebugUtil {

    private ConsentDebugUtil() {
    }

    public static void logConsentInputs(String prefix,
                                        String method,
                                        String path,
                                        String refId,
                                        String payloadHashB64,
                                        long ts,
                                        String nonce,
                                        String userId,
                                        String deviceId,
                                        String kid) {

        log.info("[CONSENT] {} method='{}'", prefix, safe(method));
        log.info("[CONSENT] {} path='{}'", prefix, safe(path));
        log.info("[CONSENT] {} refId='{}'", prefix, safe(refId));
        log.info("[CONSENT] {} payloadHashB64='{}'", prefix, safe(payloadHashB64));
        log.info("[CONSENT] {} ts='{}'", prefix, ts);
        log.info("[CONSENT] {} nonce='{}'", prefix, safe(nonce));
        log.info("[CONSENT] {} userId='{}'", prefix, safe(userId));
        log.info("[CONSENT] {} deviceId='{}'", prefix, safe(deviceId));
        log.info("[CONSENT] {} kid='{}'", prefix, safe(kid));
    }

    public static void logCanonical(String prefix, String canonical) {
        byte[] bytes = canonical.getBytes(StandardCharsets.UTF_8);
        log.info("[CONSENT] {} canonical='{}'", prefix, canonical);
        log.info("[CONSENT] {} canonical.length={}", prefix, canonical.length());
        log.info("[CONSENT] {} canonical.bytes.b64={}", prefix, Base64.getEncoder().encodeToString(bytes));
        log.info("[CONSENT] {} canonical.bytes.hex={}", prefix, toHex(bytes));
    }

    private static String safe(String s) {
        return s == null ? "<null>" : s;
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
