package com.financial.wealth.api.transactions.controllers;

import com.financial.wealth.api.transactions.services.AdminNgnAccountBalanceService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/admin/accounts/ngn")
public class AdminNgnAccountBalanceController {

    private final AdminNgnAccountBalanceService adminNgnAccountBalanceService;

    @Value("${backoffice.internal.auth-token:}")
    private String backofficeInternalAuthToken;

    public AdminNgnAccountBalanceController(AdminNgnAccountBalanceService adminNgnAccountBalanceService) {
        this.adminNgnAccountBalanceService = adminNgnAccountBalanceService;
    }

    @GetMapping("/balances/cumulative")
    public ResponseEntity<Map<String, Object>> getCumulativeBalances(
            @RequestHeader(name = "X-Backoffice-Internal-Token", required = false) String internalToken,
            @RequestHeader(name = "Authorization", required = false) String walletSystemAuthorization,
            @RequestParam(required = false) String productCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<String> accountNumbers,
            @RequestParam(defaultValue = "API") String channel) {
        requireBackofficeInternalToken(internalToken);
        return new ResponseEntity<>(adminNgnAccountBalanceService.getCumulativeBalances(productCode, keyword,
                accountNumbers, channel, walletSystemAuthorization), HttpStatus.OK);
    }

    @GetMapping("/balances/cumulative/summary")
    public ResponseEntity<Map<String, Object>> getCumulativeBalanceSummary(
            @RequestHeader(name = "X-Backoffice-Internal-Token", required = false) String internalToken,
            @RequestHeader(name = "Authorization", required = false) String walletSystemAuthorization,
            @RequestParam(required = false) String productCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) List<String> accountNumbers,
            @RequestParam(defaultValue = "API") String channel) {
        requireBackofficeInternalToken(internalToken);
        return new ResponseEntity<>(adminNgnAccountBalanceService.getCumulativeBalanceSummary(productCode, keyword,
                accountNumbers, channel, walletSystemAuthorization), HttpStatus.OK);
    }

    private void requireBackofficeInternalToken(String suppliedToken) {
        if (backofficeInternalAuthToken == null || backofficeInternalAuthToken.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Backoffice internal token is not configured");
        }
        if (suppliedToken == null || suppliedToken.trim().isEmpty()
                || !MessageDigest.isEqual(
                        backofficeInternalAuthToken.getBytes(StandardCharsets.UTF_8),
                        suppliedToken.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid backoffice internal token");
        }
    }
}
