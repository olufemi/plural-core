/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.limits;

import com.finacial.wealth.api.profiling.domain.AddAccountDetails;
import com.finacial.wealth.api.profiling.domain.GlobalLimitConfig;
import com.finacial.wealth.api.profiling.domain.ProcessorUserFailedTransInfo;
import com.finacial.wealth.api.profiling.domain.RegWalletInfo;
import com.finacial.wealth.api.profiling.domain.UserLimitConfig;
import com.finacial.wealth.api.profiling.models.ApiResponseModel;
import com.finacial.wealth.api.profiling.proxies.LedgerSummaryClient;
import com.finacial.wealth.api.profiling.repo.AddAccountDetailsRepo;
import com.finacial.wealth.api.profiling.repo.DeviceChangeLimitConfigRepo;
import com.finacial.wealth.api.profiling.repo.GlobalLimitConfigRepo;
import com.finacial.wealth.api.profiling.repo.ProcessorUserFailedTransInfoRepo;
import com.finacial.wealth.api.profiling.repo.RegWalletInfoRepository;
import com.finacial.wealth.api.profiling.repo.UserLimitConfigRepo;
import com.finacial.wealth.api.profiling.services.WalletSystemProxyService;
import com.finacial.wealth.api.profiling.utils.DecodedJWTToken;
import com.finacial.wealth.api.profiling.utils.GlobalMethods;
import com.finacial.wealth.api.profiling.utils.UttilityMethods;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author olufemioshin
 */
public class ProfilingLimitsService {

    private final LedgerSummaryClient ledgerClient;

    private final WalletSystemProxyService walletSystemProxyService;

    private final UserLimitConfigRepo userLimitRepo;
    private final GlobalLimitConfigRepo globalLimitRepo;
    private final DeviceChangeLimitConfigRepo deviceChangeRepo;
    private final RegWalletInfoRepository regWalletInfoRepo;
    private final ProcessorUserFailedTransInfoRepo procFailedRepo;
    private final UttilityMethods utilMeth;

    private final AddAccountDetailsRepo accountProfileRepo; // <-- your mapping (accountNumber -> productCode + currency)

    public ProfilingLimitsService(
            LedgerSummaryClient ledgerClient,
            UserLimitConfigRepo userLimitRepo,
            GlobalLimitConfigRepo globalLimitRepo,
            DeviceChangeLimitConfigRepo deviceChangeRepo,
            AddAccountDetailsRepo accountProfileRepo,
            RegWalletInfoRepository regWalletInfoRepo,
            ProcessorUserFailedTransInfoRepo procFailedRepo,
            WalletSystemProxyService walletSystemProxyService,
            UttilityMethods utilMeth
    ) {
        this.ledgerClient = ledgerClient;
        this.userLimitRepo = userLimitRepo;
        this.globalLimitRepo = globalLimitRepo;
        this.deviceChangeRepo = deviceChangeRepo;
        this.accountProfileRepo = accountProfileRepo;
        this.regWalletInfoRepo = regWalletInfoRepo;
        this.walletSystemProxyService = walletSystemProxyService;
        this.procFailedRepo = procFailedRepo;
        this.utilMeth = utilMeth;
    }

    private LimitsUiResponse error(LimitsUiResponse resp, int statusCode, String description) {
        resp.setStatusCode(statusCode);
        resp.setDescription(description);

        return resp;
    }

    private void recordFailedTrans(String channel) {

        ProcessorUserFailedTransInfo failed = new ProcessorUserFailedTransInfo(
                "resend-otp",
                "Wallet to Wallet transfer, Customer is invalid!",
                String.valueOf(GlobalMethods.generateTransactionId()),
                "",
                channel,
                "Profiling-Service"
        );

        procFailedRepo.save(failed);
    }

    // Cache note:
    // Put @Cacheable here if you have Spring Cache + Redis in profiling.
    // e.g. @Cacheable(value="limits-ui", key="#rq.accountNumber")
    @Transactional(readOnly = true)
    public LimitsUiResponse getLimitsUi(
            LimitsUiRequest rq,
            String auth, String channel) throws UnsupportedEncodingException {
        LimitsUiResponse response = new LimitsUiResponse();
        int failCode = 400;
        DecodedJWTToken decoded = DecodedJWTToken.getDecoded(auth);
        String currency = utilMeth.returnSETTING_ONBOARDING_DEFAULT_CURRENCY_CODE();
        String accountNumber = rq.getAccountNumber();

        // ===================== 1) MAIN CUSTOMER ==========================
        List<RegWalletInfo> list = regWalletInfoRepo.findByPhoneNumberData(accountNumber);
        if (list == null || list.isEmpty()) {

            List<AddAccountDetails> getOthers = accountProfileRepo.findByEmailAddress(decoded.emailAddress);
            if (getOthers == null || getOthers.isEmpty()) {
                recordFailedTrans(channel);
                return error(response, failCode, "Get Limits, Customer is invalid!");
            }
            if (!accountNumber.equals(getOthers.get(0).getAccountNumber())) {
                recordFailedTrans(channel);
                return error(response, failCode, "Get Limits, Account number is invalid!");
            }
            currency = getOthers.get(0).getCurrencyCode();

        }

        LimitsUiResponse out = new LimitsUiResponse();

        // 1) resolve mapping (profiling knows currency + productCode)
        //String productCode = acct.getProductCode();
        out.setAccountNumber(accountNumber);
        out.setCurrency(currency);

        // 2) resolve tier
        // If device-change overrides tier, you can apply here.
        UserLimitConfig u = userLimitRepo.findByWalletNumberQuery(accountNumber);
        if (u == null || isBlank(u.getTierCategory())) {
            out.setStatusCode(400);
            out.setDescription("Tier category not set for account");
            return out;
        }

        String tier = u.getTierCategory();
        GlobalLimitConfig g = globalLimitRepo.findByCategory(tier);
        if (g == null) {
            out.setStatusCode(400);
            out.setDescription("Global limits not found for tier: " + tier);
            return out;
        }

        // 3) build periods (calendar). You can switch to rolling later.
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate monthStart = today.withDayOfMonth(1);
        java.time.LocalDate yearStart = today.withDayOfYear(1);

        java.time.DayOfWeek dow = today.getDayOfWeek();
        int shift = dow.getValue() - java.time.DayOfWeek.MONDAY.getValue();
        if (shift < 0) {
            shift = 0;
        }
        java.time.LocalDate weekStart = today.minusDays(shift);

        List<LedgerPeriodQuery> periods = new ArrayList<LedgerPeriodQuery>();
        periods.add(LedgerPeriodQuery.of("DAILY", today, today));
        periods.add(LedgerPeriodQuery.of("WEEKLY", weekStart, today));
        periods.add(LedgerPeriodQuery.of("MONTHLY", monthStart, today));
        periods.add(LedgerPeriodQuery.of("YEARLY", yearStart, today));

        LedgerSummaryRequest lsq = LedgerSummaryRequest.of(accountNumber, "", periods);

        // 4) call ledger summary
        LedgerSummaryResponse lsr = walletSystemProxyService.getLedgerSummaryCaller(lsq);
        // LedgerSummaryResponse lsr = ledgerClient.summary(auth, channel, lsq);
        if (lsr == null || lsr.getStatusCode() != 200) {
            out.setStatusCode(502);
            out.setDescription("Unable to fetch ledger summary");
            return out;
        }

        // Map summaries by code
        java.util.Map<String, LedgerPeriodSummary> byCode = new java.util.HashMap<String, LedgerPeriodSummary>();
        if (lsr.getPeriods() != null) {
            for (LedgerPeriodSummary p : lsr.getPeriods()) {
                if (p != null && p.getCode() != null) {
                    byCode.put(p.getCode(), p);
                }
            }
        }

        // 5) Build SEND (debit) UI lines
        out.setSendSingleTransactionLimit(parseLimitOrNull(g.getWithdrawalSingleTransaction()));
        out.setSendPeriodLimits(buildSpendLines(byCode, g, true));

        // 6) Build RECEIVE (credit) UI lines
        // Some products want receive unlimited; you can store null/unlimited in config.
        // If you have a credit single limit field, plug it in; else treat as unlimited.
        out.setReceiveSingleTransactionLimit(parseLimitOrNull(g.getWalletSingleDeposit()));
        out.setReceivePeriodLimits(buildReceiveLines(byCode, g));

        out.setStatusCode(200);
        out.setDescription("OK");
        return out;
    }

    // SEND side uses DEBIT spent amounts
    private List<UiLimitLine> buildSpendLines(java.util.Map<String, LedgerPeriodSummary> byCode, GlobalLimitConfig g, boolean includeMonthly) {

        List<UiLimitLine> lines = new ArrayList<UiLimitLine>();

        lines.add(line("Daily Limit", parseLimitOrNull(g.getDailyLimit()), debitSpent(byCode, "DAILY")));
        lines.add(line("Weekly Limit", parseLimitOrNull(g.getWeeklyLimit() == null ? "0.00" : g.getWeeklyLimit()), debitSpent(byCode, "WEEKLY")));
        lines.add(line("Monthly Limit", parseLimitOrNull(g.getMonthlyLimit() == null ? "0.00" : g.getMonthlyLimit()), debitSpent(byCode, "MONTHLY")));

        return lines;
    }

    // RECEIVE side uses CREDIT amounts. Your screenshots show deposit per-tx limit can be unlimited.
    private List<UiLimitLine> buildReceiveLines(java.util.Map<String, LedgerPeriodSummary> byCode, GlobalLimitConfig g) {

        List<UiLimitLine> lines = new ArrayList<UiLimitLine>();

        // Per screenshots: daily/weekly/monthly receive limits exist.
        // If you store them in same dailyLimit/monthlyLimit, reuse; otherwise add new columns.
        lines.add(line("Daily Limit", parseLimitOrNull(g.getDailyLimit()), creditReceived(byCode, "DAILY")));
        lines.add(line("Weekly Limit", parseLimitOrNull(g.getWeeklyLimit() == null ? "0.00" : g.getWeeklyLimit()), creditReceived(byCode, "WEEKLY")));
        lines.add(line("Monthly Limit", parseLimitOrNull(g.getMonthlyLimit() == null ? "0.00" : g.getMonthlyLimit()), creditReceived(byCode, "MONTHLY")));

        return lines;
    }

    private UiLimitLine line(String label, BigDecimal limit, BigDecimal spent) {
        UiLimitLine l = new UiLimitLine();
        l.setLabel(label);
        l.setSpent(nz(spent));

        if (limit == null) {
            l.setUnlimited(true);
            l.setLimit(null);
            l.setLeft(null);
        } else {
            l.setUnlimited(false);
            l.setLimit(limit);
            BigDecimal left = limit.subtract(nz(spent));
            if (left.compareTo(BigDecimal.ZERO) < 0) {
                left = BigDecimal.ZERO;
            }
            l.setLeft(left);
        }
        return l;
    }

    private BigDecimal debitSpent(java.util.Map<String, LedgerPeriodSummary> byCode, String code) {
        LedgerPeriodSummary p = byCode.get(code);
        if (p == null) {
            return BigDecimal.ZERO;
        }
        return nz(p.getDebitAmount());
    }

    private BigDecimal creditReceived(java.util.Map<String, LedgerPeriodSummary> byCode, String code) {
        LedgerPeriodSummary p = byCode.get(code);
        if (p == null) {
            return BigDecimal.ZERO;
        }
        return nz(p.getCreditAmount());
    }

    // Unlimited representation:
    // - store as NULL in DB OR "UNLIMITED" string.
    // This parser treats blank/"UNLIMITED"/"-1" as unlimited.
    private BigDecimal parseLimitOrNull(String raw) {
        if (raw == null) {
            return null;
        }
        String s = raw.trim();
        if (s.length() == 0) {
            return null;
        }
        if ("UNLIMITED".equalsIgnoreCase(s)) {
            return null;
        }
        if ("-1".equals(s)) {
            return null;
        }
        return new BigDecimal(s);
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().length() == 0;
    }

}
