/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.limits;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author olufemioshin
 */
// Response object for the white UI screenshots
// It supports both tabs: LIMIT_ON_SEND (debit) and LIMIT_ON_RECEIVE (credit)
public class LimitsUiResponse implements Serializable {

    private int statusCode;
    private String description;

    private String currency; // profiling-known
    private String accountNumber;

// Send tab
    private BigDecimal sendSingleTransactionLimit; // debit single
    private List<UiLimitLine> sendPeriodLimits; // daily/weekly/monthly

// Receive tab
    private BigDecimal receiveSingleTransactionLimit; // credit single (can be null/unlimited)
    private List<UiLimitLine> receivePeriodLimits;

// optional: raw ledger summary debug
    private Map<String, Object> meta;

    public LimitsUiResponse() {
        this.sendPeriodLimits = new ArrayList<UiLimitLine>();
        this.receivePeriodLimits = new ArrayList<UiLimitLine>();
        this.meta = new HashMap<String, Object>();
    }

// getters/setters omitted for brevity (generate with IDE)
    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public BigDecimal getSendSingleTransactionLimit() {
        return sendSingleTransactionLimit;
    }

    public void setSendSingleTransactionLimit(BigDecimal sendSingleTransactionLimit) {
        this.sendSingleTransactionLimit = sendSingleTransactionLimit;
    }

    public List<UiLimitLine> getSendPeriodLimits() {
        return sendPeriodLimits;
    }

    public void setSendPeriodLimits(List<UiLimitLine> sendPeriodLimits) {
        this.sendPeriodLimits = sendPeriodLimits;
    }

    public BigDecimal getReceiveSingleTransactionLimit() {
        return receiveSingleTransactionLimit;
    }

    public void setReceiveSingleTransactionLimit(BigDecimal receiveSingleTransactionLimit) {
        this.receiveSingleTransactionLimit = receiveSingleTransactionLimit;
    }

    public List<UiLimitLine> getReceivePeriodLimits() {
        return receivePeriodLimits;
    }

    public void setReceivePeriodLimits(List<UiLimitLine> receivePeriodLimits) {
        this.receivePeriodLimits = receivePeriodLimits;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }
}
