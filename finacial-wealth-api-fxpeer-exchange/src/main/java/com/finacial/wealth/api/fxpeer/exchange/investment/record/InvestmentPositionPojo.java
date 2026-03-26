/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.record;

/**
 *
 * @author olufemioshin
 */
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Data
public class InvestmentPositionPojo {

    private Long id; // local response id, not DB id
    private String emailAddress;
    private String walletId;
    private Long productId;
    private String productName;
    private String investmentId;

    private BigDecimal units;
    private BigDecimal investedAmount;
    private BigDecimal marketValue;
    private BigDecimal accruedInterest;
    private BigDecimal totalAccruedInterest;
    private BigDecimal reservedLiquidationAmount;

    private String status;

    private Instant createdAt;
    private Instant updatedAt;
    private Instant settlementAt;
    private Instant maturityAt;

    private LocalDate interestStartDate;
    private String interestCapitalization;
    private LocalDate lastCapitalizationDate;
    private String currency;
    private BigDecimal minimumAmount;
    private BigDecimal price;
}
