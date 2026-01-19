/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.service;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentOrder;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentPosition;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class ActivityService {

    public void logInvestmentSubscription(InvestmentOrder order,
            InvestmentPosition position,
            java.math.BigDecimal grossDebit) {

        // TODO: integrate with your existing Activity/Notification module
        System.out.println("[Activity] Subscription SUCCESS: " + order.getOrderRef());
    }

    public void logInvestmentLiquidation(InvestmentOrder order,
            InvestmentPosition position) {

        // TODO: integrate with your existing Activity/Notification module
        System.out.println("[Activity] Liquidation SUCCESS: " + order.getOrderRef());
    }
    
     public void logInvestmentTopup(InvestmentOrder order,
            InvestmentPosition position) {

        // TODO: integrate with your existing Activity/Notification module
        System.out.println("[Activity] Liquidation SUCCESS: " + order.getOrderRef());
    }
    
   
      

    public void logInterestCapitalization(
            InvestmentPosition position, BigDecimal accrued, LocalDate today) {

        // TODO: integrate with your existing Activity/Notification module
        System.out.println("[Activity] Liquidation SUCCESS: " + position.getOrderRef());
    }
}