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
    public void createInitialHistory(InvestmentPosition position) {

        LocalDate today = LocalDate.now();

        // avoid duplicates if called twice
        historyRepo.findByPositionIdAndValuationDate(position.getId(), today)
                .ifPresent(h -> {
                    throw new BusinessException("History already exists for today");
                });

        BigDecimal price = position.getProduct().getUnitPrice() != null
                ? position.getProduct().getUnitPrice()
                : BigDecimal.ONE; // money market typical NAV = 1

        InvestmentPositionHistory hist = new InvestmentPositionHistory();
        hist.setPosition(position);
        hist.setValuationDate(today);
        hist.setPrice(price);
        hist.setUnits(position.getUnits());
        hist.setSubscriptionAmount(position.getInvestedAmount());
        hist.setMarketValue(position.getCurrentValue());
        hist.setGainLoss(position.getCurrentValue().subtract(position.getInvestedAmount()));
        hist.setCreatedAt(Instant.now());

        historyRepo.save(hist);
    }
}
