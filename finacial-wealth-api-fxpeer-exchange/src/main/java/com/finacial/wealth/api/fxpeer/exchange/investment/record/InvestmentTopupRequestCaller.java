/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.record;

import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class InvestmentTopupRequestCaller {
     private String orderRef;        // existing investment position/order ref
        private String amount;       // topup amount
      //  private String idempotencyKey;     // from client
        private String pin;
        private String currencyCode;
        private String productId;
}
