/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.models.tranfaar.outflow;

import com.financial.wealth.api.transactions.models.tranfaar.inflow.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author olufemioshin
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateQuoteWithResponse {

    @JsonProperty("quote_id")
    public String quoteId;
    public String status;
    public String type;
    public Money source;
    public Money target;
    public String rate;
    public Fees fees;
    @JsonProperty("total_payable")
    public String totalPayable;
    public String narration;
    public Timeline timeline;
    public Beneficiary beneficiary;
    @JsonProperty("funding_method")
    public FundingMethod fundingMethod;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Money {

        public String currency;
        public String amount;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Fees {

        @JsonProperty("processing_fee")
        public String processingFee;
        @JsonProperty("exchange_fee")
        public String exchangeFee;
        @JsonProperty("payout_fee")
        public String payoutFee;
        @JsonProperty("total_fees")
        public String totalFees;
        @JsonProperty("fee_currency")
        public String feeCurrency;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Timeline {

        @JsonProperty("created_at")
        public String createdAt;
        @JsonProperty("valid_until")
        public String validUntil;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Beneficiary {

        public String id;
        @JsonProperty("first_name")
        public String firstName;
        @JsonProperty("last_name")
        public String lastName;
        @JsonProperty("bank_name")
        public String bankName;
        @JsonProperty("currency_code")
        public String currencyCode;
        @JsonProperty("country_code")
        public String countryCode;
        public String email;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FundingMethod {

        public String type;
        public String instructions;
    }
}
