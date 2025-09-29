/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.models.tranfaar.outflow;

import com.financial.wealth.api.transactions.models.tranfaar.inflow.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class CreateQuoteWithdrawal {

    @JsonProperty("source_currency")
    private String sourceCurrency;

    @JsonProperty("target_currency")
    private String targetCurrency;

    @JsonProperty("source_amount")
    private String sourceAmount;

    @JsonProperty("destination_amount")
    private String destinationAmount;

    @JsonProperty("fee_config_id")
    private String feeConfigId;

    private String narration;

    @JsonProperty("expected_source_interac_email")
    private String expectedSourceInteracEmail;

    private String tz;

    @JsonProperty("quote_type")
    private String quoteType;

    public CreateQuoteWithdrawal() {
    }

    // getters & setters
    public String getSourceCurrency() {
        return sourceCurrency;
    }

    public void setSourceCurrency(String sourceCurrency) {
        this.sourceCurrency = sourceCurrency;
    }

    public String getTargetCurrency() {
        return targetCurrency;
    }

    public void setTargetCurrency(String targetCurrency) {
        this.targetCurrency = targetCurrency;
    }

    public String getSourceAmount() {
        return sourceAmount;
    }

    public void setSourceAmount(String sourceAmount) {
        this.sourceAmount = sourceAmount;
    }

    public String getDestinationAmount() {
        return destinationAmount;
    }

    public void setDestinationAmount(String destinationAmount) {
        this.destinationAmount = destinationAmount;
    }

    public String getFeeConfigId() {
        return feeConfigId;
    }

    public void setFeeConfigId(String feeConfigId) {
        this.feeConfigId = feeConfigId;
    }

    public String getNarration() {
        return narration;
    }

    public void setNarration(String narration) {
        this.narration = narration;
    }

    public String getExpectedSourceInteracEmail() {
        return expectedSourceInteracEmail;
    }

    public void setExpectedSourceInteracEmail(String expectedSourceInteracEmail) {
        this.expectedSourceInteracEmail = expectedSourceInteracEmail;
    }

    public String getTz() {
        return tz;
    }

    public void setTz(String tz) {
        this.tz = tz;
    }

    public String getQuoteType() {
        return quoteType;
    }

    public void setQuoteType(String quoteType) {
        this.quoteType = quoteType;
    }
}
