/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.domain;
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
@Table(name = "fx_investment_valuation_history")
public class InvestmentValuationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    private InvestmentPosition position;

    @Column(nullable = false)
    private LocalDate valuationDate;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal value;   // total value at end of day

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal capital;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal interestAccrued;  // interest during period

    // getters/setters
}

