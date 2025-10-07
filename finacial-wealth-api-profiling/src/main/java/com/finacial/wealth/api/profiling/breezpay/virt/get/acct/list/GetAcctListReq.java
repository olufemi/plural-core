/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.breezpay.virt.get.acct.list;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author olufemioshin
 */
public class GetAcctListReq {

    @JsonProperty("merchant_id")
    private String merchantId;
    @JsonProperty("channel_code")
    private String channelCode;
    @JsonProperty("page_number")
    private String pageNumber;
    @JsonProperty("page_size")
    private String pageSize;

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public String getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(String pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getPageSize() {
        return pageSize;
    }

    public void setPageSize(String pageSize) {
        this.pageSize = pageSize;
    }

}
