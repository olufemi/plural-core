/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.models.tranfaar.inflow;

import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class DepositWebhook {

    private String quote_id;
    private String paymentType;
    private String currency;
    private String status;
    private String email;
    private String amount;

}
