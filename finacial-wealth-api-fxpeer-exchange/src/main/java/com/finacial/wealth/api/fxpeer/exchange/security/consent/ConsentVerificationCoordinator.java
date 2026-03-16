/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.security.consent;

import com.finacial.wealth.api.fxpeer.exchange.model.BaseResponse;
import com.finacial.wealth.api.fxpeer.exchange.security.consent.hasher.raw.ConsentRawBodyUtil;
import com.finacial.wealth.api.fxpeer.exchange.security.consent.hasher.raw.RawConsentPayloadHasher;
import com.finacial.wealth.api.fxpeer.exchange.service.canonical.ConsentVerifierService;
import jakarta.servlet.http.HttpServletRequest;
import om.finacial.wealth.api.fxpeer.exchange.service.canonical.model.ConsentVerifyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 *
 * @author olufemioshin
 */
@Service
public class ConsentVerificationCoordinator {

    private static final Logger log = LoggerFactory.getLogger(ConsentVerificationCoordinator.class);

    private final ConsentHeaderExtractor consentHeaderExtractor;
    private final ConsentVerifierService consentVerifierService;

    public ConsentVerificationCoordinator(ConsentHeaderExtractor consentHeaderExtractor,
            ConsentVerifierService consentVerifierService) {
        this.consentHeaderExtractor = consentHeaderExtractor;
        this.consentVerifierService = consentVerifierService;
    }

    public <T> BaseResponse requireConsent(HttpServletRequest http,
            String method,
            String refId,
            String userId,
            T request,
            ConsentPayloadHasher<T> payloadHasher) {

        if (request == null) {
            BaseResponse res = new BaseResponse();
            res.setStatusCode(400);
            res.setDescription("Missing request payload for consent verification");
            return res;
        }

        final ConsentRequestMeta meta;
        try {
            meta = consentHeaderExtractor.extract(http);
        } catch (IllegalArgumentException e) {
            BaseResponse res = new BaseResponse();
            res.setStatusCode(400);
            res.setDescription(e.getMessage());
            return res;
        }

        try {
            String diagnostic = payloadHasher.diagnosticCanonicalPayload(request);
            if (diagnostic != null) {
                log.info("[CONSENT] diagnosticCanonical=\n{}", diagnostic);
            }

            String json = payloadHasher.appJsonPayloadString(request);
            String payloadHashB64 = payloadHasher.payloadHashB64(request);

            log.info("[CONSENT] appJsonPayloadString={}", json);
            log.info("[CONSENT] verifyPayloadHashB64={}", payloadHashB64);

            ConsentVerifyRequest body = new ConsentVerifyRequest(
                    method,
                    meta.path(),
                    refId,
                    payloadHashB64,
                    userId
            );

            log.info("[CONSENT] calling local verifier body={}", body);

            BaseResponse verifyRes = consentVerifierService.verifyOrThrow(
                    body,
                    meta.deviceId(),
                    meta.kid(),
                    meta.ts(),
                    meta.nonce(),
                    meta.signatureB64()
            );

            if (verifyRes == null) {
                BaseResponse res = new BaseResponse();
                res.setStatusCode(502);
                res.setDescription("Consent verification returned no response");
                return res;
            }

            log.info("[CONSENT] verifyRes statusCode={} description={}",
                    verifyRes.getStatusCode(),
                    verifyRes.getDescription());

            return copyResponse(verifyRes);

        } catch (Exception e) {
            log.error("[CONSENT] local verify call failed", e);
            BaseResponse res = new BaseResponse();
            res.setStatusCode(502);
            res.setDescription("Consent verification service unavailable");
            return res;
        }
    }

    public BaseResponse requireConsentUsingRawBody(HttpServletRequest http,
            String method,
            String refId,
            String userId,
            RawConsentPayloadHasher payloadHasher) {

        BaseResponse res = new BaseResponse();

        final ConsentRequestMeta meta;
        try {
            meta = consentHeaderExtractor.extract(http);
        } catch (IllegalArgumentException e) {
            res.setStatusCode(400);
            res.setDescription(e.getMessage());
            return res;
        }

        final String rawBody;
        try {
            rawBody = ConsentRawBodyUtil.getRawRequestBody(http);
        } catch (Exception e) {
            log.error("[CONSENT] unable to read raw request body", e);
            res.setStatusCode(500);
            res.setDescription("Unable to read raw request body");
            return res;
        }

        log.info("[CONSENT] headers deviceId={} kid={} ts={} nonce={} sig.len={}",
                meta.deviceId(),
                meta.kid(),
                meta.ts(),
                meta.nonce(),
                meta.signatureB64() == null ? 0 : meta.signatureB64().length());

        log.info("[CONSENT] rawRequestBody={}", rawBody);

        final String payloadHashB64;
        try {
            payloadHashB64 = payloadHasher.payloadHashB64(rawBody);
        } catch (Exception e) {
            log.error("[CONSENT] raw payload hash generation failed", e);
            res.setStatusCode(500);
            res.setDescription("Unable to compute payload hash");
            return res;
        }

        log.info("[CONSENT] verifyPayloadHashB64={}", payloadHashB64);

        ConsentVerifyRequest body = new ConsentVerifyRequest(
                method,
                meta.path(),
                refId,
                payloadHashB64,
                userId
        );

        log.info("[CONSENT] calling local verifier body={}", body);

        try {
            BaseResponse verifyRes = consentVerifierService.verifyOrThrow(
                    body,
                    meta.deviceId(),
                    meta.kid(),
                    meta.ts(),
                    meta.nonce(),
                    meta.signatureB64()
            );

            if (verifyRes == null) {
                res.setStatusCode(502);
                res.setDescription("Consent verification returned no response");
                return res;
            }

            log.info("[CONSENT] verifyRes statusCode={} description={}",
                    verifyRes.getStatusCode(),
                    verifyRes.getDescription());

            return copyResponse(verifyRes);

        } catch (Exception e) {
            log.error("[CONSENT] local verify call failed", e);
            res.setStatusCode(502);
            res.setDescription("Consent verification service unavailable");
            return res;
        }
    }

    private BaseResponse copyResponse(BaseResponse source) {
        BaseResponse res = new BaseResponse();
        res.setStatusCode(source.getStatusCode());
        res.setDescription(source.getDescription());
        if (source.getData() != null) {
            res.setData(source.getData());
        }
        return res;
    }
}
