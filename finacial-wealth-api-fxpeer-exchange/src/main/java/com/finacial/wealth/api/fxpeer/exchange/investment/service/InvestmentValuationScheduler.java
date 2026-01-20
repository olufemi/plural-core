/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.service;

import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentOrder;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentPosition;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentPositionHistory;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentProduct;
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InterestCapitalization;
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.InvestmentPositionStatus;
import com.finacial.wealth.api.fxpeer.exchange.investment.record.CustomerInvestmentsPojo;
import com.finacial.wealth.api.fxpeer.exchange.investment.record.InvestmentPositionHistoryPojo;
import com.finacial.wealth.api.fxpeer.exchange.investment.repo.InvestmentOrderRepository;
import com.finacial.wealth.api.fxpeer.exchange.investment.repo.InvestmentPositionHistoryRepository;
import com.finacial.wealth.api.fxpeer.exchange.investment.repo.InvestmentPositionRepository;
import com.finacial.wealth.api.fxpeer.exchange.model.ApiResponseModel;
import com.finacial.wealth.api.fxpeer.exchange.util.UttilityMethods;
import com.google.gson.Gson;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
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
    private final InvestmentOrderRepository investmentOrderRepository;
    final ZoneId zone = ZoneId.of("Africa/Lagos");
    private final ActivityService activityService;

    @Value("${jobs.one-shot-delay-ms:10}")
    private long delayMsSet;

    public InvestmentValuationScheduler(InvestmentPositionRepository positionRepo,
            InvestmentPositionHistoryRepository historyRepo,
            InvestmentPartnerClient partnerClient, InvestmentOrderService investmentOrderService,
            UttilityMethods utilService,
            InvestmentOrderRepository investmentOrderRepository,
            ActivityService activityService
    ) {
        this.positionRepo = positionRepo;
        this.historyRepo = historyRepo;
        this.partnerClient = partnerClient;
        this.investmentOrderService = investmentOrderService;
        this.utilService = utilService;
        this.investmentOrderRepository = investmentOrderRepository;
        this.activityService = activityService;
    }

    public static final ScheduledExecutorService scheduler
            = Executors.newSingleThreadScheduledExecutor();

    // run every day by 23:55 WAT
    //@Scheduled(cron = "0 55 23 * * *", zone = "Africa/Lagos")
    @jakarta.transaction.Transactional
    @Scheduled(cron = "${fx.investment.run.valuations.snap.shots.cron}", zone = "Africa/Lagos")
    public void snapshotDailyValuations() {

        System.out.println(" ****** Investment Checking and Processing SnapshotDailyValuations >>>>>>>>>>>>> *********** ");

        ZoneId zone = ZoneId.of("Africa/Lagos");
        LocalDate today = LocalDate.now(zone);

        List<InvestmentPosition> positions = positionRepo.findAllActivePositions();

        for (InvestmentPosition pos : positions) {

            // 1) skip if already written today
            if (historyRepo.findByPositionIdAndValuationDate(pos.getId(), today).isPresent()) {
                continue;
            }

            // Defensive null-safety
            BigDecimal invested = nz(pos.getInvestedAmount());
            BigDecimal totalAccrued = nz(pos.getTotalAccruedInterest());

            // 2) Enforce "no interest until next day"
            LocalDate interestStart = pos.getInterestStartDate();
            if (interestStart == null) {
                // fallback: settlement date + 1
                if (pos.getSettlementAt() != null) {
                    interestStart = pos.getSettlementAt().atZone(zone).toLocalDate().plusDays(1);
                } else {
                    interestStart = today.plusDays(1);
                }
                pos.setInterestStartDate(interestStart);
            }

            BigDecimal todaysAccruedInterest = BigDecimal.ZERO;

            if (!today.isBefore(interestStart)) {
                // 3) Accrue today's interest (your % daily logic)
                //BigDecimal pct = nz(pos.getProduct().getPercentageCurrValue()); // e.g 0.001 for 0.1%
                todaysAccruedInterest = computeAccruedInterestForToday(pos, today);

                totalAccrued = totalAccrued.add(todaysAccruedInterest);
            }

            // 4) Update position values
            pos.setAccruedInterest(todaysAccruedInterest);     // today's interest only
            pos.setTotalAccruedInterest(totalAccrued);         // cumulative interest
            pos.setCurrentValue(invested.add(totalAccrued));   // capital + cumulative interest
            pos.setUpdatedAt(Instant.now());
            positionRepo.save(pos);

            // 5) Record daily history snapshot
            InvestmentPositionHistory hist = new InvestmentPositionHistory();
            hist.setPosition(pos);
            hist.setValuationDate(today);
            hist.setPrice(pos.getProduct().getUnitPrice());

            hist.setUnits(nz(pos.getUnits()));
            hist.setSubscriptionAmount(invested);
            hist.setMarketValue(pos.getCurrentValue());
            hist.setGainLoss(pos.getCurrentValue().subtract(invested));
            hist.setDailyInterest(todaysAccruedInterest);
            hist.setTotalInterest(pos.getTotalAccruedInterest());
            hist.setInvestmentAmount(invested);
            hist.setMinimumAmount(pos.getProduct().getMinimumInvestmentAmount());

            // If you want "interest earned today" in history, keep this line:
            hist.setAccruedInterest(todaysAccruedInterest); // <-- add this field if you have it
            // Otherwise store daily interest in investmentAmount ONLY if that's what that column means.
            // Do NOT overwrite it twice.

            hist.setCreatedAt(Instant.now());
            hist.setMaturityDate(pos.getMaturityAt());
            hist.setActiveDate(pos.getSettlementAt());
            hist.setEmailAddress(pos.getEmailAddress());
            hist.setInvestmentId(pos.getOrderRef());
            hist.setProductName(pos.getProductName());

            historyRepo.save(hist);
        }
    }

    private LocalDate getQuarterEnd(LocalDate date) {
        int month = date.getMonthValue();

        if (month <= 3) {
            return LocalDate.of(date.getYear(), 3, 31);
        }
        if (month <= 6) {
            return LocalDate.of(date.getYear(), 6, 30);
        }
        if (month <= 9) {
            return LocalDate.of(date.getYear(), 9, 30);
        }
        return LocalDate.of(date.getYear(), 12, 31);
    }

    @Scheduled(cron = "0 10 0 * * *", zone = "Africa/Lagos")
    @Transactional
    public void runDailyCapitalizationSweep() {

        LocalDate valuationDate
                = LocalDate.now(ZoneId.of("Africa/Lagos")).minusDays(1);

        System.out.println(
                "****** Running capitalization sweep for date: "
                + valuationDate + " ******");

        runCapitalizationSweep(valuationDate);
    }

    @Transactional
    public void runCapitalizationSweep(LocalDate dateToClose) {

        List<InvestmentPosition> positions = positionRepo.findAllActivePositions();

        for (InvestmentPosition pos : positions) {

            InvestmentProduct product = pos.getProduct();
            if (product == null) {
                continue;
            }

            InterestCapitalization cap = product.getInterestCapitalization();
            if (cap == null) {
                cap = InterestCapitalization.DAILY;
            }

            // only capitalize if needed today
            if (!shouldCapitalizeOnDate(cap, dateToClose)) {
                continue;
            }

            // prevent double sweep for same date
            if (pos.getLastCapitalizationDate() != null
                    && !pos.getLastCapitalizationDate().isBefore(dateToClose)) {
                continue;
            }

            BigDecimal accruedTotal = nz(pos.getTotalAccruedInterest());
            if (accruedTotal.compareTo(BigDecimal.ZERO) <= 0) {
                pos.setLastCapitalizationDate(dateToClose);
                continue;
            }

            if (!shouldCapitalizePosition(pos, dateToClose)) {
                continue;
            }

            // ✅ Sweep accrued into capital
            // sweep accrued interest into capital
            BigDecimal accrued = nz(pos.getTotalAccruedInterest());

            if (accrued.compareTo(BigDecimal.ZERO) > 0) {
                pos.setInvestedAmount(pos.getInvestedAmount().add(accrued));
                pos.setTotalAccruedInterest(BigDecimal.ZERO);
                pos.setAccruedInterest(BigDecimal.ZERO);
                pos.setCurrentValue(pos.getInvestedAmount());
            }

            pos.setCurrentValue(pos.getInvestedAmount()); // now equals new capital
            pos.setLastCapitalizationDate(dateToClose);
            pos.setUpdatedAt(Instant.now());
            positionRepo.save(pos);
        }
    }

    private boolean shouldCapitalizeOnDate(InterestCapitalization cap, LocalDate d) {
        return switch (cap) {
            case DAILY ->
                true;
            case WEEKLY ->
                d.getDayOfWeek() == java.time.DayOfWeek.SUNDAY;
            case MONTHLY ->
                d.equals(d.with(java.time.temporal.TemporalAdjusters.lastDayOfMonth()));
            case QUARTERLY ->
                d.equals(getQuarterEnd(d));     // ✅ sacrosanct quarter end
            case BIANNUALY ->
                (d.getMonthValue() == 6 && d.getDayOfMonth() == 30)
                || (d.getMonthValue() == 12 && d.getDayOfMonth() == 31);
        };
    }

    private boolean shouldCapitalizePosition(
            InvestmentPosition pos,
            LocalDate dateToClose
    ) {
        // 1️⃣ must still be active
        if (pos.getStatus() != InvestmentPositionStatus.ACTIVE) {
            return false;
        }

        // 2️⃣ must not be past maturity
        if (pos.getMaturityAt() != null) {
            LocalDate maturityDate
                    = pos.getMaturityAt()
                            .atZone(ZoneId.of("Africa/Lagos"))
                            .toLocalDate();

            if (!dateToClose.isBefore(maturityDate)) {
                return false;
            }
        }

        // 3️⃣ must be a capitalization boundary
        if (!shouldCapitalizeOnDate(
                pos.getProduct().getInterestCapitalization(),
                dateToClose)) {
            return false;
        }

        // 4️⃣ avoid double sweep same day
        LocalDate last = pos.getLastCapitalizationDate();
        return last == null || last.isBefore(dateToClose);
    }

    private BigDecimal computeAccruedInterestForToday(
            InvestmentPosition pos,
            LocalDate valuationDate
    ) {
        BigDecimal capital = nz(pos.getInvestedAmount());
        if (capital.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        InvestmentProduct product = pos.getProduct();
        if (product == null || product.getYieldPa() == null) {
            return BigDecimal.ZERO;
        }

        InterestCapitalization cap
                = product.getInterestCapitalization() != null
                ? product.getInterestCapitalization()
                : InterestCapitalization.DAILY;

        BigDecimal annualRate = product.getYieldPa(); // e.g. 0.12
        BigDecimal divisor = getAccrualDivisor(cap, valuationDate);

        return capital
                .multiply(annualRate)
                .divide(divisor, 18, RoundingMode.HALF_UP)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal getAccrualDivisor(
            InterestCapitalization cap,
            LocalDate valuationDate
    ) {
        return switch (cap) {
            case DAILY -> {
                // Leap year support
                yield valuationDate.isLeapYear()
                ? BigDecimal.valueOf(366)
                : BigDecimal.valueOf(365);
            }
            case WEEKLY ->
                BigDecimal.valueOf(52);
            case MONTHLY ->
                BigDecimal.valueOf(12);
            case QUARTERLY ->
                BigDecimal.valueOf(4);
            case BIANNUALY ->
                BigDecimal.valueOf(2);
        };
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private LocalDate retDate(Instant exp) {
        LocalDate expDate = exp.atZone(zone).toLocalDate();
        return expDate;
    }

    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponseModel> getCustomerInvestments(String auth) {
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

            List<InvestmentOrder> currInvPojo
                    = investmentOrderRepository.findActiveByEmailAddress(email);

            List<CustomerInvestmentsPojo> items = new ArrayList<>();

            long counter = 1L;  // local generated id
            for (InvestmentOrder h : currInvPojo) {
                CustomerInvestmentsPojo pojo = toPojoCustomerInvestment(h, email);
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

        // System.out.println(" getHistory rq ::::::::::::::::  %S  " + new Gson().toJson(responseModel));
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
        dto.setMinimumAmount(h.getMinimumAmount() == null ? h.getPosition().getProduct().getMinimumInvestmentAmount() : h.getMinimumAmount());

        return dto;
    }

    private CustomerInvestmentsPojo toPojoCustomerInvestment(InvestmentOrder h, String email) {
        CustomerInvestmentsPojo dto = new CustomerInvestmentsPojo();

        /* dto.setActiveDate(retDateZoneId(h.getUpdatedAt()));
        dto.setCreatedAt(retDateZoneId(h.getCreatedAt()));
        dto.setCurrency(h.getPosition().getProduct().getCurrency());
        dto.setEmailAddress(email);
        dto.setGainLoss(h.getGainLoss());
        dto.setInvestmentAmount(h.getAmount());
        dto.setMarketValue(h.getMarketValue());
        dto.setMaturityDate(retDateZoneId(h.getMaturityDate()));
        dto.setPrice(h.getPrice());
        dto.setProductId(String.valueOf(h.getPosition().getProduct().getId()));
        dto.setStatus(h.getPosition().getStatus().toString());
        dto.setSubscriptionAmount(h.getAmount());
        dto.setUnits(h.getUnits());
        dto.setValuationDate(h.getValuationDate());
        dto.setInvestmentId(h.getInvestmentId());
        dto.setProductName(h.getProductName());
         */
        return dto;
    }
}
