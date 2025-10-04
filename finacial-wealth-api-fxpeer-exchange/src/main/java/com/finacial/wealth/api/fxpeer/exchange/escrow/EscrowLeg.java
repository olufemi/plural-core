/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.escrow;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.common.CurrencyCode;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.math.BigDecimal;
import java.time.Instant;

@Embeddable
public class EscrowLeg {

    private Long userId;

    @Enumerated(EnumType.STRING)
    private CurrencyCode currency;

    @Column(precision = 18, scale = 2)
    private BigDecimal requiredAmount;

    @Column(precision = 18, scale = 2)
    private BigDecimal fundedAmount;

    private Instant fundedAt;
    private String ledgerTxnId;
    // getters/setters...'

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public CurrencyCode getCurrency() {
        return currency;
    }

    public void setCurrency(CurrencyCode currency) {
        this.currency = currency;
    }

    public BigDecimal getRequiredAmount() {
        return requiredAmount;
    }

    public void setRequiredAmount(BigDecimal requiredAmount) {
        this.requiredAmount = requiredAmount;
    }

    public BigDecimal getFundedAmount() {
        return fundedAmount;
    }

    public void setFundedAmount(BigDecimal fundedAmount) {
        this.fundedAmount = fundedAmount;
    }

    public Instant getFundedAt() {
        return fundedAt;
    }

    public void setFundedAt(Instant fundedAt) {
        this.fundedAt = fundedAt;
    }

    public String getLedgerTxnId() {
        return ledgerTxnId;
    }

    public void setLedgerTxnId(String ledgerTxnId) {
        this.ledgerTxnId = ledgerTxnId;
    }

}
