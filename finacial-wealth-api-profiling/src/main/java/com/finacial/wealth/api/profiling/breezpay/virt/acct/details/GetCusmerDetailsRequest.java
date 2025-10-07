/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.breezpay.virt.acct.details;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class GetCusmerDetailsRequest {

    @JsonProperty("customer_id")
    private String customerId;

    @JsonProperty("merchant_id")
    private String merchantId;

    // May be an empty string from the API
    @JsonProperty("account_no")
    private String accountNo;

    @JsonProperty("channel_code")
    private String channelCode;

    private ResponseData responseData;
}
