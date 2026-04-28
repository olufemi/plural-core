/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.models.tranfaar.inflow;

import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class CreateQuoteFE {

    private String sourceAmount;

    private String destinationAmount;

    private String sourceCurrency;

    private String targetCurrency;

    private String quoteType;

    private String tz;

    private String narration;

    private String expectedSourceInteracEmail;

    private String originReference;

    private String onBehalfOf;

    private String beneficiaryId;

    private String userTag;

    private String paymentNetwork;

    private String paymentAddress;

    private String rail;
}
