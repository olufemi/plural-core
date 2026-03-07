package com.financial.wealth.api.transactions.services;

import com.financial.wealth.api.transactions.models.BaseResponse;
import com.financial.wealth.api.transactions.models.ConsentVerifyRequest;
import com.financial.wealth.api.transactions.models.LocalTransferRequest;
import com.financial.wealth.api.transactions.proxies.FxPeerClient;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public final class LocalTransferCanon {

    private static final Logger log = LoggerFactory.getLogger(LocalTransferCanon.class);

    private final FxPeerClient fxPeerClient;

    public LocalTransferCanon(FxPeerClient fxPeerClient) {
        this.fxPeerClient = fxPeerClient;
    }

    /**
     * Stable payload hash for backend canonical form. Kept only for
     * logging/diagnostics for now.
     */
    public static String payloadHashB64(LocalTransferRequest r) {
        String canonical = canonicalPayloadV1(r);

        log.info("[CONSENT] canonicalPayload=\n{}", canonical);

        byte[] sha = sha256(canonical.getBytes(StandardCharsets.UTF_8));
        String payloadHashB64 = Base64.getEncoder().encodeToString(sha);

        log.info("[CONSENT] backend canonical payloadHashB64={}", payloadHashB64);

        return payloadHashB64;
    }

    /**
     * App-compatible JSON payload string in the exact field order the app
     * hashes.
     */
    public static String appJsonPayloadString(LocalTransferRequest r) {
        String json = "{"
                + "\"receiver\":\"" + esc(nz(r.getReceiver())) + "\","
                + "\"receiverName\":\"" + esc(nz(r.getReceiverName())) + "\","
                + "\"sender\":\"" + esc(nz(r.getSender())) + "\","
                + "\"amount\":\"" + esc(nz(r.getAmount())) + "\","
                + "\"fees\":\"" + esc(nz(r.getFees())) + "\","
                + "\"theNarration\":\"" + esc(nz(r.getTheNarration())) + "\","
                + "\"processId\":\"" + esc(nz(r.getProcessId())) + "\","
                + "\"transactionType\":\"" + esc(nz(r.getTransactionType())) + "\""
                + "}";

        log.info("[CONSENT] appJsonPayloadString={}", json);
        return json;
    }

    /**
     * Use ONLY the app JSON payload hash for verification request.
     */
    public static String appJsonPayloadHashB64(LocalTransferRequest r) {
        String json = appJsonPayloadString(r);
        byte[] sha = sha256(json.getBytes(StandardCharsets.UTF_8));
        String payloadHashB64 = Base64.getEncoder().encodeToString(sha);

        log.info("[CONSENT] appJsonPayloadHashB64={}", payloadHashB64);
        return payloadHashB64;
    }

    /**
     * Canonical payload string. Kept for diagnostics/reference.
     */
    public static String canonicalPayloadV1(LocalTransferRequest r) {
        return "v1|LOCALTRANSFER"
                + "|refId=" + nz(r.getProcessId())
                + "|transactionType=" + nz(r.getTransactionType())
                + "|sender=" + nz(r.getSender())
                + "|receiver=" + nz(r.getReceiver())
                + "|receiverName=" + nz(r.getReceiverName())
                + "|amount=" + nz(r.getAmount())
                + "|fees=" + nz(r.getFees())
                + "|theNarration=" + nz(r.getTheNarration());
    }

    public BaseResponse requireConsent(HttpServletRequest http,
            String method,
            String refId,
            String payloadHashB64, // ignored intentionally now
            String userId,
            LocalTransferRequest req) {

        BaseResponse res = new BaseResponse();

        String auth = trimToNull(http.getHeader("Authorization"));
        if (auth == null) {
            res.setStatusCode(401);
            res.setDescription("Missing Authorization header");
            return res;
        }

        String tsHeader = trimToNull(http.getHeader("X-Consent-Ts"));
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

        String deviceId = trimToNull(http.getHeader("X-Device-Id"));
        if (deviceId == null) {
            res.setStatusCode(400);
            res.setDescription("Missing X-Device-Id");
            return res;
        }

        String kid = trimToNull(http.getHeader("X-Device-Kid"));
        if (kid == null) {
            res.setStatusCode(400);
            res.setDescription("Missing X-Device-Kid");
            return res;
        }

        String nonce = trimToNull(http.getHeader("X-Consent-Nonce"));
        if (nonce == null) {
            res.setStatusCode(400);
            res.setDescription("Missing X-Consent-Nonce");
            return res;
        }

        String sigB64 = trimToNull(http.getHeader("X-Consent-Sig"));
        if (sigB64 == null) {
            res.setStatusCode(400);
            res.setDescription("Missing X-Consent-Sig");
            return res;
        }

        String path = firstNonBlank(
                http.getHeader("X-Original-Uri"),
                http.getHeader("X-Forwarded-Uri"),
                "/api/transactions" + http.getRequestURI()
        );

        log.info("[CONSENT] headers deviceId={} kid={} ts={} nonce={} sig.len={}",
                deviceId, kid, ts, nonce, sigB64.length());

        if (req == null) {
            res.setStatusCode(400);
            res.setDescription("Missing request payload for consent verification");
            return res;
        }

        // For visibility only
        String backendCanonicalHash = payloadHashB64(req);

        // Use ONLY JSON fallback hash going forward
        String verifyPayloadHashB64 = appJsonPayloadHashB64(req);

        log.warn("[CONSENT] using JSON payload hash only for verify. canonicalHash={} jsonHash={}",
                backendCanonicalHash, verifyPayloadHashB64);

        ConsentVerifyRequest body = new ConsentVerifyRequest(
                method,
                path,
                refId,
                verifyPayloadHashB64,
                userId
        );

        log.info("[CONSENT] sending verify body={}", body);

        BaseResponse verifyRes;
        try {
            verifyRes = fxPeerClient.verify(
                    auth,
                    deviceId,
                    kid,
                    ts,
                    nonce,
                    sigB64,
                    body
            );
        } catch (feign.FeignException e) {
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

    private static BaseResponse copyResponse(BaseResponse source) {
        BaseResponse res = new BaseResponse();
        res.setStatusCode(source.getStatusCode());
        res.setDescription(source.getDescription());
        if (source.getData() != null) {
            res.setData(source.getData());
        }
        return res;
    }

    private static String nz(Object s) {
        return s == null ? "" : String.valueOf(s).trim();
    }

    private static String esc(String s) {
        return s == null ? "" : s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private static byte[] sha256(byte[] in) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(in);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 failed", e);
        }
    }

    private static String firstNonBlank(String... vals) {
        for (String v : vals) {
            if (v != null && !v.trim().isEmpty()) {
                return v.trim();
            }
        }
        return null;
    }
}
