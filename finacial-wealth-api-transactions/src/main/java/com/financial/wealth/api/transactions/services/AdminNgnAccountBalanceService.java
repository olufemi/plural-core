package com.financial.wealth.api.transactions.services;

import com.financial.wealth.api.transactions.breezepay.payout.AddAccountDetails;
import com.financial.wealth.api.transactions.models.BaseResponse;
import com.financial.wealth.api.transactions.repo.AddAccountDetailsRepo;
import com.financial.wealth.api.transactions.utils.UttilityMethods;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AdminNgnAccountBalanceService {

    private final AddAccountDetailsRepo addAccountDetailsRepo;
    private final UttilityMethods uttilityMethods;

    public AdminNgnAccountBalanceService(AddAccountDetailsRepo addAccountDetailsRepo, UttilityMethods uttilityMethods) {
        this.addAccountDetailsRepo = addAccountDetailsRepo;
        this.uttilityMethods = uttilityMethods;
    }

    public Map<String, Object> getCumulativeBalances(String productCode, String keyword, List<String> accountNumbers,
            String channel, String walletSystemAuthorization) {
        List<AddAccountDetails> accounts = resolveAccounts(keyword, accountNumbers);
        List<String> resolvedAccountNumbers = uniqueAccountNumbers(accounts, accountNumbers);

        BaseResponse coreResponse = uttilityMethods.getCumulativeBalances(productCode, "NGN", resolvedAccountNumbers,
                StringUtils.hasText(channel) ? channel : "API", walletSystemAuthorization);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("currency", "NGN");
        data.put("accountCount", resolvedAccountNumbers.size());
        data.put("accounts", accountRows(accounts));
        data.put("accountNumbers", resolvedAccountNumbers);
        data.put("coreBankingResponse", coreResponse);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("statusCode", coreResponse == null ? 502 : coreResponse.getStatusCode());
        response.put("description", coreResponse == null ? "Core banking cumulative balance response is empty" : coreResponse.getDescription());
        response.put("data", data);
        return response;
    }

    public Map<String, Object> getCumulativeBalanceSummary(String productCode, String keyword,
            List<String> accountNumbers, String channel, String walletSystemAuthorization) {
        List<AddAccountDetails> accounts = resolveAccounts(keyword, accountNumbers);
        List<String> resolvedAccountNumbers = uniqueAccountNumbers(accounts, accountNumbers);

        BaseResponse coreResponse = uttilityMethods.getCumulativeBalances(productCode, "NGN", resolvedAccountNumbers,
                StringUtils.hasText(channel) ? channel : "API", walletSystemAuthorization);

        Map<String, Object> coreData = coreResponse == null || coreResponse.getData() == null
                ? new LinkedHashMap<>()
                : coreResponse.getData();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("currency", "NGN");
        data.put("accountCount", resolvedAccountNumbers.size());
        data.put("totalBalance", firstAmount(coreData, "totalBalance", "cumulativeBalance", "balance"));
        data.put("totalAvailableBalance", firstAmount(coreData, "totalAvailableBalance", "availableBalance"));
        data.put("totalLedgerBalance", firstAmount(coreData, "totalLedgerBalance", "ledgerBalance"));
        data.put("coreStatusCode", coreResponse == null ? 502 : coreResponse.getStatusCode());
        data.put("coreDescription", coreResponse == null ? "Core banking cumulative balance response is empty" : coreResponse.getDescription());

        List<Object> balances = new ArrayList<>();
        Object rawBalances = coreData.get("balances");
        if (rawBalances instanceof List<?>) {
            @SuppressWarnings("unchecked")
            List<Object> castBalances = (List<Object>) rawBalances;
            balances = castBalances;
        }
        if (!balances.isEmpty()) {
            data.put("totalBalance", chooseAmount(data.get("totalBalance"), sumAmounts(balances,
                    "totalBalance", "availableBalance", "ledgerBalance", "currentBalance", "balance")));
            data.put("totalAvailableBalance", chooseAmount(data.get("totalAvailableBalance"), sumAmounts(balances,
                    "availableBalance", "available", "balance")));
            data.put("totalLedgerBalance", chooseAmount(data.get("totalLedgerBalance"), sumAmounts(balances,
                    "ledgerBalance", "ledger", "balance")));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("statusCode", coreResponse == null ? 502 : coreResponse.getStatusCode());
        response.put("description", coreResponse == null ? "Core banking cumulative balance response is empty" : coreResponse.getDescription());
        response.put("data", data);
        return response;
    }

    private List<AddAccountDetails> resolveAccounts(String keyword, List<String> requestedAccountNumbers) {
        if (requestedAccountNumbers != null && !requestedAccountNumbers.isEmpty()) {
            List<AddAccountDetails> accounts = new ArrayList<>();
            Set<Long> seen = new LinkedHashSet<>();
            for (String accountNumber : requestedAccountNumbers) {
                if (!StringUtils.hasText(accountNumber)) {
                    continue;
                }
                List<AddAccountDetails> rows = addAccountDetailsRepo.findByAccountNumberList(accountNumber.trim());
                for (AddAccountDetails row : rows) {
                    if (row != null && "NGN".equalsIgnoreCase(safe(row.getCurrencyCode())) && seen.add(row.getId())) {
                        accounts.add(row);
                    }
                }
            }
            return accounts;
        }

        if (StringUtils.hasText(keyword)) {
            return addAccountDetailsRepo.findByCurrencyCodeAndKeyword("NGN", keyword.trim());
        }
        return addAccountDetailsRepo.findByCurrencyCode("NGN");
    }

    private List<String> uniqueAccountNumbers(List<AddAccountDetails> accounts, List<String> requestedAccountNumbers) {
        Set<String> values = new LinkedHashSet<>();
        if (accounts != null) {
            for (AddAccountDetails account : accounts) {
                if (account != null && StringUtils.hasText(account.getAccountNumber())) {
                    values.add(account.getAccountNumber().trim());
                }
            }
        }
        if (requestedAccountNumbers != null) {
            for (String accountNumber : requestedAccountNumbers) {
                if (StringUtils.hasText(accountNumber)) {
                    values.add(accountNumber.trim());
                }
            }
        }
        return new ArrayList<>(values);
    }

    private List<Map<String, Object>> accountRows(List<AddAccountDetails> accounts) {
        List<Map<String, Object>> rows = new ArrayList<>();
        if (accounts == null) {
            return rows;
        }
        for (AddAccountDetails account : accounts) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", account.getId());
            row.put("accountNumber", account.getAccountNumber());
            row.put("walletId", account.getWalletId());
            row.put("emailAddress", account.getEmailAddress());
            row.put("phoneNumber", account.getPhoneNumber());
            row.put("currencyCode", account.getCurrencyCode() == null ? "NGN" : account.getCurrencyCode().toUpperCase(Locale.ROOT));
            row.put("countryCode", account.getCountryCode());
            row.put("virtualAccountNumber", account.getVirtualAccountNumber());
            row.put("virtualAccountName", account.getVirtualAccountName());
            rows.add(row);
        }
        return rows;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private Object firstAmount(Map<String, Object> data, String... keys) {
        for (String key : keys) {
            Object value = data.get(key);
            if (value != null) {
                return value;
            }
        }
        return BigDecimal.ZERO;
    }

    private Object chooseAmount(Object preferred, BigDecimal fallback) {
        if (preferred == null) {
            return fallback;
        }
        BigDecimal value = amount(preferred);
        return BigDecimal.ZERO.compareTo(value) == 0 && BigDecimal.ZERO.compareTo(fallback) != 0
                ? fallback
                : preferred;
    }

    private BigDecimal sumAmounts(List<Object> rows, String... keys) {
        BigDecimal total = BigDecimal.ZERO;
        for (Object row : rows) {
            if (!(row instanceof Map<?, ?>)) {
                continue;
            }
            Map<?, ?> map = (Map<?, ?>) row;
            for (String key : keys) {
                Object value = map.get(key);
                if (value != null) {
                    total = total.add(amount(value));
                    break;
                }
            }
        }
        return total;
    }

    private BigDecimal amount(Object value) {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        if (value instanceof String && StringUtils.hasText((String) value)) {
            try {
                return new BigDecimal(((String) value).replace(",", "").trim());
            } catch (NumberFormatException ignored) {
                return BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }
}
