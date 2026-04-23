/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.backoffice.integrations.fxpeer.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class InvestmentProductUpsertRequest {
    @NotBlank private String productCode;
    @NotBlank private String name;
    @NotNull private InvestmentType type;
    @NotBlank private String currency;
    @NotNull private BigDecimal minimumInvestmentAmount;
    @NotNull private ValuationMethod valuationMethod;

    private BigDecimal unitPrice;
    private BigDecimal yieldPa;
    private BigDecimal yieldYtd;
    private Integer tenorDays;
    private Boolean active;
    private LiquidationFeeAppliedTo liquidationFeeAppliedTo;
    private LiquidationFeeType liquidationFeeType;
    private BigDecimal liquidationFeeRate;
    private BigDecimal minLiquidationFee;
    private BigDecimal liquidationFeeCap;
    private Boolean lockEnabled;
    private Integer lockDays;
    private LiquidationFeeAppliedTo earlyLiquidationFeeAppliedTo;
    private LiquidationFeeType earlyLiquidationFeeType;
    private BigDecimal earlyLiquidationFeeRate;
    private BigDecimal earlyLiquidationFeeCap;

    private String partnerProductCode;
    private String prospectusUrl;
    private String metaJson;
    private String enableProduct;

    private BigDecimal percentageCurrValue;

    private ScheduleMode scheduleMode;
    private InterestAccrueType interestAccrueType;
    private InterestCapitalization interestCapitalization;

    private Long settlementDelayMinutes;
    private Long tenorMinutes;
    private Boolean maturityAtEndOfDay;

    private Instant settlementAt;
    private Instant maturityAt;

    @NotNull private LocalTime subscriptionCutOffTime;
}
