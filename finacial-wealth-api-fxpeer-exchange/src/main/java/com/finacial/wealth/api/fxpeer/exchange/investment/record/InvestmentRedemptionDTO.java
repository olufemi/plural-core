/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.record;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author olufemioshin
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvestmentRedemptionDTO {

    private String investmentId;
    private String productName;
    private LocalDate valuationDate;
    private BigDecimal units;
    private BigDecimal subscriptionAmount;
    private BigDecimal marketValue;
    private BigDecimal gainLoss;
    private String status;
    private Instant maturityDate;
}
