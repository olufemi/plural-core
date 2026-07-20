package com.financial.wealth.api.transactions.controllers;

import com.financial.wealth.api.transactions.models.ApiResponseModel;
import com.financial.wealth.api.transactions.services.ReversalAdminService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/admin/reversals")
public class AdminReversalController {

    private final ReversalAdminService reversalAdminService;

    @Value("${backoffice.internal.auth-token:}")
    private String backofficeInternalAuthToken;

    public AdminReversalController(ReversalAdminService reversalAdminService) {
        this.reversalAdminService = reversalAdminService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponseModel> getSummary() {
        return new ResponseEntity<>(reversalAdminService.getSummary(), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<ApiResponseModel> getCases(@RequestParam(required = false) String status) {
        return new ResponseEntity<>(reversalAdminService.getCases(status), HttpStatus.OK);
    }

    @PostMapping("/{transactionId}/retry")
    public ResponseEntity<ApiResponseModel> retry(
            @PathVariable String transactionId,
            @RequestHeader(name = "X-Backoffice-Internal-Token", required = false) String internalToken
    ) {
        requireBackofficeInternalToken(internalToken);
        return new ResponseEntity<>(reversalAdminService.retryCase(transactionId), HttpStatus.OK);
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
