/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.service;

import com.finacial.wealth.api.fxpeer.exchange.common.BusinessException;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentPosition;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentPositionHistory;
import com.finacial.wealth.api.fxpeer.exchange.investment.repo.InvestmentPositionHistoryRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

/**
 *
 * @author olufemioshin
 */
@Service
public class InvestmentHistoryService {

    private final InvestmentPositionHistoryRepository historyRepo;

    public InvestmentHistoryService(InvestmentPositionHistoryRepository historyRepo) {
        this.historyRepo = historyRepo;
    }

    @Transactional
    public void createInitialHistory(InvestmentPosition position, String email, Instant maturityDate, String orderId) {

        LocalDate today = LocalDate.now();

        // ===== Idempotent behaviour: if today's history already exists, SKIP =====
        var existing = historyRepo.findByPositionIdAndValuationDate(position.getId(), today);
        if (existing.isPresent()) {
            return;  // Do NOT throw or stop settlement
        }

        BigDecimal price = position.getProduct().getUnitPrice() != null
                ? position.getProduct().getUnitPrice()
                : BigDecimal.ONE;

        InvestmentPositionHistory hist = new InvestmentPositionHistory();
        hist.setPosition(position);
        hist.setValuationDate(today);
        hist.setPrice(price);
        hist.setUnits(position.getUnits());
        hist.setSubscriptionAmount(position.getInvestedAmount());
        hist.setMarketValue(position.getCurrentValue());
        hist.setGainLoss(position.getCurrentValue().subtract(position.getInvestedAmount()));
        hist.setCreatedAt(Instant.now());
        hist.setInvestmentAmount(position.getInvestedAmount());
        hist.setActiveDate(Instant.now());
        hist.setMaturityDate(maturityDate);
        hist.setEmailAddress(email);
        hist.setInvestmentId(orderId);
        hist.setProductName(position.getProduct().getName());

        historyRepo.save(hist);
    }

}
