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
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Entity
@Table(name = "fx_investment_request_guard",
       indexes = {
           @Index(name = "idx_guard_order_type_time",
                  columnList = "emailAddress,orderRef,requestType,createdAt")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uq_guard_idem", columnNames = "idempotencyKey")
       })
@Data
public class InvestmentRequestGuard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String emailAddress;

    @Column(nullable = false, length = 64)
    private String orderRef;

    @Column(nullable = false, length = 32)
    private String requestType; // "TOPUP"

    @Column(nullable = false, length = 64)
    private String idempotencyKey;

    @Column(nullable = false)
    private Instant createdAt;
}

