/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.record;

/**
 *
 * @author olufemioshin
 */
import java.math.BigDecimal;

public record PartnerSubscriptionResponse(
        String partnerOrderId,       // unique reference from partner
        boolean settled,             // does partner say transaction already settled?
        BigDecimal settledUnits,     // units allocated at settlement (optional)
        BigDecimal settledAmount,    // amount confirmed by partner (optional)
        String message               // partner message / description
) {}
