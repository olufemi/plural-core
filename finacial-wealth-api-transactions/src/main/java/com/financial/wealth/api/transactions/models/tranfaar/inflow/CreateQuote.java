/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.models.tranfaar.inflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class CreateQuote {

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

    @JsonProperty("origin_reference")
    private String originReference;

    @JsonProperty("on_behalf_of")
    private String onBehalfOf;

    @JsonProperty("beneficiary_id")
    private String beneficiaryId;

    @JsonProperty("user_tag")
    private String userTag;

    @JsonProperty("payment_network")
    private String paymentNetwork;

    @JsonProperty("payment_address")
    private String paymentAddress;

    private String rail;
}
