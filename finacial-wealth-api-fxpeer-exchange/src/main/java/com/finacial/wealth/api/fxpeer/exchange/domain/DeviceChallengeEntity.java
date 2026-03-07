/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.domain;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.Purpose;
import jakarta.persistence.*;
import java.time.Instant;
import lombok.Data;

@Entity
@Data
@Table(
        name = "device_challenges",
        indexes = {
            @Index(name = "idx_dc_user_device", columnList = "user_id,device_id"),
            @Index(name = "idx_dc_expires", columnList = "expires_at")
        }
)
public class DeviceChallengeEntity {

   

    @Id
    @Column(name = "id", length = 64)
    private String id; // uuid w/out dashes

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "device_id", nullable = false, length = 120)
    private String deviceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false, length = 30)
    private Purpose purpose;

    @Column(name = "tx_id", nullable = false, length = 80)
    private String txId;

    @Column(name = "tx_hash_b64", nullable = false, length = 120)
    private String txHashB64;

    @Column(name = "nonce_b64", nullable = false, length = 120)
    private String nonceB64;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // in DeviceChallengeEntity
    @Column(name = "action", nullable = false, length = 40)
    private String action;

    @Column(name = "ref_id", nullable = false, length = 80)
    private String refId;

    @Column(name = "payload_hash_b64", nullable = false, length = 120)
    private String payloadHashB64;
    
    

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public String getPayloadHashB64() {
        return payloadHashB64;
    }

    public void setPayloadHashB64(String payloadHashB64) {
        this.payloadHashB64 = payloadHashB64;
    }

    // getters/setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Purpose getPurpose() {
        return purpose;
    }

    public void setPurpose(Purpose purpose) {
        this.purpose = purpose;
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getTxHashB64() {
        return txHashB64;
    }

    public void setTxHashB64(String txHashB64) {
        this.txHashB64 = txHashB64;
    }

    public String getNonceB64() {
        return nonceB64;
    }

    public void setNonceB64(String nonceB64) {
        this.nonceB64 = nonceB64;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(Instant usedAt) {
        this.usedAt = usedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
