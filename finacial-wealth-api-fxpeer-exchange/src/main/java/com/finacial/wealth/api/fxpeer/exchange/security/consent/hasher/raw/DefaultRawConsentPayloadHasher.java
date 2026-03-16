/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.security.consent.hasher.raw;

/**
 *
 * @author olufemioshin
 */


import com.finacial.wealth.api.fxpeer.exchange.security.consent.ConsentHashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DefaultRawConsentPayloadHasher implements RawConsentPayloadHasher {

    private static final Logger log =
            LoggerFactory.getLogger(DefaultRawConsentPayloadHasher.class);

    @Override
    public String payloadHashB64(String rawBody) {

        if (rawBody == null) {
            rawBody = "";
        }

        log.info("[CONSENT] rawRequestBody={}", rawBody);

        String hash = ConsentHashUtil.sha256Base64(rawBody);

        log.info("[CONSENT] payloadHashB64={}", hash);

        return hash;
    }
}
