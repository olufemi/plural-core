/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.record;

import jakarta.validation.constraints.Min;
import java.time.LocalDate;

/**
 *
 * @author olufemioshin
 */
public record RedemptionInvestmentRequest(
        @Min(0)
        Integer page,
        @Min(1)
        Integer size,
        LocalDate fromDate,
        LocalDate toDate,
        String productName,
        String parentOrderId) {

}
