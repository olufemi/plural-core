package com.financial.wealth.api.transactions.controllers;

import com.financial.wealth.api.transactions.services.AdminNgnTransactionService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/admin/transactions")
public class AdminNgnTransactionController {

    private final AdminNgnTransactionService adminNgnTransactionService;

    @Value("${backoffice.internal.auth-token:}")
    private String backofficeInternalAuthToken;

    public AdminNgnTransactionController(AdminNgnTransactionService adminNgnTransactionService) {
        this.adminNgnTransactionService = adminNgnTransactionService;
    }

    @GetMapping("/ngn")
    public ResponseEntity<Map<String, Object>> listNgnTransactions(
            @RequestHeader(name = "X-Backoffice-Internal-Token", required = false) String internalToken,
            @RequestParam(required = false) String customer,
            @RequestParam(required = false) String walletId,
            @RequestParam(required = false) String transactionId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(defaultValue = "true") boolean includeLegacyNullCurrency,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        requireBackofficeInternalToken(internalToken);
        return new ResponseEntity<>(adminNgnTransactionService.listNgnTransactions(customer, walletId, transactionId,
                type, status, q, fromDate, toDate, includeLegacyNullCurrency, page, size), HttpStatus.OK);
    }

    @GetMapping("/ngn/{transactionId}")
    public ResponseEntity<Map<String, Object>> getNgnTransactionDetails(
            @RequestHeader(name = "X-Backoffice-Internal-Token", required = false) String internalToken,
            @PathVariable String transactionId,
            @RequestParam(defaultValue = "true") boolean includeLegacyNullCurrency) {
        requireBackofficeInternalToken(internalToken);
        return new ResponseEntity<>(adminNgnTransactionService.getNgnTransactionDetails(transactionId,
                includeLegacyNullCurrency), HttpStatus.OK);
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
