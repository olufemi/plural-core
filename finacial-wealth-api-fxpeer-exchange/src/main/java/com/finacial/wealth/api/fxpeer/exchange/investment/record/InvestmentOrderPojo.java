/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.record;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentOrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Data;
@Data
public class InvestmentOrderPojo {
    private Long id;                // local generated id (not DB)
    private String orderRef;
    private String emailAddress;
    private BigDecimal amount;
    private String currency;
    private InvestmentOrderStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    // getters/setters
}

