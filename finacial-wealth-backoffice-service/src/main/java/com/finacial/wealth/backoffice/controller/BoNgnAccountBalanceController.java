package com.finacial.wealth.backoffice.controller;

import com.finacial.wealth.backoffice.integrations.transactions.TransactionsClient;
import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping({"/backoffice/accounts/ngn", "/bo/backoffice/accounts/ngn"})
public class BoNgnAccountBalanceController {

    private final TransactionsClient transactionsClient;

    @Value("${bo.downstream.internal-token:}")
    private String downstreamInternalToken;

    public BoNgnAccountBalanceController(TransactionsClient transactionsClient) {
        this.transactionsClient = transactionsClient;
    }

    @GetMapping("/balances/cumulative")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public Map<String, Object> getCumulativeBalances(
            @Parameter(hidden = true)
            @RequestParam(required = false) String productCode,
            @Parameter(description = "Optional customer/account search. Searches email, phone, wallet id, account number, and virtual account number. Leave empty to retrieve all NGN accounts.")
            @RequestParam(required = false) String keyword,
            @Parameter(hidden = true)
            @RequestParam(required = false) List<String> accountNumbers,
            @Parameter(hidden = true)
            @RequestParam(defaultValue = "API") String channel) {
        requireInternalTokenConfigured();
        return transactionsClient.getNgnCumulativeAccountBalances(downstreamInternalToken, productCode, keyword,
                accountNumbers, channel);
    }

    @GetMapping("/balances/cumulative/summary")
    @PreAuthorize("hasAnyAuthority('transactions.view','transactions.filter','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_OPERATIONS','ROLE_FINANCE')")
    public Map<String, Object> getCumulativeBalanceSummary(
            @Parameter(description = "Optional customer/account search. Leave empty to retrieve the aggregate total for all local NGN accounts.")
            @RequestParam(required = false) String keyword) {
        requireInternalTokenConfigured();
        return transactionsClient.getNgnCumulativeAccountBalanceSummary(downstreamInternalToken, null, keyword,
                null, "API");
    }

    private void requireInternalTokenConfigured() {
        if (downstreamInternalToken == null || downstreamInternalToken.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "bo.downstream.internal-token is required for NGN cumulative account balance lookup");
        }
    }
}
