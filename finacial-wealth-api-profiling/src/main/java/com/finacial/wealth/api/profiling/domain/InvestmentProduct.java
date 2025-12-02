/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.domain;


import com.finacial.wealth.api.profiling.models.InvestmentType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import lombok.Data;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

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
    private String prospectusUrl;        // or JSON, detailed info

    @Lob
    private String metaJson;             // configuration/eligibility rules

    private BigDecimal percentageCurrValue;
    //private BigDecimal accruedInterest; 


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

    // getters/setters/constructors
}
