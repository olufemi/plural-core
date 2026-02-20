/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.controllers;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.service.canonical.ReceiptKeyService;
import com.finacial.wealth.api.fxpeer.exchange.service.canonical.ReceiptSigningService;
import lombok.RequiredArgsConstructor;
import om.finacial.wealth.api.fxpeer.exchange.service.canonical.model.ReceiptKeysResponse;
import om.finacial.wealth.api.fxpeer.exchange.service.canonical.model.ReceiptSignRequest;
import om.finacial.wealth.api.fxpeer.exchange.service.canonical.model.ReceiptSignResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/canonical/crypto")
@RequiredArgsConstructor
public class ReceiptCryptoController {

    private final ReceiptKeyService receiptKeyService;
    private final ReceiptSigningService receiptSigningService;

    @GetMapping("/keys")
    public ReceiptKeysResponse getReceiptKeys() {
        return receiptKeyService.getReceiptKeys();
    }

    @PostMapping("/sign")
    public ReceiptSignResponse sign(@RequestBody ReceiptSignRequest req) {
        return receiptSigningService.sign(req);
    }
}
