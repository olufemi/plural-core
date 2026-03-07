/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.service.canonical;

import com.finacial.wealth.api.fxpeer.exchange.domain.DeviceKeyEntity;
import com.finacial.wealth.api.fxpeer.exchange.repo.DeviceKeyRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 *
 * @author olufemioshin
 */
@Service
@RequiredArgsConstructor
public class ConsentVerifier {

    private final DeviceKeyRepo deviceKeyRepo;
    private final ReplayNonceStore replayNonceStore;

    // allow 2 minutes skew
    private static final long MAX_SKEW_MS = 2 * 60 * 1000;

    public void verifyOrThrow(String userId,
            String httpMethod,
            String path,
            String processId,
            String bodySha256B64,
            String deviceId,
            String kid,
            long ts,
            String nonce,
            String sigB64) {

        long now = System.currentTimeMillis();
        if (Math.abs(now - ts) > MAX_SKEW_MS) {
            throw new IllegalArgumentException("Consent timestamp out of range");
        }

        // replay key (per user/device/nonce)
        String replayKey = "consent:" + userId + ":" + deviceId + ":" + nonce;
        boolean fresh = replayNonceStore.markIfNew(replayKey, now, MAX_SKEW_MS);
        if (!fresh) {
            throw new IllegalArgumentException("Replay detected");
        }

        DeviceKeyEntity key = deviceKeyRepo
                .findByUserIdAndDeviceIdAndKidAndStatus(userId, deviceId, kid, DeviceKeyEntity.Status.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("Device key not active"));

        String canonical = canonicalV1(userId, httpMethod, path, processId, bodySha256B64, ts, nonce, deviceId, kid);

        boolean ok = Es256DerUtil.verifySpkiB64DerSigB64(key.getPublicSpkiB64(), canonical, sigB64);
        if (!ok) {
            throw new IllegalArgumentException("Invalid consent signature");
        }
    }

    private String canonicalV1(String userId, String method, String path, String processId,
            String bodySha256B64, long ts, String nonce, String deviceId, String kid) {

        return "v1|CONSENT"
                + "|" + method
                + "|" + path
                + "|processId=" + processId
                + "|bodySha256B64=" + bodySha256B64
                + "|ts=" + ts
                + "|nonce=" + nonce
                + "|userId=" + userId
                + "|deviceId=" + deviceId
                + "|kid=" + kid;
    }
}
