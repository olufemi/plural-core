/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.service.canonical;

/**
 *
 * @author olufemioshin
 */
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

@Service
@RequiredArgsConstructor
public class KeyMaterialService {

    private final KeyMaterialCryptoService crypto;
    
    public PrivateKey loadPrivateKey(String encryptedPkcs8B64) {
    String trace = java.util.UUID.randomUUID().toString().substring(0, 8);

    try {
        System.out.println("[receipt][" + trace + "] loadPrivateKey inputLen=" + (encryptedPkcs8B64 == null ? 0 : encryptedPkcs8B64.length())
                + " head=" + safeHead(encryptedPkcs8B64));

        byte[] pkcs8 = crypto.decrypt(encryptedPkcs8B64);

        System.out.println("[receipt][" + trace + "] decrypt OK bytes=" + pkcs8.length
                + " firstByte=" + (pkcs8.length == 0 ? "EMPTY" : String.format("0x%02X", pkcs8[0]))
                + " sha256=" + sha256Hex(pkcs8));

        KeyFactory kf = KeyFactory.getInstance("EC");
        PrivateKey pk = kf.generatePrivate(new PKCS8EncodedKeySpec(pkcs8));

        System.out.println("[receipt][" + trace + "] PKCS8 parse OK alg=" + pk.getAlgorithm());
        return pk;

    } catch (Exception e) {
        System.out.println("[receipt][" + trace + "] ERROR type=" + e.getClass().getName() + " msg=" + e.getMessage());

        Throwable c = e.getCause();
        if (c != null) {
            System.out.println("[receipt][" + trace + "] CAUSE type=" + c.getClass().getName() + " msg=" + c.getMessage());
        }
        throw new IllegalStateException("Failed to load EC private key", e);
    }
}

private String safeHead(String s) {
    if (s == null) return "null";
    String t = s.replaceAll("\\s", ""); // only for logging head safely
    return t.substring(0, Math.min(16, t.length()));
}

private String sha256Hex(byte[] b) {
    try {
        var md = java.security.MessageDigest.getInstance("SHA-256");
        byte[] d = md.digest(b);
        StringBuilder sb = new StringBuilder();
        for (byte x : d) sb.append(String.format("%02x", x));
        return sb.toString();
    } catch (Exception ex) {
        return "na";
    }
}

   /* public PrivateKey loadPrivateKey(String encryptedPkcs8B64) {
        try {
            byte[] pkcs8 = crypto.decrypt(encryptedPkcs8B64);
            KeyFactory kf = KeyFactory.getInstance("EC");
            return kf.generatePrivate(new PKCS8EncodedKeySpec(pkcs8));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load EC private key", e);
        }
    }*/
}
