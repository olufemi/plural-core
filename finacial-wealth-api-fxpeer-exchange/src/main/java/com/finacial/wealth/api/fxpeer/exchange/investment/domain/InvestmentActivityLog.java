/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.domain;

import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.ActivityType;
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentPositionStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author olufemioshin
 */
@Entity
@Table(name = "investment_activity_log")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestmentActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private Long positionId;

    private String orderRef;

    @Enumerated(EnumType.STRING)
    private ActivityType activityType;
     private InvestmentPositionStatus investmentPositionStatus;

    private BigDecimal amount;

    private String description;

    private LocalDateTime createdAt;
}
