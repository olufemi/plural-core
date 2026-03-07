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
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.Purpose;
import com.finacial.wealth.api.fxpeer.exchange.repo.DeviceChallengeRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.*;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceChallengeService {

    private final DeviceChallengeRepo repo;

    public DeviceChallengeEntity createTxChallenge(String userId, String deviceId, String txId, String txHashB64, Duration ttl) {
        DeviceChallengeEntity c = new DeviceChallengeEntity();
        c.setId(UUID.randomUUID().toString().replace("-", ""));
        c.setUserId(userId);
        c.setDeviceId(deviceId);
        c.setPurpose(Purpose.CONSENT);
        c.setTxId(txId);
        c.setTxHashB64(txHashB64);
        c.setNonceB64(randomB64(24));
        c.setCreatedAt(Instant.now());
        c.setExpiresAt(Instant.now().plus(ttl));
        return repo.save(c);
    }

    @Transactional
    public DeviceChallengeEntity getValidUnused(String userId, String challengeId) {
        DeviceChallengeEntity c = repo.findByIdAndUserId(challengeId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid challenge"));

        if (c.getUsedAt() != null) throw new IllegalArgumentException("Challenge already used");
        if (Instant.now().isAfter(c.getExpiresAt())) throw new IllegalArgumentException("Challenge expired");
        return c;
    }

    private String randomB64(int bytes) {
        byte[] b = new byte[bytes];
        new SecureRandom().nextBytes(b);
        return Base64.getEncoder().encodeToString(b);
    }
}
