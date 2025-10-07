/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.breezpay.virt.create.acct;

/**
 *
 * @author olufemioshin
 */
import com.fasterxml.jackson.annotation.JsonProperty;

public class ResponseData {

    @JsonProperty("virtual_acct_no")
    private String virtualAcctNo;

    @JsonProperty("virtual_acct_name")
    private String virtualAcctName;

    @JsonProperty("expiry_datetime")
    private String expiryDatetime; // may be an empty string from API

    public String getVirtualAcctNo() {
        return virtualAcctNo;
    }

    public void setVirtualAcctNo(String virtualAcctNo) {
        this.virtualAcctNo = virtualAcctNo;
    }

    public String getVirtualAcctName() {
        return virtualAcctName;
    }

    public void setVirtualAcctName(String virtualAcctName) {
        this.virtualAcctName = virtualAcctName;
    }

    public String getExpiryDatetime() {
        return expiryDatetime;
    }

    public void setExpiryDatetime(String expiryDatetime) {
        this.expiryDatetime = expiryDatetime;
    }

}
