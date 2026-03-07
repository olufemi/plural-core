/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package om.finacial.wealth.api.fxpeer.exchange.service.canonical.model;

import com.finacial.wealth.api.fxpeer.exchange.domain.DeviceChallengeEntity;
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.Purpose;
import com.finacial.wealth.api.fxpeer.exchange.repo.DeviceChallengeRepo;
import com.finacial.wealth.api.fxpeer.exchange.service.canonical.ConsentPayloadHashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 *
 * @author olufemioshin
 */
@Service
@RequiredArgsConstructor
public class ConsentChallengeService {

    private final DeviceChallengeRepo repo;

    public DeviceChallengeEntity create(String userId, ConsentChallengeRequest req, java.time.Duration ttl) {
        if (req.deviceId() == null || req.deviceId().trim().isEmpty()) throw new IllegalArgumentException("deviceId required");
        if (req.action() == null || req.action().trim().isEmpty()) throw new IllegalArgumentException("action required");
        if (req.refId() == null || req.refId().trim().isEmpty()) throw new IllegalArgumentException("refId required");

        DeviceChallengeEntity c = new DeviceChallengeEntity();
        c.setId(java.util.UUID.randomUUID().toString().replace("-", ""));
        c.setUserId(userId);
        c.setDeviceId(req.deviceId().trim());
        c.setPurpose(Purpose.CONSENT);
        c.setAction(req.action().trim());
        c.setRefId(req.refId().trim());
        c.setPayloadHashB64(ConsentPayloadHashUtil.payloadHashB64(c.getAction(), c.getRefId(), req.payload()));
        c.setNonceB64(randomB64(24));
        c.setCreatedAt(java.time.Instant.now());
        c.setExpiresAt(java.time.Instant.now().plus(ttl));
        return repo.save(c);
    }

    public DeviceChallengeEntity getValidUnused(String userId, String challengeId) {
        DeviceChallengeEntity c = repo.findByIdAndUserId(challengeId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid challenge"));
        if (c.getUsedAt() != null) throw new IllegalArgumentException("Challenge already used");
        if (java.time.Instant.now().isAfter(c.getExpiresAt())) throw new IllegalArgumentException("Challenge expired");
        return c;
    }

    private String randomB64(int bytes) {
        byte[] b = new byte[bytes];
        new java.security.SecureRandom().nextBytes(b);
        return java.util.Base64.getEncoder().encodeToString(b);
    }
}
