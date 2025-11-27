/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.record;

import jakarta.persistence.Column;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class InvestmentPositionHistoryPojo {

    private Long id;

    private String productId;

    private LocalDate valuationDate; // daily snapshots

    private BigDecimal price;        // NAV / unit price for the day

    private BigDecimal units;        // units held that day

    private BigDecimal subscriptionAmount; // original invested amount (capital)

    private BigDecimal investmentAmount; // original invested amount (capital)

    private BigDecimal marketValue;  // value as at day end

    private BigDecimal gainLoss;     // marketValue - subscriptionAmount

    private LocalDate createdAt;

    private LocalDate maturityDate;

    private LocalDate activeDate;
   

    private String emailAddress;
    private String status;
    private String currency;
    private String investmentId;
    private String productName;
}
