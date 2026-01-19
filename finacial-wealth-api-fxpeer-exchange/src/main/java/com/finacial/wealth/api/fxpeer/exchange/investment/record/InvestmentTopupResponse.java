/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.record;

import java.math.BigDecimal;

/**
 *
 * @author olufemioshin
 */
public record InvestmentTopupResponse(
        String orderRef,
        BigDecimal topupAmount,
        BigDecimal newCapital,
        BigDecimal newCurrentValue,
        String status
) {}

