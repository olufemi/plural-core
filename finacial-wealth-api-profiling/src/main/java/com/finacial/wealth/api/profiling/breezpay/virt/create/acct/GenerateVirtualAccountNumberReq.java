/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.breezpay.virt.create.acct;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import org.hibernate.validator.constraints.NotBlank;

/**
 *
 * @author olufemioshin
 */
public class GenerateVirtualAccountNumberReq {

    @JsonProperty("customer_id")

    private String customerId;

    @JsonProperty("merchant_id")

    private String merchantId;

    @JsonProperty("customer_name")

    private String customerName;

    @JsonProperty("bvn")

    @Pattern(regexp = "\\d{11}", message = "bvn must be 11 digits")
    private String bvn;

    @JsonProperty("customer_email")
    private String customerEmail;

    @JsonProperty("customer_phone")
   
    // Nigeria local: 11 digits starting with 0 (tweak if you prefer E.164)
    @Pattern(regexp = "^0\\d{10}$", message = "phone must be 11 digits starting with 0")
    private String customerPhone;

    @JsonProperty("request_authorizer")

    private String requestAuthorizer;

    @JsonProperty("currency")

    @Pattern(regexp = "^[A-Z]{3}$", message = "currency must be an ISO 4217 code (e.g., NGN)")
    private String currency;

    @JsonProperty("forcedebit")

    @Pattern(regexp = "^[YN]$", message = "forcedebit must be 'Y' or 'N'")
    private String forceDebit;

    @JsonProperty("channel_code")

    private String channelCode;

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getBvn() {
        return bvn;
    }

    public void setBvn(String bvn) {
        this.bvn = bvn;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getRequestAuthorizer() {
        return requestAuthorizer;
    }

    public void setRequestAuthorizer(String requestAuthorizer) {
        this.requestAuthorizer = requestAuthorizer;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getForceDebit() {
        return forceDebit;
    }

    public void setForceDebit(String forceDebit) {
        this.forceDebit = forceDebit;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

}
