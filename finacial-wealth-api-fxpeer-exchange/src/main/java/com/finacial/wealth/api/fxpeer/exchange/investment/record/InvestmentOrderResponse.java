/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.record;

import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentOrderStatus;
import java.math.BigDecimal;

/**
 *
 * @author olufemioshin
 */
public record InvestmentOrderResponse(
        String orderRef,
        String productName,
        BigDecimal amount,
        BigDecimal fees,
        BigDecimal netAmount,
        InvestmentOrderStatus status
) {}