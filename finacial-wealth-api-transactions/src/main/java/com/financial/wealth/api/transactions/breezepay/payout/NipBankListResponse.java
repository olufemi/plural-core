/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.breezepay.payout;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
public class NipBankListResponse {
    @JsonProperty("ResponseCode") private String responseCode;
    @JsonProperty("ResponseMessage") private String responseMessage;
    @JsonProperty("getNipBankListResponse") private List<NipBankItem> getNipBankListResponse;

    public String getResponseCode() { return responseCode; }
    public void setResponseCode(String responseCode) { this.responseCode = responseCode; }
    public String getResponseMessage() { return responseMessage; }
    public void setResponseMessage(String responseMessage) { this.responseMessage = responseMessage; }
    public List<NipBankItem> getGetNipBankListResponse() { return getNipBankListResponse; }
    public void setGetNipBankListResponse(List<NipBankItem> v) { this.getNipBankListResponse = v; }
}
