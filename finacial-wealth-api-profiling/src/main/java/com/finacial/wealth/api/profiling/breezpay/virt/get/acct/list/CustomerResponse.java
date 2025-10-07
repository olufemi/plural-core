/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.breezpay.virt.get.acct.list;

import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class CustomerResponse {
     private String response_code;
    private String response_message;
    private ResponseData response_data;
}
