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
import java.util.Base64;

public final class TxHashUtil {
    private TxHashUtil() {}

    // Replace with: hash(canonical_tx_fields)
    public static String txHashB64Placeholder(String txId) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(("v1|TX|" + txId).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(d);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
