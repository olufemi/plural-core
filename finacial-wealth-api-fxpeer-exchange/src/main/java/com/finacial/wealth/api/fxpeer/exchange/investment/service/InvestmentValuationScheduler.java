/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.service;

import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentPosition;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentPositionHistory;
import com.finacial.wealth.api.fxpeer.exchange.investment.repo.InvestmentPositionHistoryRepository;
import com.finacial.wealth.api.fxpeer.exchange.investment.repo.InvestmentPositionRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 *
 * @author olufemioshin
 */
@Service
public class InvestmentValuationScheduler {

    private final InvestmentPositionRepository positionRepo;
    private final InvestmentPositionHistoryRepository historyRepo;
    private final InvestmentPartnerClient partnerClient;

    public InvestmentValuationScheduler(InvestmentPositionRepository positionRepo,
            InvestmentPositionHistoryRepository historyRepo,
            InvestmentPartnerClient partnerClient) {
        this.positionRepo = positionRepo;
        this.historyRepo = historyRepo;
        this.partnerClient = partnerClient;
    }

    // run every day by 23:55 WAT
    @Scheduled(cron = "0 55 23 * * *", zone = "Africa/Lagos")
    @Transactional
    public void snapshotDailyValuations() {

        LocalDate today = LocalDate.now();

        var positions = positionRepo.findAllActivePositions();

        for (InvestmentPosition pos : positions) {

            // skip if already written today
            if (historyRepo.findByPositionIdAndValuationDate(pos.getId(), today).isPresent()) {
                continue;
            }

            // Pull today's NAV/valuation from partner
            /*PartnerValuationResponse val = partnerClient.getValuation(
                    pos.getProduct().getPartnerProductCode(),
                    pos.getUnits(),
                    pos.getEmailAddress()
            );*/
            // update position live values
            BigDecimal getCurrValuePerc = pos.getProduct().getPercentageCurrValue() == null ? BigDecimal.ZERO : pos.getProduct().getPercentageCurrValue();
            // BigDecimal getAccruedInterest = pos.getProduct().getAccruedInterest() == null ? BigDecimal.ZERO : pos.getProduct().getAccruedInterest();
            BigDecimal getCurrValue = pos.getInvestedAmount().add(getCurrValuePerc.multiply(pos.getInvestedAmount()));
            pos.setCurrentValue(getCurrValue);
            pos.setAccruedInterest(getCurrValue.subtract(pos.getInvestedAmount()));
            pos.setUpdatedAt(Instant.now());
            positionRepo.save(pos);

            // record daily history
            InvestmentPositionHistory hist = new InvestmentPositionHistory();
            hist.setPosition(pos);
            hist.setValuationDate(today);
            hist.setPrice(pos.getProduct().getUnitPrice());
            hist.setUnits(pos.getUnits());
            hist.setSubscriptionAmount(pos.getInvestedAmount());
            hist.setMarketValue(getCurrValue);
            hist.setGainLoss(getCurrValue.subtract(pos.getInvestedAmount()));
            hist.setCreatedAt(Instant.now());

            historyRepo.save(hist);
        }
    }
}
