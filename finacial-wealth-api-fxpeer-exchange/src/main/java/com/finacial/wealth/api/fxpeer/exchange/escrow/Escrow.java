/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.escrow;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.common.*;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "escrows")
public class Escrow extends AuditedBase {

    private Long orderId;
    @Enumerated(EnumType.STRING)
    private EscrowStatus status;
    private Instant expiresAt;
    @Embedded
    private EscrowLeg buyerLeg;
    @Embedded
    private EscrowLeg sellerLeg;
    private boolean buyerReleased;
    private boolean sellerReleased;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public EscrowStatus getStatus() {
        return status;
    }

    public void setStatus(EscrowStatus status) {
        this.status = status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public EscrowLeg getBuyerLeg() {
        return buyerLeg;
    }

    public void setBuyerLeg(EscrowLeg buyerLeg) {
        this.buyerLeg = buyerLeg;
    }

    public EscrowLeg getSellerLeg() {
        return sellerLeg;
    }

    public void setSellerLeg(EscrowLeg sellerLeg) {
        this.sellerLeg = sellerLeg;
    }

    public boolean isBuyerReleased() {
        return buyerReleased;
    }

    public void setBuyerReleased(boolean buyerReleased) {
        this.buyerReleased = buyerReleased;
    }

    public boolean isSellerReleased() {
        return sellerReleased;
    }

    public void setSellerReleased(boolean sellerReleased) {
        this.sellerReleased = sellerReleased;
    }
}
