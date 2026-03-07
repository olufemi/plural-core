package com.finacial.wealth.api.fxpeer.exchange.service.canonical;

import com.finacial.wealth.api.fxpeer.exchange.domain.DeviceKeyEntity;
import com.finacial.wealth.api.fxpeer.exchange.model.BaseResponse;
import com.finacial.wealth.api.fxpeer.exchange.repo.DeviceKeyRepo;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import om.finacial.wealth.api.fxpeer.exchange.service.canonical.model.ConsentVerifyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ConsentVerifierService {

    private static final Logger log = LoggerFactory.getLogger(ConsentVerifierService.class);

    private final DeviceKeyRepo deviceKeyRepo;

    public ConsentVerifierService(DeviceKeyRepo deviceKeyRepo) {
        this.deviceKeyRepo = deviceKeyRepo;
    }

    public BaseResponse verifyOrThrow(ConsentVerifyRequest body,
            String deviceId,
            String kid,
            Long tsHeader,
            String nonceHeader,
            String sigB64) {

        if (body == null) {
            return new BaseResponse(400, "Invalid request body");
        }

        if (isBlank(body.getMethod())
                || isBlank(body.getPath())
                || isBlank(body.getRefId())
                || isBlank(body.getPayloadHashB64())
                || isBlank(body.getUserId())
                || isBlank(deviceId)
                || isBlank(kid)
                || isBlank(nonceHeader)
                || isBlank(sigB64)
                || tsHeader == null) {
            return new BaseResponse(400, "Missing required consent fields");
        }

        Optional<DeviceKeyEntity> keyOpt = deviceKeyRepo.findByUserIdAndDeviceIdAndKidAndStatus(
                body.getUserId(),
                deviceId,
                kid,
                DeviceKeyEntity.Status.ACTIVE
        );

        log.info("[CONSENT] key lookup userId={} deviceId={} kid={} found={}",
                body.getUserId(), deviceId, kid, keyOpt.isPresent());

        if (!keyOpt.isPresent()) {
            return new BaseResponse(403, "Active device key not found");
        }

        DeviceKeyEntity key = keyOpt.get();

        byte[] storedKeyBytes;
        try {
            storedKeyBytes = Base64.getDecoder().decode(key.getPublicSpkiB64());
            log.info("[CONSENT] storedPublicKey.spki.length={}", storedKeyBytes.length);
            log.info("[CONSENT] storedPublicKey.spki.sha256={}", sha256B64(storedKeyBytes));
        } catch (Exception e) {
            log.error("[CONSENT] stored public key base64 decode failed", e);
            return new BaseResponse(403, "Invalid stored public key encoding");
        }

        PublicKey publicKey;
        try {
            publicKey = parsePublicKey(key.getPublicSpkiB64());
        } catch (Exception e) {
            log.error("[CONSENT] parse public key failed", e);
            return new BaseResponse(403, "Invalid stored public key");
        }

        byte[] sigBytes;
        try {
            sigBytes = Base64.getDecoder().decode(sigB64);
        } catch (Exception e) {
            log.warn("[CONSENT] invalid signature encoding", e);
            return new BaseResponse(403, "Invalid consent signature encoding");
        }

        log.info("[CONSENT] sigBytes.length={}", sigBytes.length);
        log.info("[CONSENT] sigB64={}", sigB64);

        String singleLineCanonical = ConsentCanonicalBuilder.singleLine(
                body.getMethod(),
                body.getPath(),
                body.getRefId(),
                body.getPayloadHashB64(),
                tsHeader.longValue(),
                nonceHeader,
                body.getUserId(),
                deviceId,
                kid
        );

        try {
            log.info("[CONSENT] VERIFY_CANONICAL singleLine='{}'", singleLineCanonical);
            log.info("[CONSENT] singleLine.sha256={}",
                    sha256B64(singleLineCanonical.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            log.warn("[CONSENT] failed to hash single-line canonical", e);
        }

        boolean singleLineOk = verifyWithFallbackSignatureFormats(publicKey, singleLineCanonical, sigBytes);
        log.info("[CONSENT] singleLine.verify.result={}", singleLineOk);

        if (singleLineOk) {
            log.info("[CONSENT] signature verified using single-line canonical");
            return new BaseResponse(200, "Consent verified");
        }

        String multiLineCanonical = ConsentCanonicalBuilder.multiLine(
                body.getMethod(),
                body.getPath(),
                body.getRefId(),
                body.getPayloadHashB64(),
                tsHeader.longValue(),
                nonceHeader,
                body.getUserId(),
                deviceId,
                kid
        );

        try {
            log.info("[CONSENT] VERIFY_CANONICAL multiLine='{}'", multiLineCanonical);
            log.info("[CONSENT] multiLine.sha256={}",
                    sha256B64(multiLineCanonical.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            log.warn("[CONSENT] failed to hash multi-line canonical", e);
        }

        boolean multiLineOk = verifyWithFallbackSignatureFormats(publicKey, multiLineCanonical, sigBytes);
        log.info("[CONSENT] multiLine.verify.result={}", multiLineOk);

        if (multiLineOk) {
            log.warn("[CONSENT] signature verified using multiline canonical compatibility mode");
            return new BaseResponse(200, "Consent verified");
        }

        log.warn("[CONSENT] reject: Invalid consent signature for userId={} deviceId={} kid={}",
                body.getUserId(), deviceId, kid);

        BaseResponse res = new BaseResponse(403, "Invalid consent signature");
        res.addData("canonicalLength", singleLineCanonical.length());
        res.addData("canonical", singleLineCanonical);
        res.addData("canonicalBytesB64",
                Base64.getEncoder().encodeToString(singleLineCanonical.getBytes(StandardCharsets.UTF_8)));

        return res;
    }

    private boolean verifyWithFallbackSignatureFormats(PublicKey publicKey, String canonical, byte[] sigBytes) {
        try {
            byte[] canonicalBytes = canonical.getBytes(StandardCharsets.UTF_8);

            log.info("[CONSENT] canonical.sha256={}", sha256B64(canonicalBytes));
            log.info("[CONSENT] sigBytes.length={}", sigBytes.length);

            boolean derOk = verifyDer(publicKey, canonicalBytes, sigBytes);
            log.info("[CONSENT] verify DER result={}", derOk);

            if (derOk) {
                return true;
            }

            if (sigBytes.length == 64) {
                try {
                    byte[] derFromRaw = p1363ToDer(sigBytes);
                    boolean rawOk = verifyDer(publicKey, canonicalBytes, derFromRaw);
                    log.info("[CONSENT] verify RAW-P1363->DER result={}", rawOk);
                    return rawOk;
                } catch (Exception e) {
                    log.warn("[CONSENT] raw P1363 conversion failed", e);
                }
            } else {
                log.info("[CONSENT] signature is not 64 bytes, so raw P1363 fallback skipped");
            }

            return false;
        } catch (Exception e) {
            log.warn("[CONSENT] verification failure", e);
            return false;
        }
    }

    private boolean verifyDer(PublicKey publicKey, byte[] data, byte[] derSig) {
        try {
            Signature verifier = Signature.getInstance("SHA256withECDSA");
            verifier.initVerify(publicKey);
            verifier.update(data);
            return verifier.verify(derSig);
        } catch (Exception e) {
            log.warn("[CONSENT] signature verify error: {}", e.getMessage());
            return false;
        }
    }

    private PublicKey parsePublicKey(String publicSpkiB64) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(publicSpkiB64);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            return keyFactory.generatePublic(spec);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid stored public key", e);
        }
    }

    private byte[] p1363ToDer(byte[] sig) {
        if (sig == null || sig.length != 64) {
            throw new IllegalArgumentException("Expected 64-byte P1363 signature");
        }

        byte[] r = Arrays.copyOfRange(sig, 0, 32);
        byte[] s = Arrays.copyOfRange(sig, 32, 64);

        return derEncode(r, s);
    }

    private byte[] derEncode(byte[] r, byte[] s) {
        byte[] derR = derInteger(r);
        byte[] derS = derInteger(s);

        int seqLen = 2 + derR.length + 2 + derS.length;
        byte[] out = new byte[2 + seqLen];

        int i = 0;
        out[i++] = 0x30;
        out[i++] = (byte) seqLen;

        out[i++] = 0x02;
        out[i++] = (byte) derR.length;
        System.arraycopy(derR, 0, out, i, derR.length);
        i += derR.length;

        out[i++] = 0x02;
        out[i++] = (byte) derS.length;
        System.arraycopy(derS, 0, out, i, derS.length);

        return out;
    }

    private byte[] derInteger(byte[] value) {
        int firstNonZero = 0;
        while (firstNonZero < value.length - 1 && value[firstNonZero] == 0) {
            firstNonZero++;
        }

        byte[] stripped = Arrays.copyOfRange(value, firstNonZero, value.length);

        if (stripped.length == 0) {
            return new byte[]{0x00};
        }

        if ((stripped[0] & 0x80) != 0) {
            byte[] prefixed = new byte[stripped.length + 1];
            prefixed[0] = 0x00;
            System.arraycopy(stripped, 0, prefixed, 1, stripped.length);
            return prefixed;
        }

        return stripped;
    }

    private String sha256B64(byte[] input) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(md.digest(input));
        } catch (Exception e) {
            return "n/a";
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
