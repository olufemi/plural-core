/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.domain;

import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InterestAccrueType;
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InterestCapitalization;
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentType;
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.ScheduleMode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Data;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 *
 * @author olufemioshin
 */
@Entity
@Data
@Table(name = "fx_investment_product")
public class InvestmentProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String productCode;          // internal code

    @Column(nullable = false, length = 512)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private InvestmentType type;         // MUTUAL_FUND, BOND, etc.

    @Column(nullable = false, length = 3)
    private String currency;             // "NGN", "GBP", ...

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal minimumInvestmentAmount;

    @Column(precision = 19, scale = 8)
    private BigDecimal unitPrice;        // optional, for unit-based products

    @Column(precision = 9, scale = 4)
    private BigDecimal yieldPa;          // annual yield

    @Column(precision = 9, scale = 4)
    private BigDecimal yieldYtd;         // YTD yield

    @Column
    private Integer tenorDays;           // optional

    @Column(nullable = false)
    private boolean active = true;

    @Column(length = 64)
    private String partnerProductCode;   // identifier known by partner

    @Lob
    @Column(length = 10000)
    private String prospectusUrl;        // or JSON, detailed info

    @Lob
    @Column(length = 10000)
    private String metaJson;             // configuration/eligibility rules

    private BigDecimal percentageCurrValue;
    //private BigDecimal accruedInterest; 

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ScheduleMode scheduleMode = ScheduleMode.RELATIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InterestAccrueType interestAccrueType = InterestAccrueType.DAILY;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InterestCapitalization interestCapitalization = InterestCapitalization.DAILY;

    // all RELATIVE values stored in minutes
    @Column(nullable = false)
    private Long settlementDelayMinutes = 0L;   // editable

    @Column(nullable = false)
    private Long tenorMinutes = 30L * 24L * 60L; // default 30 days in minutes (editable)

    @Column(nullable = false)
    private Boolean maturityAtEndOfDay = true; // optional behavior

    // FIXED mode fields (still allowed)
    @Column
    private Instant settlementAt;

    @Column
    private Instant maturityAt;

    @Column(name = "subscription_cutoff_time", nullable = false)
    private LocalTime subscriptionCutOffTime; // e.g. 16:30

    // getters/setters/constructors
}
