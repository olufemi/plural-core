/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.service.canonical;

import com.finacial.wealth.api.fxpeer.exchange.domain.DeviceChallengeEntity;
import com.finacial.wealth.api.fxpeer.exchange.domain.DeviceKeyEntity;
import com.finacial.wealth.api.fxpeer.exchange.repo.DeviceChallengeRepo;
import com.finacial.wealth.api.fxpeer.exchange.repo.DeviceKeyRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import om.finacial.wealth.api.fxpeer.exchange.service.canonical.model.ConsentApproveRequest;
import om.finacial.wealth.api.fxpeer.exchange.service.canonical.model.ConsentApproveResponse;
import om.finacial.wealth.api.fxpeer.exchange.service.canonical.model.ConsentCanonical;
import org.springframework.stereotype.Service;

/**
 *
 * @author olufemioshin
 */
@Service
@RequiredArgsConstructor
public class ConsentApproveService {

    private final DeviceKeyRepo keyRepo;
    private final DeviceChallengeRepo challengeRepo;

    @Transactional
    public ConsentApproveResponse approve(String userId, ConsentApproveRequest req) {

        if (req.alg() == null || !"ES256".equalsIgnoreCase(req.alg())) {
            throw new IllegalArgumentException("Unsupported alg");
        }

        DeviceChallengeEntity c = challengeRepo.findByIdAndUserId(req.challengeId(), userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid challenge"));

        if (c.getUsedAt() != null) throw new IllegalArgumentException("Challenge already used");
        if (java.time.Instant.now().isAfter(c.getExpiresAt())) throw new IllegalArgumentException("Challenge expired");
        if (!c.getDeviceId().equals(req.deviceId())) throw new IllegalArgumentException("deviceId mismatch");

        DeviceKeyEntity key = keyRepo.findByUserIdAndDeviceIdAndKidAndStatus(userId, req.deviceId(), req.deviceKid(), DeviceKeyEntity.Status.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("Device key not active"));

        String canonical = ConsentCanonical.canonicalV1(c, userId, req.deviceKid());

        boolean ok = Es256DerUtil.verifySpkiB64DerSigB64(key.getPublicSpkiB64(), canonical, req.sigB64());
        if (!ok) throw new IllegalArgumentException("Invalid signature");

        c.setUsedAt(java.time.Instant.now());
        challengeRepo.save(c);

        key.setLastSeenAt(java.time.Instant.now());
        keyRepo.save(key);

        return new ConsentApproveResponse(true, c.getAction(), c.getRefId());
    }
}
