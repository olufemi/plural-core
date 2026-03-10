/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.security.consent;

/**
 *
 * @author olufemioshin
 */
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class ConsentHeaderExtractor {

    public ConsentRequestMeta extract(HttpServletRequest http) {
        String authorization = ConsentStringUtil.trimToNull(http.getHeader("Authorization"));
        if (authorization == null) {
            throw new IllegalArgumentException("Missing Authorization header");
        }

        String tsHeader = ConsentStringUtil.trimToNull(http.getHeader("X-Consent-Ts"));
        if (tsHeader == null) {
            throw new IllegalArgumentException("Missing X-Consent-Ts");
        }

        long ts;
        try {
            ts = Long.parseLong(tsHeader);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid X-Consent-Ts");
        }

        String deviceId = ConsentStringUtil.trimToNull(http.getHeader("X-Device-Id"));
        if (deviceId == null) {
            throw new IllegalArgumentException("Missing X-Device-Id");
        }

        String kid = ConsentStringUtil.trimToNull(http.getHeader("X-Device-Kid"));
        if (kid == null) {
            throw new IllegalArgumentException("Missing X-Device-Kid");
        }

        String nonce = ConsentStringUtil.trimToNull(http.getHeader("X-Consent-Nonce"));
        if (nonce == null) {
            throw new IllegalArgumentException("Missing X-Consent-Nonce");
        }

        String signatureB64 = ConsentStringUtil.trimToNull(http.getHeader("X-Consent-Sig"));
        if (signatureB64 == null) {
            throw new IllegalArgumentException("Missing X-Consent-Sig");
        }

        String path = ConsentStringUtil.firstNonBlank(
                http.getHeader("X-Original-Uri"),
                http.getHeader("X-Forwarded-Uri"),
                "/api/fxpeer-exchange" + http.getRequestURI()
        );

        return new ConsentRequestMeta(
                authorization,
                deviceId,
                kid,
                ts,
                nonce,
                signatureB64,
                path
        );
    }
}
