/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.security.consent;

/**
 *
 * @author olufemioshin
 */
public abstract class BaseJsonConsentHasher<T> implements ConsentPayloadHasher<T> {

    @Override
    public String payloadHashB64(T request) {
        return ConsentHashUtil.sha256Base64(appJsonPayloadString(request));
    }

    @Override
    public String diagnosticCanonicalPayload(T request) {
        return null;
    }
}
