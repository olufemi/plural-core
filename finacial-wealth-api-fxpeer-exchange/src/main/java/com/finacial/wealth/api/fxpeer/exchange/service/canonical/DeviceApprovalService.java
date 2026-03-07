/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.service.canonical;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.domain.DeviceChallengeEntity;
import com.finacial.wealth.api.fxpeer.exchange.domain.DeviceKeyEntity;
import com.finacial.wealth.api.fxpeer.exchange.repo.DeviceChallengeRepo;
import com.finacial.wealth.api.fxpeer.exchange.repo.DeviceKeyRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class DeviceApprovalService {

    private final DeviceKeyRepo keyRepo;
    private final DeviceChallengeRepo challengeRepo;

    @Transactional
    public void approveTx(String userId, String txId, String deviceId, String deviceKid, String challengeId, String sigDerB64) {

        DeviceKeyEntity key = keyRepo.findByUserIdAndDeviceIdAndKidAndStatus(userId, deviceId, deviceKid, DeviceKeyEntity.Status.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("Device key not active"));

        DeviceChallengeEntity c = challengeRepo.findByIdAndUserId(challengeId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid challenge"));

        if (!txId.equals(c.getTxId())) throw new IllegalArgumentException("txId mismatch");
        if (!deviceId.equals(c.getDeviceId())) throw new IllegalArgumentException("deviceId mismatch");
        if (c.getUsedAt() != null) throw new IllegalArgumentException("Challenge already used");
        if (Instant.now().isAfter(c.getExpiresAt())) throw new IllegalArgumentException("Challenge expired");

        String canonical = DeviceChallengeCanonical.canonicalV1(c, userId, deviceKid);

        boolean ok = Es256DerUtil.verifySpkiB64DerSigB64(key.getPublicSpkiB64(), canonical, sigDerB64);
        if (!ok) throw new IllegalArgumentException("Invalid signature");

        c.setUsedAt(Instant.now());
        challengeRepo.save(c);

        key.setLastSeenAt(Instant.now());
        keyRepo.save(key);

        // TODO: mark transaction approved in your tx workflow
    }
}