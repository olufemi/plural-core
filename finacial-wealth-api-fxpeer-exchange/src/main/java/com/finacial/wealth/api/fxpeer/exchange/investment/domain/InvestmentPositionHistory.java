/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.domain;

/**
 *
 * @author olufemioshin
 */
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

@Entity
@Data
@Table(
        name = "fx_investment_position_history",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_position_date",
                    columnNames = {"position_id", "valuation_date"}
            )
        },
        indexes = {
            @Index(name = "idx_hist_position", columnList = "position_id"),
            @Index(name = "idx_hist_date", columnList = "valuation_date")
        }
)
public class InvestmentPositionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many history rows per position
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    private InvestmentPosition position;

    @Column(name = "valuation_date", nullable = false)
    private LocalDate valuationDate; // daily snapshots

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal price;        // NAV / unit price for the day

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal units;        // units held that day

    @Column(name = "subscription_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal subscriptionAmount; // original invested amount (capital)

    @Column(name = "investment_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal investmentAmount; // original invested amount (capital)

    @Column(name = "market_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal marketValue;  // value as at day end

    @Column(name = "gain_loss", nullable = false, precision = 19, scale = 2)
    private BigDecimal gainLoss;     // marketValue - subscriptionAmount

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = true)
    private Instant maturityDate;

    @Column(nullable = true)
    private Instant activeDate;

    private String emailAddress;
    
    private BigDecimal minimumAmount;

    private String investmentId;
    private String productName;
    @Column(name = "daily_interest", precision = 19, scale = 2)
    private BigDecimal dailyInterest;

    @Column(name = "total_interest", precision = 19, scale = 2)
    private BigDecimal totalInterest;
    
     @Column(name = "accrued_interest", precision = 19, scale = 2)
    private BigDecimal accruedInterest;

}
