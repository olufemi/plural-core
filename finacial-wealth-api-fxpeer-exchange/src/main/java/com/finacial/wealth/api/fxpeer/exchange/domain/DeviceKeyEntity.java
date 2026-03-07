/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.domain;

/**
 *
 * @author olufemioshin
 */
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
  name = "user_device_keys",
  indexes = {
    @Index(name="idx_udk_user_status", columnList="user_id,status"),
    @Index(name="idx_udk_user_device", columnList="user_id,device_id"),
    @Index(name="idx_udk_user_device_status", columnList="user_id,device_id,status"),
    @Index(name="idx_udk_user_device_kid_status", columnList="user_id,device_id,kid,status")
  }
)
public class DeviceKeyEntity {

    public enum Status { PENDING, ACTIVE, REVOKED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="user_id", nullable=false)
    private String userId;

    @Column(name="device_id", nullable=false, length=120)
    private String deviceId;

    @Column(name="kid", nullable=false, length=40)
    private String kid;

    @Lob
    @Column(name="public_spki_b64", nullable=false, length=5000)
    private String publicSpkiB64;

    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable=false, length=20)
    private Status status;

    @Column(name="created_at", nullable=false)
    private Instant createdAt;

    @Column(name="activated_at")
    private Instant activatedAt;

    @Column(name="revoked_at")
    private Instant revokedAt;

    @Column(name="last_seen_at")
    private Instant lastSeenAt;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    

    // getters/setters
    public Long getId() { return id; }
      public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getKid() { return kid; }
    public void setKid(String kid) { this.kid = kid; }
    public String getPublicSpkiB64() { return publicSpkiB64; }
    public void setPublicSpkiB64(String publicSpkiB64) { this.publicSpkiB64 = publicSpkiB64; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getActivatedAt() { return activatedAt; }
    public void setActivatedAt(Instant activatedAt) { this.activatedAt = activatedAt; }
    public Instant getRevokedAt() { return revokedAt; }
    public void setRevokedAt(Instant revokedAt) { this.revokedAt = revokedAt; }
    public Instant getLastSeenAt() { return lastSeenAt; }
    public void setLastSeenAt(Instant lastSeenAt) { this.lastSeenAt = lastSeenAt; }
}
