/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.security.consent;

/**
 *
 * @author olufemioshin
 */
public record ConsentRequestMeta(
        String authorization,
        String deviceId,
        String kid,
        long ts,
        String nonce,
        String signatureB64,
        String path
) {
}
