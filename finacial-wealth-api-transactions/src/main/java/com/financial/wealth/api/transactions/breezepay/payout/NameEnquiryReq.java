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

public class NameEnquiryReq implements Serializable {

    @JsonProperty("credit_account")
    @SerializedName("credit_account")
    private String creditAccount;

    @JsonProperty("credit_bank_code")
    @SerializedName("credit_bank_code")
    private String creditBankCode;

    @JsonProperty("sender_account")
    @SerializedName("sender_account")
    private String senderAccount;

    @JsonProperty("msg_id")
    @SerializedName("msg_id")
    private String msgId;

    @JsonProperty("channel_code")
    @SerializedName("channel_code")
    private String channelCode;

    public NameEnquiryReq() {}

    public NameEnquiryReq(String creditAccount, String creditBankCode, String senderAccount,
                            String msgId, String channelCode) {
        this.creditAccount = creditAccount;
        this.creditBankCode = creditBankCode;
        this.senderAccount = senderAccount;
        this.msgId = msgId;
        this.channelCode = channelCode;
    }

    public String getCreditAccount() { return creditAccount; }
    public void setCreditAccount(String creditAccount) { this.creditAccount = creditAccount; }

    public String getCreditBankCode() { return creditBankCode; }
    public void setCreditBankCode(String creditBankCode) { this.creditBankCode = creditBankCode; }

    public String getSenderAccount() { return senderAccount; }
    public void setSenderAccount(String senderAccount) { this.senderAccount = senderAccount; }

    public String getMsgId() { return msgId; }
    public void setMsgId(String msgId) { this.msgId = msgId; }

    public String getChannelCode() { return channelCode; }
    public void setChannelCode(String channelCode) { this.channelCode = channelCode; }

    @Override
    public String toString() {
        return "NipCreditRequest{" +
                "creditAccount='" + creditAccount + '\'' +
                ", creditBankCode='" + creditBankCode + '\'' +
                ", senderAccount='" + senderAccount + '\'' +
                ", msgId='" + msgId + '\'' +
                ", channelCode='" + channelCode + '\'' +
                '}';
    }
}


