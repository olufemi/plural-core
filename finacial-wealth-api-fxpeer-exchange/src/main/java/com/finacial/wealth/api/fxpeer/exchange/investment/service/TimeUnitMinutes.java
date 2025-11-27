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

    public static Instant computeMaturityAt(InvestmentProduct p, Instant settlementAt) {
        if (p.getScheduleMode() == ScheduleMode.FIXED
                && p.getMaturityAt() != null) {
            return p.getMaturityAt();
        }

        // RELATIVE: minutes base
        Instant rawMaturity = settlementAt.plus(Duration.ofMinutes(p.getTenorMinutes()));

        if (!Boolean.TRUE.equals(p.getMaturityAtEndOfDay())) {
            return rawMaturity; // exact minute maturity
        }

        // If you still want "end of maturity day"
        LocalDate maturityDate = rawMaturity.atZone(ZONE).toLocalDate();
        return maturityDate.atTime(LocalTime.MAX).atZone(ZONE).toInstant();
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
}
