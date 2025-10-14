/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.breezepay.payin;

import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class WebHookRequest {
    private String processId;
    private String amount;
    private String virtualAccount;
}
