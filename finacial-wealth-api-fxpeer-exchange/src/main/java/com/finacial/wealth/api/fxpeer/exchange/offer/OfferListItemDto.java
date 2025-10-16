/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.offer;

import java.math.BigDecimal;
import java.time.Instant;

/**
 *
 * @author olufemioshin
 */
public record OfferListItemDto(
        Long id,
        String currencySell,
        String currencyReceive,
        BigDecimal rate,
        BigDecimal qtyTotal,
        BigDecimal qtyAvailable,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        Instant expiryAt,
        boolean expired,
        String status,
        boolean showInTopDeals,
        
        String poweredBy,
        String correlationId
        ) {

}
