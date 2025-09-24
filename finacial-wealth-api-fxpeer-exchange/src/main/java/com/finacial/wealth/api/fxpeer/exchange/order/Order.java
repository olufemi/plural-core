/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.order;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.common.CurrencyCode;
import com.finacial.wealth.api.fxpeer.exchange.common.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "orders")
public class Order extends com.finacial.wealth.api.fxpeer.exchange.common.AuditedBase {

    private Long offerId;
    private Long sellerUserId;
    private Long buyerUserId;
    @Enumerated(EnumType.STRING)
    private CurrencyCode currencySell; // seller gives
    @Enumerated(EnumType.STRING)
    private CurrencyCode currencyReceive; // seller gets
    @Column(precision = 18, scale = 2)
    private BigDecimal sellAmount;
    @Column(precision = 18, scale = 2)
    private BigDecimal receiveAmount;
    @Column(precision = 18, scale = 6)
    private BigDecimal rate;
    @Column(precision = 18, scale = 2)
    private BigDecimal feesBuyer = BigDecimal.ZERO;
    @Column(precision = 18, scale = 2)
    private BigDecimal feesSeller = BigDecimal.ZERO;
    private Instant lockExpiresAt;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

// getters/setters omitted for brevity
    public Long getOfferId() {
        return offerId;
    }

    public void setOfferId(Long offerId) {
        this.offerId = offerId;
    }

    public Long getSellerUserId() {
        return sellerUserId;
    }

    public void setSellerUserId(Long sellerUserId) {
        this.sellerUserId = sellerUserId;
    }

    public Long getBuyerUserId() {
        return buyerUserId;
    }

    public void setBuyerUserId(Long buyerUserId) {
        this.buyerUserId = buyerUserId;
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

    public BigDecimal getSellAmount() {
        return sellAmount;
    }

    public void setSellAmount(BigDecimal sellAmount) {
        this.sellAmount = sellAmount;
    }

    public BigDecimal getReceiveAmount() {
        return receiveAmount;
    }

    public void setReceiveAmount(BigDecimal receiveAmount) {
        this.receiveAmount = receiveAmount;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public BigDecimal getFeesBuyer() {
        return feesBuyer;
    }

    public void setFeesBuyer(BigDecimal feesBuyer) {
        this.feesBuyer = feesBuyer;
    }

    public BigDecimal getFeesSeller() {
        return feesSeller;
    }

    public void setFeesSeller(BigDecimal feesSeller) {
        this.feesSeller = feesSeller;
    }

    public Instant getLockExpiresAt() {
        return lockExpiresAt;
    }

    public void setLockExpiresAt(Instant lockExpiresAt) {
        this.lockExpiresAt = lockExpiresAt;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
