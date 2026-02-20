/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.service.canonical;

/**
 *
 * @author olufemioshin
 */
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class KeyMaterialCryptoService {

    private static final int GCM_TAG_BITS = 128;
    private static final int IV_LEN = 12;

    private final SecretKeySpec masterKey;
    private final SecureRandom random = new SecureRandom();

    public KeyMaterialCryptoService(@Value("${receipt.masterKeyB64}") String masterKeyB64) {
        byte[] key = Base64.getDecoder().decode(masterKeyB64);
        if (key.length != 32) {
            throw new IllegalArgumentException("receipt.masterKeyB64 must be 32 bytes base64");
        }
        this.masterKey = new SecretKeySpec(key, "AES");
        
          System.out.println("[receipt] KeyMaterialCryptoService initialized. masterKeyLen="
                + key.length + " sha256=" + sha256Hex(key));

        System.out.println("[receipt] KeyMaterialCryptoService initialized. masterKeyLen="
                + key.length + " sha256=" + sha256Hex(key));
    }

    private String sha256Hex(byte[] b) {
        try {
            var md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] d = md.digest(b);
            StringBuilder sb = new StringBuilder();
            for (byte x : d) {
                sb.append(String.format("%02x", x));
            }
            return sb.toString();
        } catch (Exception e) {
            return "na";
        }
    }

    public String encrypt(byte[] plain) {
        try {
            byte[] iv = new byte[IV_LEN];
            random.nextBytes(iv);

            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.ENCRYPT_MODE, masterKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] ct = c.doFinal(plain);

            byte[] out = new byte[iv.length + ct.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(ct, 0, out, iv.length, ct.length);

            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            throw new IllegalStateException("Encrypt failed", e);
        }
    }

    /*public byte[] decrypt(String encB64) {
        try {
            byte[] in = Base64.getDecoder().decode(encB64);
            if (in.length < IV_LEN + 16) {
                throw new IllegalArgumentException("Invalid encrypted data");
            }

            byte[] iv = new byte[IV_LEN];
            byte[] ct = new byte[in.length - IV_LEN];
            System.arraycopy(in, 0, iv, 0, IV_LEN);
            System.arraycopy(in, IV_LEN, ct, 0, ct.length);

            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.DECRYPT_MODE, masterKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            return c.doFinal(ct);
        } catch (Exception e) {
            throw new IllegalStateException("Decrypt failed", e);
        }
    }*/
    public byte[] decrypt(String encB64) {
        String trace = java.util.UUID.randomUUID().toString().substring(0, 8);
        try {
            if (encB64 == null) {
                throw new IllegalArgumentException("encB64 is null");
            }

            String cleaned = encB64.replaceAll("\\s", "");
            System.out.println("[receipt][" + trace + "] decrypt inputLen=" + encB64.length()
                    + " cleanedLen=" + cleaned.length()
                    + " head=" + cleaned.substring(0, Math.min(16, cleaned.length())));

            byte[] in;
            try {
                in = Base64.getDecoder().decode(cleaned);
            } catch (IllegalArgumentException badB64) {
                System.out.println("[receipt][" + trace + "] Base64 decode FAILED: " + badB64.getMessage());
                throw badB64;
            }

            System.out.println("[receipt][" + trace + "] Base64 decode OK bytes=" + in.length);

            if (in.length < IV_LEN + 16) {
                throw new IllegalArgumentException("Invalid encrypted data (too short): " + in.length);
            }

            byte[] iv = new byte[IV_LEN];
            byte[] ct = new byte[in.length - IV_LEN];
            System.arraycopy(in, 0, iv, 0, IV_LEN);
            System.arraycopy(in, IV_LEN, ct, 0, ct.length);

            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.DECRYPT_MODE, masterKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            try {
                return c.doFinal(ct);
            } catch (javax.crypto.AEADBadTagException badTag) {
                System.out.println("[receipt][" + trace + "] AES/GCM BAD TAG (wrong master key or corrupted ciphertext)");
                throw badTag;
            }

        } catch (Exception e) {
            System.out.println("[receipt][" + trace + "] decrypt ERROR type=" + e.getClass().getName() + " msg=" + e.getMessage());
            throw new IllegalStateException("Decrypt failed", e);
        }
    }
}
