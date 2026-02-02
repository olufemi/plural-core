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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author olufemioshin
 */
@Service
public class ProfilingLimitsService {

    private final LedgerSummaryClient ledgerClient;

    private final WalletSystemProxyService walletSystemProxyService;

    private final UserLimitConfigRepo userLimitRepo;
    private final GlobalLimitConfigRepo globalLimitRepo;
    private final DeviceChangeLimitConfigRepo deviceChangeRepo;
    private final RegWalletInfoRepository regWalletInfoRepo;
    private final ProcessorUserFailedTransInfoRepo procFailedRepo;
    private final UttilityMethods utilMeth;
    // private BigDecimal currencyRate = BigDecimal.ONE;

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

    static class CurrencyCtx {

        final String currency;
        final BigDecimal rate;

        CurrencyCtx(String currency, BigDecimal rate) {
            this.currency = currency;
            this.rate = rate;
        }
    }

    // Cache note:
    // Put @Cacheable here if you have Spring Cache + Redis in profiling.
    // e.g. @Cacheable(value="limits-ui", key="#rq.accountNumber")
    @Transactional(readOnly = true)
    public LimitsUiResponse getLimitsUi(
            LimitsUiRequest rq,
            String auth, String channel) throws UnsupportedEncodingException {

        final int failCode = 400;
        LimitsUiResponse out = new LimitsUiResponse();

        // ---- basics
        DecodedJWTToken decoded = DecodedJWTToken.getDecoded(auth);
        String accountNumber = rq.getAccountNumber();

        // Default currency + rate
        String currency = utilMeth.returnSETTING_ONBOARDING_DEFAULT_CURRENCY_CODE();
        BigDecimal currencyRate = BigDecimal.ONE;

        String walletId = null;

        // ===================== 1) RESOLVE WALLET + CURRENCY ==========================
        List<RegWalletInfo> list = regWalletInfoRepo.findByPhoneNumberData(accountNumber);

        if (list == null || list.isEmpty()) {
            // fallback: get account by email (legacy/secondary path)
            List<AddAccountDetails> getOthers = accountProfileRepo.findByEmailAddress(decoded.emailAddress);
            if (getOthers == null || getOthers.isEmpty()) {
                recordFailedTrans(channel);
                return error(out, failCode, "Get Limits, Customer is invalid!");
            }

            AddAccountDetails acct = getOthers.get(0);

            if (acct.getAccountNumber() == null || !accountNumber.equals(acct.getAccountNumber())) {
                recordFailedTrans(channel);
                return error(out, failCode, "Get Limits, Account number is invalid!");
            }

            walletId = acct.getWalletId();

            if (acct.getCurrencyCode() != null && !acct.getCurrencyCode().trim().isEmpty()) {
                currency = acct.getCurrencyCode().trim();
            }

        } else {
            // main customer path
            RegWalletInfo w = list.get(0);
            walletId = w.getWalletId();

            // IMPORTANT:
            // If RegWalletInfo has currencyCode, use it here. If not, try to fetch currency from profile by walletId.
            // Uncomment whichever exists in your project.
            // Example A: if RegWalletInfo has currencyCode
            // if (w.getCurrencyCode() != null && !w.getCurrencyCode().trim().isEmpty()) {
            //     currency = w.getCurrencyCode().trim();
            // }
            // Example B: fetch from account profile by walletId (recommended if you have it)
            AddAccountDetails acct = accountProfileRepo.findFirstByWalletId(walletId);
            if (acct != null && acct.getCurrencyCode() != null && !acct.getCurrencyCode().trim().isEmpty()) {
                currency = acct.getCurrencyCode().trim();
            }
        }

        // currency -> rate
        if ("NGN".equalsIgnoreCase(currency)) {
            currencyRate = new BigDecimal(utilMeth.returnSETTING_ONBOARDING_NGN_CURRENCY_CODE_RATE());
        } else {
            currencyRate = BigDecimal.ONE;
        }

        // Set UI basics
        out.setAccountNumber(accountNumber);
        out.setCurrency(currency);

        if (walletId == null || walletId.trim().isEmpty()) {
            return error(out, failCode, "Get Limits, WalletId not found!");
        }

        // ===================== 2) RESOLVE TIER + GLOBAL LIMITS ==========================
        UserLimitConfig u = userLimitRepo.findByWalletNumberQuery(walletId);
        if (u == null || isBlank(u.getTierCategory())) {
            return error(out, failCode, "Tier category not set for account");
        }

        String tier = u.getTierCategory();
        GlobalLimitConfig g = globalLimitRepo.findByCategory(tier);
        if (g == null) {
            return error(out, failCode, "Global limits not found for tier: " + tier);
        }

        // ===================== 3) BUILD PERIODS ==========================
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate monthStart = today.withDayOfMonth(1);

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

        LedgerSummaryRequest lsq = LedgerSummaryRequest.of(accountNumber, "", periods);

        // ===================== 4) CALL LEDGER SUMMARY ==========================
        LedgerSummaryResponse lsr = walletSystemProxyService.getLedgerSummaryCaller(lsq);

        if (lsr == null) {
            return error(out, failCode, "Ledger service returned empty response");
        }

        // NOTE: your ledger sometimes returns 0 for success. Accept both.
        if (!(lsr.getStatusCode() == 0 || lsr.getStatusCode() == 200)) {
            out.setStatusCode(lsr.getStatusCode());
            out.setDescription(lsr.getDescription() != null ? lsr.getDescription() : "Unable to fetch ledger summary");
            return out;
        }

        // ===================== 5) MAP PERIOD SUMMARIES ==========================
        java.util.Map<String, LedgerPeriodSummary> byCode = new java.util.HashMap<String, LedgerPeriodSummary>();
        if (lsr.getPeriods() != null) {
            for (LedgerPeriodSummary p : lsr.getPeriods()) {
                if (p != null && p.getCode() != null) {
                    byCode.put(p.getCode(), p);
                }
            }
        }

        // ===================== 6) BUILD UI LIMITS ==========================
        out.setSendSingleTransactionLimit(parseLimitOrNull(g.getWithdrawalSingleTransaction(), currencyRate));
        out.setSendPeriodLimits(buildSpendLines(byCode, g, true, currencyRate));

        out.setReceiveSingleTransactionLimit(parseLimitOrNull(g.getWalletSingleDeposit(), currencyRate));
        out.setReceivePeriodLimits(buildReceiveLines(byCode, g, currencyRate));

        out.setStatusCode(200);
        out.setDescription("OK");
        return out;
    }

    // SEND side uses DEBIT spent amounts
    private List<UiLimitLine> buildSpendLines(java.util.Map<String, LedgerPeriodSummary> byCode, GlobalLimitConfig g, boolean includeMonthly, BigDecimal currencyRate) {

        List<UiLimitLine> lines = new ArrayList<UiLimitLine>();
        lines.add(line("Daily Limit", parseLimitOrNull(g.getDailyLimit(), currencyRate), debitSpent(byCode, "DAILY")));
        lines.add(line("Weekly Limit", parseLimitOrNull(g.getWeeklyLimit() == null ? "0.00" : g.getWeeklyLimit(), currencyRate), debitSpent(byCode, "SINGLE")));
        lines.add(line("Monthly Limit", parseLimitOrNull(g.getMonthlyLimit() == null ? "0.00" : g.getMonthlyLimit(), currencyRate), debitSpent(byCode, "MONTHLY")));

        return lines;
    }

    // RECEIVE side uses CREDIT amounts. Your screenshots show deposit per-tx limit can be unlimited.
    private List<UiLimitLine> buildReceiveLines(java.util.Map<String, LedgerPeriodSummary> byCode, GlobalLimitConfig g, BigDecimal currencyRate
    ) {

        List<UiLimitLine> lines = new ArrayList<UiLimitLine>();

        // Per screenshots: daily/weekly/monthly receive limits exist.
        // If you store them in same dailyLimit/monthlyLimit, reuse; otherwise add new columns.
        lines.add(line("Daily Limit", parseLimitOrNull(g.getDailyLimit(), currencyRate), creditReceived(byCode, "DAILY")));
        lines.add(line("Weekly Limit", parseLimitOrNull(g.getWeeklyLimit() == null ? "0.00" : g.getWeeklyLimit(), currencyRate), creditReceived(byCode, "WEEKLY")));
        lines.add(line("Monthly Limit", parseLimitOrNull(g.getMonthlyLimit() == null ? "0.00" : g.getMonthlyLimit(), currencyRate), creditReceived(byCode, "MONTHLY")));

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
    private BigDecimal parseLimitOrNull(String raw, BigDecimal currencyRate) {
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
        return new BigDecimal(s).multiply(currencyRate);
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().length() == 0;
    }

}
