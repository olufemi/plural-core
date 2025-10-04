/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.offer;

import com.finacial.wealth.api.fxpeer.exchange.common.CurrencyCode;
import java.math.BigDecimal;
import java.time.Instant;

/**
 *
 * @author olufemioshin
 */
public class CreateOfferRq {

    private CurrencyCode currencySell;
    private CurrencyCode currencyReceive;
    private BigDecimal rate;
    private BigDecimal qtyTotal;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private Instant expiryAt;
    private boolean showInTopDeals;

    public CreateOfferRq(CurrencyCode currencySell, CurrencyCode currencyReceive,
            BigDecimal rate, BigDecimal qtyTotal, Instant expiryAt) {
        this.currencySell = currencySell;
        this.currencyReceive = currencyReceive;
        this.rate = rate;
        this.qtyTotal = qtyTotal;
        this.expiryAt = expiryAt;
    }

    public boolean isShowInTopDeals() {
        return showInTopDeals;
    }

    public void setShowInTopDeals(boolean showInTopDeals) {
        this.showInTopDeals = showInTopDeals;
    }

    public BigDecimal getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(BigDecimal minAmount) {
        this.minAmount = minAmount;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }

    // getters only (immutable) or add setters if you prefer
    public CurrencyCode getCurrencySell() {
        return currencySell;
    }

    public CurrencyCode getCurrencyReceive() {
        return currencyReceive;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public BigDecimal getQtyTotal() {
        return qtyTotal;
    }

    public Instant getExpiryAt() {
        return expiryAt;
    }
}
