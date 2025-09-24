/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.services.utils;

/**
 *
 * @author olufemioshin
 */
import com.financial.wealth.api.transactions.enumm.ContributionFrequency;
import com.financial.wealth.api.transactions.enumm.PayoutPolicy;
import java.time.*;
import java.util.*;
import static java.time.temporal.TemporalAdjusters.*;

public final class ScheduleUtils {

    private ScheduleUtils() {
    }

    public static LocalDate normalizeStart(LocalDate start, ContributionFrequency freq,
            Integer dayOfWeek, Integer dayOfMonth) {
        switch (freq) {
            case WEEKLY:
            case BIWEEKLY:
                if (dayOfWeek == null) {
                    throw new IllegalArgumentException("dayOfWeek required for WEEKLY/BIWEEKLY");
                }
                DayOfWeek dow = DayOfWeek.of(dayOfWeek);
                return start.getDayOfWeek() == dow ? start : start.with(nextOrSame(dow));
            case MONTHLY:
            case QUARTERLY:
                if (dayOfMonth == null) {
                    throw new IllegalArgumentException("dayOfMonth required for MONTHLY/QUARTERLY");
                }
                int dom = Math.min(dayOfMonth, 28);
                if (start.getDayOfMonth() <= dom) {
                    return start.withDayOfMonth(dom);
                }
                return start.plusMonths(1).withDayOfMonth(dom);
            default:
                throw new IllegalArgumentException("Unsupported frequency");
        }
    }

    public static List<LocalDate> nextContributionDates(LocalDate startInclusive,
            int cycles,
            ContributionFrequency freq) {
        List<LocalDate> out = new ArrayList<>(cycles);
        LocalDate d = startInclusive;
        for (int i = 0; i < cycles; i++) {
            out.add(d);
            switch (freq) {
                case WEEKLY:
                    d = d.plusWeeks(1);
                    break;
                case BIWEEKLY:
                    d = d.plusWeeks(2);
                    break;
                case MONTHLY:
                    d = d.plusMonths(1);
                    break;
                case QUARTERLY:
                    d = d.plusMonths(3);
                    break;
            }
        }
        return out;
    }

    public static List<LocalDate> payoutDates(List<LocalDate> contributionDates,
            PayoutPolicy policy,
            ContributionFrequency freq) {
        List<LocalDate> out = new ArrayList<>(contributionDates.size());
        switch (policy) {
            case EACH_CYCLE:
                out.addAll(contributionDates);
                return out;
            case PERIOD_END:
                for (LocalDate c : contributionDates) {
                    switch (freq) {
                        case WEEKLY:
                            out.add(c.with(DayOfWeek.SUNDAY)); // or Saturday, choose org policy
                            break;
                        case BIWEEKLY:
                            out.add(c.plusWeeks(2).minusDays(1)); // last day of the 2-week window
                            break;
                        case MONTHLY:
                            out.add(c.with(lastDayOfMonth()));
                            break;
                        case QUARTERLY:
                            Month m = c.getMonth();
                            int qEndMonth = ((m.getValue() - 1) / 3 + 1) * 3; // 3,6,9,12
                            LocalDate qEnd = LocalDate.of(c.getYear(), qEndMonth, 1).with(lastDayOfMonth());
                            out.add(qEnd);
                            break;
                    }
                }
                return out;
            case AFTER_ALL_CONTRIBUTIONS:
                LocalDate last = contributionDates.get(contributionDates.size() - 1);
                out.add(last);
                return out;
            default:
                throw new IllegalArgumentException("Unsupported policy");
        }
    }
}
