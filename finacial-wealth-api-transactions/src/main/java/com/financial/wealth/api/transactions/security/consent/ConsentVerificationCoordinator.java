/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.security.consent;

/**
 *
 * @author olufemioshin
 */
import com.financial.wealth.api.transactions.models.BaseResponse;
import com.financial.wealth.api.transactions.models.ConsentVerifyRequest;
import com.financial.wealth.api.transactions.proxies.FxPeerClient;
import com.financial.wealth.api.transactions.security.consent.hasher.raw.ConsentRawBodyUtil;
import com.financial.wealth.api.transactions.security.consent.hasher.raw.RawConsentPayloadHasher;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class ConsentVerificationCoordinator {

    private static final Logger log = LoggerFactory.getLogger(ConsentVerificationCoordinator.class);

    private final FxPeerClient fxPeerClient;
    private final ConsentHeaderExtractor consentHeaderExtractor;

    public ConsentVerificationCoordinator(FxPeerClient fxPeerClient,
            ConsentHeaderExtractor consentHeaderExtractor) {
        this.fxPeerClient = fxPeerClient;
        this.consentHeaderExtractor = consentHeaderExtractor;
    }

    public <T> BaseResponse requireConsent(HttpServletRequest http,
            String method,
            String refId,
            String userId,
            T request,
            ConsentPayloadHasher<T> payloadHasher) {

        BaseResponse res = new BaseResponse();

        if (request == null) {
            res.setStatusCode(400);
            res.setDescription("Missing request payload for consent verification");
            return res;
        }

        ConsentRequestMeta meta = new ConsentRequestMeta();
        BaseResponse headerRes = consentHeaderExtractor.extract(http, meta);
        if (headerRes.getStatusCode() != 200) {
            return copyResponse(headerRes);
        }

        log.info("[CONSENT] headers deviceId={} kid={} ts={} nonce={} sig.len={}",
                meta.getDeviceId(), meta.getKid(), meta.getTs(),
                meta.getNonce(),
                meta.getSignatureB64() == null ? 0 : meta.getSignatureB64().length());

        String diagnosticCanonical = null;
        try {
            diagnosticCanonical = payloadHasher.diagnosticCanonicalPayload(request);
        } catch (Exception e) {
            log.warn("[CONSENT] diagnostic canonical generation failed", e);
        }

        if (diagnosticCanonical != null) {
            log.info("[CONSENT] diagnosticCanonical=\n{}", diagnosticCanonical);
        }

        String verifyPayloadHashB64;
        try {
            String appJson = payloadHasher.appJsonPayloadString(request);
            log.info("[CONSENT] appJsonPayloadString={}", appJson);

            verifyPayloadHashB64 = payloadHasher.payloadHashB64(request);
            log.info("[CONSENT] verifyPayloadHashB64={}", verifyPayloadHashB64);
        } catch (Exception e) {
            log.error("[CONSENT] payload hash generation failed", e);
            res.setStatusCode(500);
            res.setDescription("Unable to prepare consent payload");
            return res;
        }

        ConsentVerifyRequest body = new ConsentVerifyRequest(
                method,
                meta.getPath(),
                refId,
                verifyPayloadHashB64,
                userId
        );

        log.info("[CONSENT] sending verify body={}", body);

        BaseResponse verifyRes;
        try {
            verifyRes = fxPeerClient.verify(
                    meta.getAuthorization(),
                    meta.getDeviceId(),
                    meta.getKid(),
                    meta.getTs(),
                    meta.getNonce(),
                    meta.getSignatureB64(),
                    body
            );
        } catch (FeignException e) {
            String responseBody = e.contentUTF8();
            log.error("[CONSENT] verify call failed status={} body={}",
                    e.status(), responseBody, e);

            res.setStatusCode(e.status());
            res.setDescription(responseBody != null && !responseBody.isEmpty()
                    ? responseBody
                    : "Consent verification failed");
            return res;
        } catch (Exception e) {
            log.error("[CONSENT] verify call failed", e);
            res.setStatusCode(502);
            res.setDescription("Consent verification service unavailable");
            return res;
        }

        log.info("[CONSENT] verifyRes statusCode={} description={}",
                verifyRes == null ? null : verifyRes.getStatusCode(),
                verifyRes == null ? null : verifyRes.getDescription());

        if (verifyRes == null) {
            res.setStatusCode(502);
            res.setDescription("Consent verification returned no response");
            return res;
        }

        return copyResponse(verifyRes);
    }

    public BaseResponse requireConsentUsingRawBody(HttpServletRequest http,
            String method,
            String refId,
            String userId,
            RawConsentPayloadHasher payloadHasher) {

        BaseResponse res = new BaseResponse();

        ConsentRequestMeta meta = new ConsentRequestMeta();

        BaseResponse headerRes = consentHeaderExtractor.extract(http, meta);
        if (headerRes.getStatusCode() != 200) {
            return copyResponse(headerRes);
        }

        String rawBody;
        try {
            rawBody = ConsentRawBodyUtil.getRawRequestBody(http);
        } catch (Exception e) {
            res.setStatusCode(500);
            res.setDescription("Unable to read raw request body");
            return res;
        }

        log.info("[CONSENT] headers deviceId={} kid={} ts={} nonce={} sig.len={}",
                meta.getDeviceId(),
                meta.getKid(),
                meta.getTs(),
                meta.getNonce(),
                meta.getSignatureB64() == null ? 0 : meta.getSignatureB64().length());

        String payloadHashB64;

        try {
            payloadHashB64 = payloadHasher.payloadHashB64(rawBody);
        } catch (Exception e) {
            log.error("[CONSENT] payload hash generation failed", e);
            res.setStatusCode(500);
            res.setDescription("Unable to compute payload hash");
            return res;
        }

        ConsentVerifyRequest body = new ConsentVerifyRequest(
                method,
                meta.getPath(),
                refId,
                payloadHashB64,
                userId
        );

        log.info("[CONSENT] sending verify body={}", body);

        try {

            BaseResponse verifyRes = fxPeerClient.verify(
                    meta.getAuthorization(),
                    meta.getDeviceId(),
                    meta.getKid(),
                    meta.getTs(),
                    meta.getNonce(),
                    meta.getSignatureB64(),
                    body
            );

            if (verifyRes == null) {
                res.setStatusCode(502);
                res.setDescription("Consent verification returned no response");
                return res;
            }

            return copyResponse(verifyRes);

        } catch (feign.FeignException e) {

            String responseBody = e.contentUTF8();

            log.error("[CONSENT] verify call failed status={} body={}",
                    e.status(), responseBody, e);

            res.setStatusCode(e.status());
            res.setDescription(responseBody != null ? responseBody : "Consent verification failed");

            return res;

        } catch (Exception e) {

            log.error("[CONSENT] verify call failed", e);

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
