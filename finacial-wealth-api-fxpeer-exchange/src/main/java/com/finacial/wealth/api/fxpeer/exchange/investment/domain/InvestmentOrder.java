/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.domain;

import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentOrderStatus;
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentOrderType;
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.LiquidationApprovalStatus;
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

/**
 *
 * @author olufemioshin
 */
@Entity
@Data
@Table(name = "fx_investment_order",
        indexes = {
            @Index(name = "idx_invest_order_emailAddress", columnList = "emailAddress"),
            @Index(name = "idx_invest_order_idempotency", columnList = "idempotencyKey", unique = true)
        })
public class InvestmentOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64, unique = true)
    private String orderRef;     // public ref for UI & receipts

    @Column(nullable = false, length = 64, unique = true)
    private String parentOrderRef;     // public ref for UI & receipts

    @Column(nullable = false, length = 64)
    private String idempotencyKey; // for duplicate protection

    @Column(nullable = false, length = 64)
    private String emailAddress;

    @Column(nullable = false, length = 64)
    private String walletId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private InvestmentProduct product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private InvestmentPosition position; // null for FIRST subscription

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private InvestmentOrderType type;    // SUBSCRIPTION or LIQUIDATION

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private InvestmentOrderStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;           // gross investment or liquidation amount

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amountBalance;           // investment balance

    @Column(precision = 19, scale = 8)
    private BigDecimal units;            // units bought or sold

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal fees;             // total fees

    @Column(precision = 19, scale = 2)
    private BigDecimal tax;              // optional

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal netAmount;        // amount to debit/credit wallet after fees+tax

    @Column(length = 64)
    private String partnerOrderId;

    @Column(length = 512)
    private String failureReason;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    // getters/setters
}
