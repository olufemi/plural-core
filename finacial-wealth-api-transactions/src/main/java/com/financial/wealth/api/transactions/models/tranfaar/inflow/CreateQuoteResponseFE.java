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
public class CreateQuoteResponseFE {

    public String quoteId;
    public String status;
    public String totalFees;
    public String feeCurrency;
    public String createdAt;
    public String validUntil;
    public String firstName;
    public String lastName;
    public String bankName;
    public String currencyCode;
    public String countryCode;
    public String email;
}
