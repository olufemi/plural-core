/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.controllers;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.service.canonical.DeviceApprovalService;
import com.finacial.wealth.api.fxpeer.exchange.service.canonical.DeviceChallengeService;
import com.finacial.wealth.api.fxpeer.exchange.service.canonical.TxHashUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import om.finacial.wealth.api.fxpeer.exchange.service.canonical.model.TxApproveRequest;
import om.finacial.wealth.api.fxpeer.exchange.service.canonical.model.TxChallengeResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/canonical/crypto")
public class DeviceTxApprovalController {

    private final DeviceChallengeService challengeService;
    private final DeviceApprovalService approvalService;

    @PostMapping("/{txId}/challenge")
    public TxChallengeResponse challenge(@PathVariable String txId, @PathVariable String emailAddress,
                                         @RequestHeader("X-Device-Id") String deviceId) {

        String userId = emailAddress; // replace

        // IMPORTANT: compute txHash from your tx details.
        // For now, simplest placeholder: hash txId only (replace immediately).
        String txHashB64 = TxHashUtil.txHashB64Placeholder(txId);

        var c = challengeService.createTxChallenge(userId, deviceId, txId, txHashB64, Duration.ofMinutes(2));

        return new TxChallengeResponse(c.getId(), c.getNonceB64(), c.getExpiresAt().toString(), c.getTxId(), c.getTxHashB64());
    }

    @PostMapping("/{txId}/approve")
    public Object approve(@PathVariable String txId,
                          @RequestBody TxApproveRequest req) {

        String userId = req.emailAddress(); // replace

        if (req.alg() == null || !"ES256".equalsIgnoreCase(req.alg())) {
            throw new IllegalArgumentException("Unsupported alg");
        }

        approvalService.approveTx(userId, txId, req.deviceId(), req.deviceKid(), req.challengeId(), req.sigB64());
        return java.util.Map.of("success", true, "txId", txId);
    }
}
