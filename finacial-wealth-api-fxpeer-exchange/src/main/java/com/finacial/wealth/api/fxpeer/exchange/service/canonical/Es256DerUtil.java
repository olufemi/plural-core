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
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public final class Es256DerUtil {

    private Es256DerUtil() {
    }

    public static boolean verifySpkiB64DerSigB64(String publicSpkiB64, String message, String sigDerB64) {
        try {
            byte[] spkiDer = Base64.getDecoder().decode(stripWs(publicSpkiB64));
            PublicKey pub = KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(spkiDer));

            Signature s = Signature.getInstance("SHA256withECDSA");
            s.initVerify(pub);
            s.update(message.getBytes(StandardCharsets.UTF_8));

            byte[] sigDer = Base64.getDecoder().decode(stripWs(sigDerB64));
            return s.verify(sigDer);
        } catch (Exception e) {
            return false;
        }
    }

    private static String stripWs(String s) {
        return s == null ? "" : s.replaceAll("\\s", "");
    }
}
