/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.offer;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.common.AuditedBase;
import com.finacial.wealth.api.fxpeer.exchange.common.CurrencyCode;
import com.finacial.wealth.api.fxpeer.exchange.common.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "offers")
public class Offer extends AuditedBase {

    @Column(nullable = false)
    private Long sellerUserId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CurrencyCode currencySell;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CurrencyCode currencyReceive;
    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal rate; // receive per sell
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal qtyTotal;
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal qtyAvailable;
    private Instant expiryAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferStatus status;

// getters/setters
    public Long getSellerUserId() {
        return sellerUserId;
    }

    public void setSellerUserId(Long sellerUserId) {
        this.sellerUserId = sellerUserId;
    }

    public CurrencyCode getCurrencySell() {
        return currencySell;
    }

    public void setCurrencySell(CurrencyCode currencySell) {
        this.currencySell = currencySell;
    }

    public CurrencyCode getCurrencyReceive() {
        return currencyReceive;
    }

    public void setCurrencyReceive(CurrencyCode currencyReceive) {
        this.currencyReceive = currencyReceive;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public BigDecimal getQtyTotal() {
        return qtyTotal;
    }

    public void setQtyTotal(BigDecimal qtyTotal) {
        this.qtyTotal = qtyTotal;
    }

    public BigDecimal getQtyAvailable() {
        return qtyAvailable;
    }

    public void setQtyAvailable(BigDecimal qtyAvailable) {
        this.qtyAvailable = qtyAvailable;
    }

    public Instant getExpiryAt() {
        return expiryAt;
    }

    public void setExpiryAt(Instant expiryAt) {
        this.expiryAt = expiryAt;
    }

    public OfferStatus getStatus() {
        return status;
    }

    public void setStatus(OfferStatus status) {
        this.status = status;
    }
}
