/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.service.canonical;

/**
 *
 * @author olufemioshin
 */
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

public final class ConsentPayloadHashUtil {
    private ConsentPayloadHashUtil() {}

    public static String payloadHashB64(String action, String refId, Map<String, Object> payload) {
        String canonical = canonicalPayloadV1(action, refId, payload);
        byte[] sha = sha256(canonical.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(sha);
    }

    static String canonicalPayloadV1(String action, String refId, Map<String, Object> payload) {
        // Stable ordering
        TreeMap<String, Object> tm = new TreeMap<>();
        if (payload != null) tm.putAll(payload);

        StringBuilder sb = new StringBuilder();
        sb.append("v1|").append(action).append("|").append(refId == null ? "" : refId);

        for (Map.Entry<String, Object> e : tm.entrySet()) {
            sb.append("|").append(e.getKey()).append("=").append(String.valueOf(e.getValue()));
        }
        return sb.toString();
    }

    static byte[] sha256(byte[] in) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(in);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
