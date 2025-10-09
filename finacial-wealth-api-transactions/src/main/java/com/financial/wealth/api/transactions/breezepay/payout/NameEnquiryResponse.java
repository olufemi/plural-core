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

public class NameEnquiryResponse implements Serializable {

    @JsonProperty("response_code")
    @SerializedName("response_code")
    private String responseCode;

    @JsonProperty("response_message")
    @SerializedName("response_message")
    private String responseMessage;

    @JsonProperty("response_data")
    @SerializedName("response_data")
    private ResponseData responseData;

    public NameEnquiryResponse() {
    }

    public NameEnquiryResponse(String responseCode, String responseMessage, ResponseData responseData) {
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.responseData = responseData;
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

    public ResponseData getResponseData() {
        return responseData;
    }

    public void setResponseData(ResponseData responseData) {
        this.responseData = responseData;
    }

    @Override
    public String toString() {
        return "NipCreditResponse{"
                + "responseCode='" + responseCode + '\''
                + ", responseMessage='" + responseMessage + '\''
                + ", responseData=" + responseData
                + '}';
    }

    // ---- nested DTO ----
    public static class ResponseData implements Serializable {

        @JsonProperty("account_name")
        @SerializedName("account_name")
        private String accountName;

        @JsonProperty("bank_name")
        @SerializedName("bank_name")
        private String bankName;

        @JsonProperty("session_id")
        @SerializedName("session_id")
        private String sessionId;

        @JsonProperty("provider_code")
        @SerializedName("provider_code")
        private String providerCode;

        public ResponseData() {
        }

        public ResponseData(String accountName, String bankName, String sessionId, String providerCode) {
            this.accountName = accountName;
            this.bankName = bankName;
            this.sessionId = sessionId;
            this.providerCode = providerCode;
        }

        public String getAccountName() {
            return accountName;
        }

        public void setAccountName(String accountName) {
            this.accountName = accountName;
        }

        public String getBankName() {
            return bankName;
        }

        public void setBankName(String bankName) {
            this.bankName = bankName;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getProviderCode() {
            return providerCode;
        }

        public void setProviderCode(String providerCode) {
            this.providerCode = providerCode;
        }

        @Override
        public String toString() {
            return "ResponseData{"
                    + "accountName='" + accountName + '\''
                    + ", bankName='" + bankName + '\''
                    + ", sessionId='" + sessionId + '\''
                    + ", providerCode='" + providerCode + '\''
                    + '}';
        }
    }
}
