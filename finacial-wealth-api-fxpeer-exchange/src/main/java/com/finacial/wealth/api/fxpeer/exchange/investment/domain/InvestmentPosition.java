/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.domain;

import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InterestCapitalization;
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentPositionStatus;
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

/**
 *
 * @author olufemioshin
 */
@Entity
@Data
@Table(name = "fx_investment_position")
public class InvestmentPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String emailAddress;

    @Column(nullable = false, length = 64)
    private String walletId;             // link to contribution wallet

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private InvestmentProduct product;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal units;            // units currently held

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal investedAmount;   // original capital // capital

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal currentValue;     // capital + accrued interest // investedAmount + accruedInterest

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal accruedInterest;  // for display // not yet capitalized
    private BigDecimal totalAccruedInterest; //lifetime (optional audit)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private InvestmentPositionStatus status = InvestmentPositionStatus.ACTIVE;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    private String orderRef;
    private String productName;

    // FIXED mode fields (still allowed)
    @Column
    private Instant settlementAt;

    @Column
    private Instant maturityAt;

    @Column(name = "interest_start_date", nullable = false)
    private LocalDate interestStartDate; //T+1 accrual start

    @Enumerated(EnumType.STRING)
    private InterestCapitalization interestCapitalization;
    
    private LocalDate lastCapitalizationDate;    // prevents double sweeps

    // getters/setters
}
