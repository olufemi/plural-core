/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.offer;

import com.finacial.wealth.api.fxpeer.exchange.common.CurrencyCode;
import java.math.BigDecimal;
import java.time.Instant;

/**
 *
 * @author olufemioshin
 */
public record CreateOfferRq(CurrencyCode currencySell, CurrencyCode currencyReceive,
        BigDecimal rate, BigDecimal qtyTotal, Instant expiryAt) {

}
