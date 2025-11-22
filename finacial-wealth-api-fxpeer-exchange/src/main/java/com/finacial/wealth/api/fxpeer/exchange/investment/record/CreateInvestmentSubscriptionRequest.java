/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.record;

import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentProduct;
import java.math.BigDecimal;

/**
 *
 * @author olufemioshin
 */
public record CreateInvestmentSubscriptionRequest(
        String walletId,
        Long productId,
        BigDecimal fees, 
        BigDecimal units, 
        BigDecimal grossDebit, 
        BigDecimal amount,          // user-entered amount
        String idempotencyKey,     // e.g. from UI or UUID on client
        String emailAddress, 
        String phoneNumber,InvestmentProduct product,
        String orderRef
) {

}
