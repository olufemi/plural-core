/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.record;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class InvestmentProductRecord {

    private String productId;

    private String productCode;          // internal code

    private String name;

    private String InvestmentType;         // MUTUAL_FUND, BOND, etc.

    private String currency;             // "NGN", "GBP", ...

    private BigDecimal minimumInvestmentAmount;

    private String description;
    private String issuerName;
    private String fundManager;
    private String riskRating;
    private Integer minimumHoldingDays;
    private Integer maximumHoldingDays;
    private BigDecimal maximumTotalRaise;
    private Boolean autoCloseAtCapacity;
    private Integer liquidationFrequencyLimit;
    private String liquidationFrequencyPeriod;
    private String maturityDefaultAction;

    private BigDecimal unitPrice;        // optional, for unit-based products

    private BigDecimal yieldPa;          // Annual Yield (Per Annum Yield)

    private BigDecimal yieldYtd;         // Year-To-Date Yield

    private String valuationMethod;
    private String liquidationFeeAppliedTo;
    private String liquidationFeeType;
    private BigDecimal liquidationFeeRate;
    private BigDecimal minLiquidationFee;
    private BigDecimal liquidationFeeCap;
    private Boolean lockEnabled;
    private Integer lockDays;
    private String earlyLiquidationFeeAppliedTo;
    private String earlyLiquidationFeeType;
    private BigDecimal earlyLiquidationFeeRate;
    private BigDecimal earlyLiquidationFeeCap;

    private Integer tenorDays;           // optional

    private boolean active = true;

    private String enableProduct;

    private String partnerProductCode;   // identifier known by partner

    private String prospectusUrl;

    private String metaJson;

    private BigDecimal percentageCurrValue;

    private String scheduleMode;

    private String interestAccrueType;

    private String interestCapitalization;

    private Long settlementDelayMinutes;

    private Long tenorMinutes = 30L * 24L * 60L; // default 30 days in minutes (editable)

    private Boolean maturityAtEndOfDay = true; // optional behavior

    private Instant settlementAt;

    private Instant maturityAt;

    private LocalTime subscriptionCutOffTime;

    private Instant createdAt;

    private Instant updatedAt;

}
