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
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;

public class NipCreditAccountTransferRequest implements Serializable {

    @JsonProperty("debit_merchant_id")
    @SerializedName("debit_merchant_id")
    private String debitMerchantId;

    @JsonProperty("debit_customer_id")
    @SerializedName("debit_customer_id")
    private String debitCustomerId;

    @JsonProperty("credit_account")
    @SerializedName("credit_account")
    private String creditAccount;

    @JsonProperty("credit_bank_code")
    @SerializedName("credit_bank_code")
    private String creditBankCode;

    @JsonProperty("debit_virtual_account")
    @SerializedName("debit_virtual_account")
    private String debitVirtualAccount;

    @JsonProperty("transaction_amount")
    @SerializedName("transaction_amount")
    private BigDecimal transactionAmount;

    @JsonProperty("transaction_narration")
    @SerializedName("transaction_narration")
    private String transactionNarration;

    @JsonProperty("transaction_reference")
    @SerializedName("transaction_reference")
    private String transactionReference;

    @JsonProperty("channel_code")
    @SerializedName("channel_code")
    private String channelCode;

    public NipCreditAccountTransferRequest() {}

    public NipCreditAccountTransferRequest(String debitMerchantId, String debitCustomerId,
                                           String creditAccount, String creditBankCode,
                                           String debitVirtualAccount, BigDecimal transactionAmount,
                                           String transactionNarration, String transactionReference,
                                           String channelCode) {
        this.debitMerchantId = debitMerchantId;
        this.debitCustomerId = debitCustomerId;
        this.creditAccount = creditAccount;
        this.creditBankCode = creditBankCode;
        this.debitVirtualAccount = debitVirtualAccount;
        this.transactionAmount = transactionAmount;
        this.transactionNarration = transactionNarration;
        this.transactionReference = transactionReference;
        this.channelCode = channelCode;
    }

    public String getDebitMerchantId() { return debitMerchantId; }
    public void setDebitMerchantId(String debitMerchantId) { this.debitMerchantId = debitMerchantId; }

    public String getDebitCustomerId() { return debitCustomerId; }
    public void setDebitCustomerId(String debitCustomerId) { this.debitCustomerId = debitCustomerId; }

    public String getCreditAccount() { return creditAccount; }
    public void setCreditAccount(String creditAccount) { this.creditAccount = creditAccount; }

    public String getCreditBankCode() { return creditBankCode; }
    public void setCreditBankCode(String creditBankCode) { this.creditBankCode = creditBankCode; }

    public String getDebitVirtualAccount() { return debitVirtualAccount; }
    public void setDebitVirtualAccount(String debitVirtualAccount) { this.debitVirtualAccount = debitVirtualAccount; }

    public BigDecimal getTransactionAmount() { return transactionAmount; }
    public void setTransactionAmount(BigDecimal transactionAmount) { this.transactionAmount = transactionAmount; }

    public String getTransactionNarration() { return transactionNarration; }
    public void setTransactionNarration(String transactionNarration) { this.transactionNarration = transactionNarration; }

    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }

    public String getChannelCode() { return channelCode; }
    public void setChannelCode(String channelCode) { this.channelCode = channelCode; }

    @Override
    public String toString() {
        return "NipCreditAccountTransferRequest{" +
                "debitMerchantId='" + debitMerchantId + '\'' +
                ", debitCustomerId='" + debitCustomerId + '\'' +
                ", creditAccount='" + creditAccount + '\'' +
                ", creditBankCode='" + creditBankCode + '\'' +
                ", debitVirtualAccount='" + debitVirtualAccount + '\'' +
                ", transactionAmount=" + transactionAmount +
                ", transactionNarration='" + transactionNarration + '\'' +
                ", transactionReference='" + transactionReference + '\'' +
                ", channelCode='" + channelCode + '\'' +
                '}';
    }
}
