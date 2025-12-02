/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.service;

import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentPosition;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentPositionHistory;
import com.finacial.wealth.api.fxpeer.exchange.investment.record.InvestmentPositionHistoryPojo;
import com.finacial.wealth.api.fxpeer.exchange.investment.repo.InvestmentPositionHistoryRepository;
import com.finacial.wealth.api.fxpeer.exchange.investment.repo.InvestmentPositionRepository;
import com.finacial.wealth.api.fxpeer.exchange.model.ApiResponseModel;
import com.finacial.wealth.api.fxpeer.exchange.util.UttilityMethods;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
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
    private final InvestmentOrderService investmentOrderService;
    private final UttilityMethods utilService;
    final ZoneId zone = ZoneId.of("Africa/Lagos");

    @Value("${jobs.one-shot-delay-ms:10}")
    private long delayMsSet;

    public InvestmentValuationScheduler(InvestmentPositionRepository positionRepo,
            InvestmentPositionHistoryRepository historyRepo,
            InvestmentPartnerClient partnerClient, InvestmentOrderService investmentOrderService,
            UttilityMethods utilService
    ) {
        this.positionRepo = positionRepo;
        this.historyRepo = historyRepo;
        this.partnerClient = partnerClient;
        this.investmentOrderService = investmentOrderService;
        this.utilService = utilService;
    }

    public static final ScheduledExecutorService scheduler
            = Executors.newSingleThreadScheduledExecutor();

    // configurable delay (in milliseconds)
    /*public static void runOnce(Runnable task, long delayMs) {
        scheduler.schedule(() -> {
            try {
                task.run();
            } finally {
                scheduler.shutdown(); // shuts down after running once
            }
        }, delayMs, TimeUnit.MILLISECONDS);
    }

    public void triggerOnce() {
        this.runOnce(this::snapshotDailyValuations, delayMsSet);
    }*/
    // run every day by 23:55 WAT
    //@Scheduled(cron = "0 55 23 * * *", zone = "Africa/Lagos")
    @jakarta.transaction.Transactional
    @Scheduled(cron = "${fx.investment.run.valuations.snap.shots.cron}")
    public void snapshotDailyValuations() {

        System.out.println(" ****** Investment Checking and Processing SnapshotDailyValuations  >>>>>>>>>>>>>   *********** ");

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
            hist.setInvestmentAmount(pos.getAccruedInterest());
            hist.setUnits(pos.getUnits());
            hist.setSubscriptionAmount(pos.getInvestedAmount());
            hist.setMarketValue(getCurrValue);
            hist.setGainLoss(getCurrValue.subtract(pos.getInvestedAmount()));
            hist.setCreatedAt(Instant.now());
            hist.setMaturityDate(pos.getMaturityAt());
            hist.setActiveDate(pos.getSettlementAt());
            hist.setEmailAddress(pos.getEmailAddress());
            hist.setInvestmentAmount(pos.getInvestedAmount());
            hist.setInvestmentId(pos.getOrderRef());
            hist.setProductName(pos.getProductName());

            historyRepo.save(hist);
        }
    }

    private LocalDate retDate(Instant exp) {
        LocalDate expDate = exp.atZone(zone).toLocalDate();
        return expDate;
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponseModel> getHistory(String auth) {
        ApiResponseModel responseModel = new ApiResponseModel();
        int statusCode = 500;
        String statusMessage = "Something went wrong!";

        try {
            statusCode = 400;

            if (auth == null || auth.trim().isEmpty()) {
                responseModel.setStatusCode(statusCode);
                responseModel.setDescription("Missing Authorization header");
                responseModel.setData(Collections.emptyList());
                return ResponseEntity.ok(responseModel);
            }

            final String email = utilService.getClaimFromJwt(auth, "emailAddress");
            if (email == null || email.trim().isEmpty()) {
                responseModel.setStatusCode(statusCode);
                responseModel.setDescription("Invalid token: emailAddress claim missing");
                responseModel.setData(Collections.emptyList());
                return ResponseEntity.ok(responseModel);
            }

            List<InvestmentPositionHistory> histories
                    = historyRepo.findHistoryByEmailAddress(email);

            List<InvestmentPositionHistoryPojo> items = new ArrayList<>();

            long counter = 1L;  // local generated id
            for (InvestmentPositionHistory h : histories) {
                InvestmentPositionHistoryPojo pojo = toPojo(h, email);
                pojo.setId(counter++);   // <-- set non-DB id here
                items.add(pojo);
            }

            responseModel.setData(items);
            responseModel.setStatusCode(200);
            responseModel.setDescription(
                    items.isEmpty()
                    ? "No investment history found for this customer."
                    : "Investment history fetched successfully."
            );

        } catch (Exception ex) {
            ex.printStackTrace();
            responseModel.setStatusCode(statusCode);
            responseModel.setDescription(statusMessage);
            responseModel.setData(Collections.emptyList());
        }

        return ResponseEntity.ok(responseModel);
    }

    private LocalDate retDateZoneId(Instant exp) {
        if (exp == null) {
            return null;   // or return LocalDate.now(), or some default
        }
        return exp.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private InvestmentPositionHistoryPojo toPojo(InvestmentPositionHistory h, String email) {
        InvestmentPositionHistoryPojo dto = new InvestmentPositionHistoryPojo();

        dto.setActiveDate(retDateZoneId(h.getActiveDate()));
        dto.setCreatedAt(retDateZoneId(h.getCreatedAt()));
        dto.setCurrency(h.getPosition().getProduct().getCurrency());
        dto.setEmailAddress(email);
        dto.setGainLoss(h.getGainLoss());
        dto.setInvestmentAmount(h.getInvestmentAmount());
        dto.setMarketValue(h.getMarketValue());
        dto.setMaturityDate(retDateZoneId(h.getMaturityDate()));
        dto.setPrice(h.getPrice());
        dto.setProductId(String.valueOf(h.getPosition().getProduct().getId()));
        dto.setStatus(h.getPosition().getStatus().toString());
        dto.setSubscriptionAmount(h.getSubscriptionAmount());
        dto.setUnits(h.getUnits());
        dto.setValuationDate(h.getValuationDate());
        dto.setInvestmentId(h.getInvestmentId());
        dto.setProductName(h.getProductName());

        return dto;
    }
}
