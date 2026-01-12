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
public record LiquidateInvestmentRequest(
       // Long positionId,
        BigDecimal liquidationAmount, // if null => full liquidation
        String orderId,
        boolean fullLiquidation
        
       
) {}