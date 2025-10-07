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
public class GetCusmerDetailsResponse {

    @JsonProperty("customer_id")
    private String customerId;

    @JsonProperty("customer_name")
    private String customerName;

    @JsonProperty("virtual_account_no")
    private String virtualAccountNo;

    @JsonProperty("virtual_account_name")
    private String virtualAccountName;

    @JsonProperty("customer_status")
    private String customerStatus;

}
