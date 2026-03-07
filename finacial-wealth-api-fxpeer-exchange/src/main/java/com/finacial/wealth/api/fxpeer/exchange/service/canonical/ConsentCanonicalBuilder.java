/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.service.canonical;

/**
 *
 * @author olufemioshin
 */
public final class ConsentCanonicalBuilder {

    private ConsentCanonicalBuilder() {
    }

    public static String singleLine(String method,
            String path,
            String refId,
            String payloadHashB64,
            long ts,
            String nonce,
            String userId,
            String deviceId,
            String kid) {
        return "v1|CONSENT"
                + "|" + nz(method).trim().toUpperCase()
                + "|" + nz(path).trim()
                + "|refId=" + nz(refId)
                + "|payloadHashB64=" + nz(payloadHashB64)
                + "|ts=" + ts
                + "|nonce=" + nz(nonce)
                + "|userId=" + nz(userId)
                + "|deviceId=" + nz(deviceId)
                + "|kid=" + nz(kid);
    }

    public static String multiLine(String method,
            String path,
            String refId,
            String payloadHashB64,
            long ts,
            String nonce,
            String userId,
            String deviceId,
            String kid) {
        return "v1|CONSENT\n"
                + "|" + nz(method).trim().toUpperCase() + "\n"
                + "|" + nz(path).trim() + "\n"
                + "|refId=" + nz(refId) + "\n"
                + "|payloadHashB64=" + nz(payloadHashB64) + "\n"
                + "|ts=" + ts + "\n"
                + "|nonce=" + nz(nonce) + "\n"
                + "|userId=" + nz(userId) + "\n"
                + "|deviceId=" + nz(deviceId) + "\n"
                + "|kid=" + nz(kid);
    }

    public static String multiLineCrLf(String method,
            String path,
            String refId,
            String payloadHashB64,
            long ts,
            String nonce,
            String userId,
            String deviceId,
            String kid) {
        return "v1|CONSENT\r\n"
                + "|" + nz(method).trim().toUpperCase() + "\r\n"
                + "|" + nz(path).trim() + "\r\n"
                + "|refId=" + nz(refId) + "\r\n"
                + "|payloadHashB64=" + nz(payloadHashB64) + "\r\n"
                + "|ts=" + ts + "\r\n"
                + "|nonce=" + nz(nonce) + "\r\n"
                + "|userId=" + nz(userId) + "\r\n"
                + "|deviceId=" + nz(deviceId) + "\r\n"
                + "|kid=" + nz(kid);
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
