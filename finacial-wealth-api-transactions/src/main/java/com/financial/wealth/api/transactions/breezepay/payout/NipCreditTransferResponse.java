/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.breezepay.payout;

/**
 *
 * @author olufemioshin
 */
import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

public class NipCreditTransferResponse implements Serializable {

    @JsonProperty("response_code")
    @SerializedName("response_code")
    private String responseCode;

    @JsonProperty("response_message")
    @SerializedName("response_message")
    private String responseMessage;

    @JsonProperty("transaction_reference")
    @SerializedName("transaction_reference")
    private String transactionReference;

    public NipCreditTransferResponse() {
    }

    public NipCreditTransferResponse(String responseCode, String responseMessage, String transactionReference) {
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.transactionReference = transactionReference;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    @Override
    public String toString() {
        return "NipInternalTransferResponse{"
                + "responseCode='" + responseCode + '\''
                + ", responseMessage='" + responseMessage + '\''
                + ", transactionReference='" + transactionReference + '\''
                + '}';
    }
}
