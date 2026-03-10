/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.security.consent;

/**
 *
 * @author olufemioshin
 */
public interface ConsentPayloadHasher<T> {

    String appJsonPayloadString(T request);

    default String diagnosticCanonicalPayload(T request) {
        return null;
    }

    default String payloadHashB64(T request) {
        return ConsentHashUtil.sha256Base64(appJsonPayloadString(request));
    }
}
