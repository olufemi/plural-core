/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange;

/**
 *
 * @author olufemioshin
 */


import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class PublicKeyDebug {

    public static void main(String[] args) throws Exception {

        String dbSpkiB64 = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE1qjn/ssXcwHt+HGDajAmc66f7oVcPHHH3DTODsgYuJ1jRTGoM9EIcTahpREdUgR4WUnmpR70i396vjuEVdCtTw==";

        PublicKey dbKey = parsePublicKey(dbSpkiB64);

        System.out.println("dbKey.algorithm = " + dbKey.getAlgorithm());
        System.out.println("dbKey.format = " + dbKey.getFormat());
        System.out.println("dbKey.sha256 = " + sha256B64(Base64.getDecoder().decode(dbSpkiB64)));
    }

    private static PublicKey parsePublicKey(String publicSpkiB64) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(publicSpkiB64);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("EC").generatePublic(spec);
    }

    private static String sha256B64(byte[] input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return Base64.getEncoder().encodeToString(md.digest(input));
    }
}
