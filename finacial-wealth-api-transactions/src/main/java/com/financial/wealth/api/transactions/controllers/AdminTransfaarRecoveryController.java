package com.financial.wealth.api.transactions.controllers;

import com.financial.wealth.api.transactions.tranfaar.services.WebhookKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/transfaar")
@RequiredArgsConstructor
public class AdminTransfaarRecoveryController {

    private final WebhookKeyService keyService;

    @GetMapping("/payment-deposit/recovery-candidates")
    public ResponseEntity<?> getDepositRecoveryCandidates(
            @RequestParam(value = "statuses", defaultValue = "PENDING,FAILED") String statuses,
            @RequestParam(value = "limit", defaultValue = "50") int limit
    ) {
        return ResponseEntity.ok(keyService.getDepositRecoveryCandidates(statuses, limit));
    }

    @PostMapping("/payment-deposit/replay/{quoteId}")
    public ResponseEntity<?> replayDeposit(@PathVariable("quoteId") String quoteId) {
        return keyService.replayDepositByQuoteId(quoteId);
    }
}
