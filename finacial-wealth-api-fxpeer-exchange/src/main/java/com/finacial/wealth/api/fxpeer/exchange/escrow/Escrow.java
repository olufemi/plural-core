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
import lombok.Data;

@Entity
@Table(name = "escrows")
public class Escrow extends AuditedBase {

    // Optional: @Column(name = "order_id") if you want snake_case in DB
    private Long orderId;

    @Enumerated(EnumType.STRING)
    private EscrowStatus status;

    private Instant expiresAt;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "userId", column = @Column(name = "buyer_user_id")),
        @AttributeOverride(name = "currency", column = @Column(name = "buyer_currency")),
        @AttributeOverride(name = "requiredAmount", column = @Column(name = "buyer_required_amount", precision = 18, scale = 2)),
        @AttributeOverride(name = "fundedAmount", column = @Column(name = "buyer_funded_amount", precision = 18, scale = 2)),
        @AttributeOverride(name = "fundedAt", column = @Column(name = "buyer_funded_at")),
        @AttributeOverride(name = "ledgerTxnId", column = @Column(name = "buyer_ledger_txn_id"))
    })
    private EscrowLeg buyerLeg;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "userId", column = @Column(name = "seller_user_id")),
        @AttributeOverride(name = "currency", column = @Column(name = "seller_currency")),
        @AttributeOverride(name = "requiredAmount", column = @Column(name = "seller_required_amount", precision = 18, scale = 2)),
        @AttributeOverride(name = "fundedAmount", column = @Column(name = "seller_funded_amount", precision = 18, scale = 2)),
        @AttributeOverride(name = "fundedAt", column = @Column(name = "seller_funded_at")),
        @AttributeOverride(name = "ledgerTxnId", column = @Column(name = "seller_ledger_txn_id"))
    })
    private EscrowLeg sellerLeg;

    private boolean buyerReleased;
    private boolean sellerReleased;

    // getters/setters...
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
