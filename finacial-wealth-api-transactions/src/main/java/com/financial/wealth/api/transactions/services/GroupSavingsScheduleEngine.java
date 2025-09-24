/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.services;

import com.financial.wealth.api.transactions.enumm.ContributionFrequency;
import com.financial.wealth.api.transactions.enumm.PayoutPolicy;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 *
 * @author olufemioshin
 */
@Service
public final class GroupSavingsScheduleEngine {

    private GroupSavingsScheduleEngine() {
    }

    // === Business rules ===
    private static int periodDays(ContributionFrequency f) {
        switch (f) {
            case WEEKLY:
                return 7;
            case BIWEEKLY:
                return 14;   // every two weeks
            case MONTHLY:
                return 28;   // simplified month
            case QUARTERLY:
                return 84;   // simplified quarter
            default:
                throw new IllegalArgumentException("Unsupported frequency: " + f);
        }
    }

    /**
     * Max window for making a contribution within a cycle (your rule).
     */
    private static int maxWindowDays(ContributionFrequency f) {
        switch (f) {
            case WEEKLY:
                return 7;
            case BIWEEKLY:
                return 7;
            case MONTHLY:
                return 28;
            case QUARTERLY:
                return 28;
            default:
                throw new IllegalArgumentException("Unsupported frequency: " + f);
        }
    }

    /**
     * First contribution date from *today* based on period length.
     */
    public static LocalDate nextContributionDate(ContributionFrequency f) {
        return LocalDate.now().plusDays(periodDays(f));
    }

    /**
     * Build a schedule of N cycles from today, including contribution window +
     * payout date.
     */
    public static List<Cycle> buildSchedule(ContributionFrequency f, int cycles, PayoutPolicy policy) {
        List<Cycle> out = new ArrayList<>(cycles);
        int step = periodDays(f);
        int window = maxWindowDays(f);

        // first cycle anchor
        LocalDate contrib = LocalDate.now().plusDays(step);

        for (int i = 1; i <= cycles; i++) {
            LocalDate windowEnd = contrib.plusDays(window - 1);

            LocalDate payout;
            switch (policy) {
                case EACH_CYCLE:
                    // Mark payout on each cycle’s anchor day
                    payout = contrib;
                    break;
                case PERIOD_END:
                    // Mark payout at *end* of the cycle period (same as contrib in this fixed-step model),
                    // or use contrib.plusDays(step - 1) if you want the literal last day of the window.
                    payout = contrib;
                    break;
                case AFTER_ALL_CONTRIBUTIONS:
                    // Defer payout to the very last cycle’s anchor
                    payout = null; // set after the loop
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported payout policy: " + policy);
            }

            out.add(new Cycle(i, contrib, windowEnd, payout));
            contrib = contrib.plusDays(step);
        }

        if (policy == PayoutPolicy.AFTER_ALL_CONTRIBUTIONS) {
            // single payout on the final cycle date
            Cycle last = out.get(out.size() - 1);
            out.set(out.size() - 1, new Cycle(last.cycleNumber, last.contributionDate, last.contributionWindowEnd, last.contributionDate));
        }

        return out;
    }

    /**
     * One schedule row.
     */
    public static final class Cycle {

        public final int cycleNumber;
        public final LocalDate contributionDate;       // due/anchor day for the cycle
        public final LocalDate contributionWindowEnd;  // last acceptable day to pay for that cycle
        public final LocalDate payoutDate;             // when payout is marked (may be null until finalization)

        public Cycle(int cycleNumber, LocalDate contributionDate, LocalDate contributionWindowEnd, LocalDate payoutDate) {
            this.cycleNumber = cycleNumber;
            this.contributionDate = contributionDate;
            this.contributionWindowEnd = contributionWindowEnd;
            this.payoutDate = payoutDate;
        }

        @Override
        public String toString() {
            return "Cycle{"
                    + "cycleNumber=" + cycleNumber
                    + ", contributionDate=" + contributionDate
                    + ", contributionWindowEnd=" + contributionWindowEnd
                    + ", payoutDate=" + payoutDate
                    + '}';
        }
    }
}
