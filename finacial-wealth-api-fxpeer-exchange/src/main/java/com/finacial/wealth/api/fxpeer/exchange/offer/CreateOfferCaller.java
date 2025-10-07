/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.offer;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class CreateOfferCaller {

    String currencySell;
    String currencyReceive;
    String rate;
    String qtyTotal;
    private String expiredAt;

    @NotNull(message = "min is required")
    private String minAmount;

    @NotNull(message = "max is required")
    private String maxAmount;
    private boolean showInTopDeals;
    private String pin;

}
