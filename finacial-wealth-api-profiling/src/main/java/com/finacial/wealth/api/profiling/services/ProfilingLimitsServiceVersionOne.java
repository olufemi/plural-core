/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.services;

import com.finacial.wealth.api.profiling.domain.GlobalLimitConfig;
import com.finacial.wealth.api.profiling.domain.UserLimitConfig;
import com.finacial.wealth.api.profiling.models.LedgerSummaryResponse;
import com.finacial.wealth.api.profiling.models.ProfilingLimitsResponse;
import com.finacial.wealth.api.profiling.proxies.LedgerSummaryClient;
import com.finacial.wealth.api.profiling.repo.DeviceChangeLimitConfigRepo;
import com.finacial.wealth.api.profiling.repo.GlobalLimitConfigRepo;
import com.finacial.wealth.api.profiling.repo.UserLimitConfigRepo;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author olufemioshin
 */
@Service
public class ProfilingLimitsServiceVersionOne {

    @Autowired
    private LedgerSummaryClient ledgerClient;

    @Autowired
    private UserLimitConfigRepo userLimitRepo;

    @Autowired
    private GlobalLimitConfigRepo globalLimitRepo;

    @Autowired(required = false)
    private DeviceChangeLimitConfigRepo deviceLimitRepo;

    public ProfilingLimitsResponse getLimits(String accountNumber, String productCode, String currency) {
        List<UserLimitConfig> user = userLimitRepo.findByWalletNumber(accountNumber);
        GlobalLimitConfig global = globalLimitRepo.findByCategory(user.get(0).getTierCategory());

        ProfilingLimitsResponse resp = new ProfilingLimitsResponse();
        resp.setAccountNumber(accountNumber);
        resp.setCurrency(currency);
        resp.setTier(user.get(0).getTierCategory());

        ProfilingLimitsResponse.Limits limits = new ProfilingLimitsResponse.Limits();
        limits.setSend(buildSendLimits(accountNumber, productCode, global));
        limits.setReceive(buildReceiveLimits(accountNumber, productCode, global));

        resp.setLimits(limits);
        return resp;
    }

    private ProfilingLimitsResponse.Direction buildSendLimits(String acct, String product, GlobalLimitConfig g) {
        ProfilingLimitsResponse.Direction d = new ProfilingLimitsResponse.Direction();

        d.setSingle(buildLimit("Send Limit (Single transaction)", g.getWithdrawalSingleTransaction(), BigDecimal.ZERO));
        d.setDaily(buildPeriodLimit("Daily Limit", acct, product, "DAILY", g.getWithdrawal()));
        d.setWeekly(buildPeriodLimit("Weekly Limit", acct, product, "WEEKLY", g.getWithdrawal()));
        d.setMonthly(buildPeriodLimit("Monthly Limit", acct, product, "MONTHLY", g.getWithdrawal()));

        return d;
    }

    private ProfilingLimitsResponse.Direction buildReceiveLimits(String acct, String product, GlobalLimitConfig g) {
        ProfilingLimitsResponse.Direction d = new ProfilingLimitsResponse.Direction();

        d.setSingle(unlimited("Deposit (Per transaction) Limit"));
        d.setDaily(buildPeriodLimit("Daily Deposit Limit", acct, product, "DAILY", g.getWalletDeposit()));
        d.setWeekly(buildPeriodLimit("Weekly Deposit Limit", acct, product, "WEEKLY", g.getWalletDeposit()));
        d.setMonthly(buildPeriodLimit("Monthly Deposit Limit", acct, product, "MONTHLY", g.getWalletDeposit()));

        return d;
    }

    private ProfilingLimitsResponse.Limit buildPeriodLimit(
            String label,
            String acct,
            String product,
            String period,
            String totalStr) {

        LedgerSummaryResponse s = ledgerClient.summary(acct, product, period);
        BigDecimal spent = s.getSummary().getDebit().getAmount();
        return buildLimit(label, totalStr, spent);
    }

    private ProfilingLimitsResponse.Limit buildLimit(String label, String totalStr, BigDecimal spent) {
        ProfilingLimitsResponse.Limit l = new ProfilingLimitsResponse.Limit();
        l.setLabel(label);

        if (totalStr == null || new BigDecimal(totalStr).compareTo(BigDecimal.ZERO) <= 0) {
            l.setUnlimited(true);
            return l;
        }

        BigDecimal total = new BigDecimal(totalStr);
        BigDecimal left = total.subtract(spent);

        l.setTotal(total.toPlainString());
        l.setSpent(spent.toPlainString());
        l.setLeft(left.max(BigDecimal.ZERO).toPlainString());
        l.setUnlimited(false);
        return l;
    }

    private ProfilingLimitsResponse.Limit unlimited(String label) {
        ProfilingLimitsResponse.Limit l = new ProfilingLimitsResponse.Limit();
        l.setLabel(label);
        l.setUnlimited(true);
        return l;
    }

}
