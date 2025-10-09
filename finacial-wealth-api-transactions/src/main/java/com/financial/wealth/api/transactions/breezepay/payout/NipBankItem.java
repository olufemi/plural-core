/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.breezepay.payout;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author olufemioshin
 */
public class NipBankItem {
    @JsonProperty("bankCode") private String bankCode;
    @JsonProperty("bankName") private String bankName;
    public String getBankCode() { return bankCode; }
    public void setBankCode(String bankCode) { this.bankCode = bankCode; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
}
