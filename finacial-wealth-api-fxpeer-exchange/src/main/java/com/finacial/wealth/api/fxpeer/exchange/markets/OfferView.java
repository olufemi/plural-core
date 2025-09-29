/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.markets;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.common.CurrencyCode;
import com.finacial.wealth.api.fxpeer.exchange.rating.model.SellerStats;
import java.math.BigDecimal;

public record OfferView(
        Long offerId,
        Long sellerUserId,
        CurrencyCode currencySell,
        CurrencyCode currencyReceive,
        BigDecimal rate,
        BigDecimal qtyAvailable,
        BigDecimal qtyTotal,
        SellerStats seller) {

}
