/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.security.consent;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.profiling.response.BaseResponse;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

@Component
public class ConsentHeaderExtractor {

    public BaseResponse extract(HttpServletRequest http, ConsentRequestMeta out) {
        BaseResponse res = new BaseResponse();

        String auth = ConsentStringUtil.trimToNull(http.getHeader("Authorization"));
        if (auth == null) {
            res.setStatusCode(401);
            res.setDescription("Missing Authorization header");
            return res;
        }

        String tsHeader = ConsentStringUtil.trimToNull(http.getHeader("X-Consent-Ts"));
        if (tsHeader == null) {
            res.setStatusCode(400);
            res.setDescription("Missing X-Consent-Ts");
            return res;
        }

        Long ts;
        try {
            ts = Long.parseLong(tsHeader);
        } catch (NumberFormatException e) {
            res.setStatusCode(400);
            res.setDescription("Invalid X-Consent-Ts");
            return res;
        }

        String deviceId = ConsentStringUtil.trimToNull(http.getHeader("X-Device-Id"));
        if (deviceId == null) {
            res.setStatusCode(400);
            res.setDescription("Missing X-Device-Id");
            return res;
        }

        String kid = ConsentStringUtil.trimToNull(http.getHeader("X-Device-Kid"));
        if (kid == null) {
            res.setStatusCode(400);
            res.setDescription("Missing X-Device-Kid");
            return res;
        }

        String nonce = ConsentStringUtil.trimToNull(http.getHeader("X-Consent-Nonce"));
        if (nonce == null) {
            res.setStatusCode(400);
            res.setDescription("Missing X-Consent-Nonce");
            return res;
        }

        String sigB64 = ConsentStringUtil.trimToNull(http.getHeader("X-Consent-Sig"));
        if (sigB64 == null) {
            res.setStatusCode(400);
            res.setDescription("Missing X-Consent-Sig");
            return res;
        }

        String path = ConsentStringUtil.firstNonBlank(
                http.getHeader("X-Original-Uri"),
                http.getHeader("X-Forwarded-Uri"),
                "/api/profiling" + http.getRequestURI()
        );

        out.setAuthorization(auth);
        out.setTs(ts);
        out.setDeviceId(deviceId);
        out.setKid(kid);
        out.setNonce(nonce);
        out.setSignatureB64(sigB64);
        out.setPath(path);

        res.setStatusCode(200);
        res.setDescription("OK");
        return res;
    }
}
