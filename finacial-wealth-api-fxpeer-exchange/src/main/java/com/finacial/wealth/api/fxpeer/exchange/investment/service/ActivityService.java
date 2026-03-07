/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.service;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentActivityLog;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentOrder;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentPosition;
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.ActivityType;
import com.finacial.wealth.api.fxpeer.exchange.investment.repo.InvestmentActivityLogRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ActivityService {

    private static final Logger log = LoggerFactory.getLogger(ActivityService.class);
    private final InvestmentActivityLogRepository investmentActivityLogRepository;

    public ActivityService(InvestmentActivityLogRepository investmentActivityLogRepository) {
        this.investmentActivityLogRepository = investmentActivityLogRepository;
    }

    @Transactional
    public void logInvestmentSubscription(InvestmentOrder order,
            InvestmentPosition position,
            BigDecimal grossDebit) {

        // 1️⃣ Persist activity
        InvestmentActivityLog activity = InvestmentActivityLog.builder()
                .orderRef(order.getOrderRef())
                .positionId(order.getPosition().getId())
                .orderRef(order.getOrderRef())
                .activityType(ActivityType.INVESTMENT_SUBSCRIPTION)
                .amount(grossDebit)
                .description("Investment subscription successful for product: "
                        + order.getOrderRef())
                .createdAt(LocalDateTime.now())
                .build();

        investmentActivityLogRepository.save(activity);

        log.info("Investment subscription activity logged for orderRef={}",
                order.getOrderRef());
    }

    public void logInvestmentLiquidation(InvestmentOrder order,
            InvestmentPosition position) {
        
         InvestmentActivityLog activity = InvestmentActivityLog.builder()
                .orderRef(order.getOrderRef())
                .positionId(order.getPosition().getId())
                .orderRef(order.getOrderRef())
                .activityType(ActivityType.INVESTMENT_LIQUIDATION)
                 .investmentPositionStatus(position.getStatus())
                .amount(order.getAmount())
                .description("Investment liquidation successful for product: "
                        + order.getOrderRef())
                .createdAt(LocalDateTime.now())
                .build();

        investmentActivityLogRepository.save(activity);


        // TODO: integrate with your existing Activity/Notification module
        System.out.println("[Activity] Liquidation SUCCESS: " + order.getOrderRef());
    }

    public void logInvestmentTopup(InvestmentOrder order,
            InvestmentPosition position) {
        
         
         InvestmentActivityLog activity = InvestmentActivityLog.builder()
                .orderRef(order.getOrderRef())
                .positionId(order.getPosition().getId())
                .orderRef(order.getOrderRef())
                .activityType(ActivityType.INVESTMENT_TOPUP)
                .amount(order.getAmount())
                .description("Investment topup successful for product: "
                        + order.getOrderRef())
                .createdAt(LocalDateTime.now())
                .build();

        investmentActivityLogRepository.save(activity);

        // TODO: integrate with your existing Activity/Notification module
        System.out.println("[Activity] Liquidation SUCCESS: " + order.getOrderRef());
    }

    public void logInterestCapitalization(
            InvestmentPosition position, BigDecimal accrued, LocalDate today) {

        // TODO: integrate with your existing Activity/Notification module
        System.out.println("[Activity] Liquidation SUCCESS: " + position.getOrderRef());
    }
}
