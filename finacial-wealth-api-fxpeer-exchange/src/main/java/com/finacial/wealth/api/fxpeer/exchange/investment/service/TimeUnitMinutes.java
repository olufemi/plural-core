/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.investment.service;

import com.finacial.wealth.api.fxpeer.exchange.common.NotFoundException;
import com.finacial.wealth.api.fxpeer.exchange.investment.domain.InvestmentProduct;
import com.finacial.wealth.api.fxpeer.exchange.investment.ennum.ScheduleMode;
import com.finacial.wealth.api.fxpeer.exchange.investment.record.UpdateProductScheduleRq;
import com.finacial.wealth.api.fxpeer.exchange.investment.repo.InvestmentProductRepository;
import jakarta.transaction.Transactional;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 *
 * @author olufemioshin
 */
public final class TimeUnitMinutes {

    private final InvestmentProductRepository productRepo;

    public TimeUnitMinutes(InvestmentProductRepository productRepo) {
        this.productRepo = productRepo;
    }

    public enum Unit {
        MINUTES(1),
        HOURS(60),
        DAYS(24 * 60),
        WEEKS(7 * 24 * 60);

        private final long toMinutes;

        Unit(long toMinutes) {
            this.toMinutes = toMinutes;
        }

        public long toMinutes(long value) {
            return value * toMinutes;
        }
    }

    public static long toMinutes(long value, Unit unit) {
        if (value < 0) {
            throw new IllegalArgumentException("value must be >= 0");
        }
        return unit.toMinutes(value);
    }

    private static final ZoneId ZONE = ZoneId.of("Africa/Lagos");

    public static Instant computeSettlementAt(InvestmentProduct p, Instant subscribedAt) {
        if (p.getScheduleMode() == ScheduleMode.FIXED
                && p.getSettlementAt() != null) {
            return p.getSettlementAt();
        }
        // RELATIVE: minutes base
        return subscribedAt.plus(Duration.ofMinutes(p.getSettlementDelayMinutes()));
    }

    public static Instant computeMaturityAt(
            InvestmentProduct p,
            Instant settlementAt
    ) {
        ZoneId zone = ZONE; // Africa/Lagos

        // 1️⃣ FIXED maturity (absolute date)
        if (p.getScheduleMode() == ScheduleMode.FIXED
                && p.getMaturityAt() != null) {
            return p.getMaturityAt();
        }

        // 2️⃣ RELATIVE maturity (legacy / testing)
        if (p.getScheduleMode() == ScheduleMode.RELATIVE
                && p.getTenorMinutes() != null) {

            Instant raw = settlementAt.plus(Duration.ofMinutes(p.getTenorMinutes()));

            if (!Boolean.TRUE.equals(p.getMaturityAtEndOfDay())) {
                return raw;
            }

            LocalDate d = raw.atZone(zone).toLocalDate();
            return d.atTime(LocalTime.MAX).atZone(zone).toInstant();
        }

        // 3️⃣ CAPITALIZATION-based maturity (✅ new correct logic)
        if (p.getScheduleMode() == ScheduleMode.CAPITALIZATION) {

            LocalDate settleDate
                    = settlementAt.atZone(zone).toLocalDate();

            LocalDate maturityDate = switch (p.getInterestCapitalization()) {

                case DAILY ->
                    settleDate;

                case WEEKLY ->
                    settleDate.with(
                    java.time.temporal.TemporalAdjusters
                    .nextOrSame(DayOfWeek.SUNDAY)
                    );

                case MONTHLY ->
                    settleDate.with(
                    java.time.temporal.TemporalAdjusters
                    .lastDayOfMonth()
                    );

                case QUARTERLY ->
                    getQuarterEnd(settleDate);

                case BIANNUALY ->
                    getBiAnnualEnd(settleDate);
            };

            return maturityDate
                    .atTime(LocalTime.MAX)
                    .atZone(zone)
                    .toInstant();
        }

        throw new IllegalStateException(
                "Unable to compute maturity for product " + p.getProductCode());
    }

    @Transactional
    public InvestmentProduct updateRelativeSchedule(Long productId, UpdateProductScheduleRq rq) {
        InvestmentProduct p = productRepo.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (rq.getSettlementDelayValue() != null && rq.getSettlementDelayUnit() != null) {
            long mins = TimeUnitMinutes.toMinutes(rq.getSettlementDelayValue(), rq.getSettlementDelayUnit());
            p.setSettlementDelayMinutes(mins);
        }

        if (rq.getTenorValue() != null && rq.getTenorUnit() != null) {
            long mins = TimeUnitMinutes.toMinutes(rq.getTenorValue(), rq.getTenorUnit());
            p.setTenorMinutes(mins);
        }

        return productRepo.save(p);
    }

    private static LocalDate getQuarterEnd(LocalDate d) {
        int y = d.getYear();
        int m = d.getMonthValue();

        if (m <= 3) {
            return LocalDate.of(y, 3, 31);
        }
        if (m <= 6) {
            return LocalDate.of(y, 6, 30);
        }
        if (m <= 9) {
            return LocalDate.of(y, 9, 30);
        }
        return LocalDate.of(y, 12, 31);
    }

    private static LocalDate getBiAnnualEnd(LocalDate d) {
        int y = d.getYear();
        int m = d.getMonthValue();

        if (m <= 6) {
            return LocalDate.of(y, 6, 30);
        }
        return LocalDate.of(y, 12, 31);
    }
}
