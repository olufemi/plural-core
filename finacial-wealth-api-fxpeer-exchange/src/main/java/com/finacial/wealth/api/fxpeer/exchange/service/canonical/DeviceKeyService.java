/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.service.canonical;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.domain.DeviceKeyEntity;
import com.finacial.wealth.api.fxpeer.exchange.repo.DeviceKeyRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class DeviceKeyService {

    private static final Logger log = LoggerFactory.getLogger(DeviceKeyService.class);

    private final DeviceKeyRepo repo;

    // Single-device policy: only 1 ACTIVE device per user
    private final boolean singleDevice = true;

    @Transactional
    public DeviceBindingResult upsertOnLogin(String userId, String deviceId, String publicSpkiB64) {

        log.info("[DEVICE-KEY] upsertOnLogin userId={} deviceId={} publicSpkiPresent={}",
                userId,
                deviceId,
                publicSpkiB64 != null && !publicSpkiB64.trim().isEmpty());

        if (deviceId == null || deviceId.trim().isEmpty()) {
            // If you want strict: throw; else return NONE
            return DeviceBindingResult.none();
        }

        String cleanedSpki = publicSpkiB64 == null ? null : publicSpkiB64.replaceAll("\\s", "");

        // If device already ACTIVE with same deviceId, accept and update last_seen
        Optional<DeviceKeyEntity> existingActiveForDevice = repo.findByUserIdAndDeviceIdAndStatus(userId, deviceId, DeviceKeyEntity.Status.ACTIVE);
        if (existingActiveForDevice.isPresent()) {
            log.info("[DEVICE-KEY] existing active device found for same deviceId={}", deviceId);

            DeviceKeyEntity e = existingActiveForDevice.get();
            e.setLastSeenAt(Instant.now());
            repo.save(e);
            return DeviceBindingResult.active(deviceId, e.getKid(), e.getPublicSpkiB64());
        }

        // If user has an ACTIVE device (different deviceId), then this is device-change -> PENDING until OTP confirm
        Optional<DeviceKeyEntity> activeAny = repo.findFirstByUserIdAndStatusOrderByActivatedAtDesc(userId, DeviceKeyEntity.Status.ACTIVE);
        if (activeAny.isPresent()) {
            log.info("[DEVICE-KEY] another active device exists for userId={}, creating pending for deviceId={}", userId, deviceId);

            // create/update PENDING record for this device (new device)
            DeviceKeyEntity pending = findOrCreatePending(userId, deviceId, cleanedSpki);
            return DeviceBindingResult.pending(deviceId, pending.getKid(), pending.getPublicSpkiB64());
        }

        // No ACTIVE device yet: create ACTIVE immediately (or you can choose PENDING + OTP even for first device)
        log.info("[DEVICE-KEY] first device for userId={}, creating active for deviceId={}", userId, deviceId);
        DeviceKeyEntity created = new DeviceKeyEntity();
        created.setUserId(userId);
        created.setDeviceId(deviceId);
        created.setKid(newDeviceKid());
        created.setPublicSpkiB64(require(cleanedSpki, "devicePublicKeySpkiB64 is required for first device"));
        created.setStatus(DeviceKeyEntity.Status.ACTIVE);
        created.setCreatedAt(Instant.now());
        created.setActivatedAt(Instant.now());
        created.setLastSeenAt(Instant.now());
        repo.save(created);

        log.info("[DEVICE-KEY] saved userId={} deviceId={} kid={} status={}",
                created.getUserId(),
                created.getDeviceId(),
                created.getKid(),
                created.getStatus());

        return DeviceBindingResult.active(deviceId, created.getKid(), created.getPublicSpkiB64());
    }

    @Transactional
    public DeviceBindingResult confirmOtpActivate(String userId, String deviceId) {
        // OTP validation should happen outside before calling this method
        DeviceKeyEntity pending = repo.findByUserIdAndDeviceIdAndStatus(userId, deviceId, DeviceKeyEntity.Status.PENDING)
                .orElseThrow(() -> new IllegalArgumentException("No pending device to activate"));

        // Activate this device
        pending.setStatus(DeviceKeyEntity.Status.ACTIVE);
        pending.setActivatedAt(Instant.now());
        pending.setLastSeenAt(Instant.now());
        repo.save(pending);

        // Revoke other active devices (single device policy)
        if (singleDevice) {
            List<DeviceKeyEntity> actives = repo.findByUserIdAndStatus(userId, DeviceKeyEntity.Status.ACTIVE);
            for (DeviceKeyEntity e : actives) {
                if (!e.getDeviceId().equals(deviceId)) {
                    e.setStatus(DeviceKeyEntity.Status.REVOKED);
                    e.setRevokedAt(Instant.now());
                    repo.save(e);
                }
            }
        }

        return DeviceBindingResult.active(deviceId, pending.getKid(), pending.getPublicSpkiB64());
    }

    private DeviceKeyEntity findOrCreatePending(String userId, String deviceId, String publicSpkiB64) {
        // if pending exists, refresh SPKI if provided
        Optional<DeviceKeyEntity> pendingOpt = repo.findByUserIdAndDeviceIdAndStatus(userId, deviceId, DeviceKeyEntity.Status.PENDING);
        if (pendingOpt.isPresent()) {
            DeviceKeyEntity p = pendingOpt.get();
            if (publicSpkiB64 != null && !publicSpkiB64.isEmpty()) {
                p.setPublicSpkiB64(publicSpkiB64);
            }
            p.setLastSeenAt(Instant.now());
            return repo.save(p);
        }

        DeviceKeyEntity p = new DeviceKeyEntity();
        p.setUserId(userId);
        p.setDeviceId(deviceId);
        p.setKid(newDeviceKid());
        p.setPublicSpkiB64(require(publicSpkiB64, "devicePublicKeySpkiB64 is required"));
        p.setStatus(DeviceKeyEntity.Status.PENDING);
        p.setCreatedAt(Instant.now());
        p.setLastSeenAt(Instant.now());
        return repo.save(p);
    }

    private String newDeviceKid() {
        String d = DateTimeFormatter.BASIC_ISO_DATE.format(LocalDate.now());
        return "d_" + d + "_" + UUID.randomUUID().toString().substring(0, 6);
    }

    private String require(String v, String msg) {
        if (v == null || v.trim().isEmpty()) {
            throw new IllegalArgumentException(msg);
        }
        return v;
    }

    public record DeviceBindingResult(String deviceId, String status, String activeKid, String publicKeySpki) {

        static DeviceBindingResult active(String deviceId, String kid, String publicKeySpki) {
            return new DeviceBindingResult(deviceId, "ACTIVE", kid, publicKeySpki);
        }

        static DeviceBindingResult pending(String deviceId, String kid, String publicKeySpki) {
            return new DeviceBindingResult(deviceId, "PENDING", kid, publicKeySpki);
        }

        static DeviceBindingResult none() {
            return new DeviceBindingResult(null, "NONE", null, null);
        }
    }
}
