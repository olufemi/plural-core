/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.models.tranfaar.inflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 *
 * @author olufemioshin
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateQuoteResponse {

    public String quoteId;
    public String status;
    public String type;
    public Money source;
    public Money target;
    public String rate;
    public Fees fees;
    public String totalPayable;
    public String narration;
    public Timeline timeline;
    public Beneficiary beneficiary;
    public FundingMethod fundingMethod;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Money {

        public String currency;
        public String amount;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Fees {

        public String processingFee;
        public String exchangeFee;
        public String payoutFee;
        public String totalFees;
        public String feeCurrency;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Timeline {

        public String createdAt;
        public String validUntil;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Beneficiary {

        public String id;
        public String firstName;
        public String lastName;
        public String bankName;
        public String currencyCode;
        public String countryCode;
        public String email;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FundingMethod {

        public String type;
        public String instructions;
    }
}
