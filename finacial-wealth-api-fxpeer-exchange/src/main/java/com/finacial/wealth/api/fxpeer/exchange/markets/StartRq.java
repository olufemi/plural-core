/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.markets;

/**
 *
 * @author olufemioshin
 */
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/** Request payload for starting a negotiation (POST /api/negs). */
public record StartRq(
        @NotNull Long offerId,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotNull @DecimalMin("0.000001") BigDecimal rate,
        long ttlSeconds
) {}
