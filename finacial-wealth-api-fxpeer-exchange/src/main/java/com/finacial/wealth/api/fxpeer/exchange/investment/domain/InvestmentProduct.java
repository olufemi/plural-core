/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.domain;

import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentType;
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
    
    

    // getters/setters/constructors
}

