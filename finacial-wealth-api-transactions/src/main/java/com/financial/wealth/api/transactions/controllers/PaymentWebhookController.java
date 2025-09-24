/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.controllers;

/**
 *
 * @author olufemioshin
 */
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.wealth.api.transactions.services.utils.HmacUtil;

import com.financial.wealth.api.transactions.tranfaar.services.WebhookKeyService;
import com.financial.wealth.api.transactions.tranfaar.util.HmacSigner;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;

@RestController
@RequestMapping("/webhooks/transfaar")
@RequiredArgsConstructor
public class PaymentWebhookController {

    @Value("${transfaar.hmac-secret}")
    private String hmacSecret;

    @Value("${create.quote.send.to.third.party}")
    private String sendToThirdParty;

    private final WebhookKeyService keyService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Accept RAW body so we can verify exactly what was signed
    @PostMapping("/payment-deposit")
    public ResponseEntity<?> handlePayment(
            @RequestHeader("X-API-Key") String keyId,
            @RequestHeader("timestamp") String timestamp,
            @RequestHeader("signature") String signature,
            @RequestBody String rawBody
    ) {
        if (sendToThirdParty.endsWith("1")) {
            // 1) Basic header checks
            if (!StringUtils.hasText(keyId) || !StringUtils.hasText(timestamp) || !StringUtils.hasText(signature)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing signature headers");
            }

            // 2) Anti-replay (5 minutes window)
            try {
                long ts = Long.parseLong(timestamp); // expecting epoch seconds
                long now = Instant.now().getEpochSecond();
                if (Math.abs(now - ts) > 300) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Stale timestamp");
                }
            } catch (NumberFormatException nfe) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid timestamp");
            }

            // 3) Lookup secret
            /* String secret = keyService.findSecret(keyId);
        if (secret == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unknown key id");
        }*/
            String secret = hmacSecret;

            // 4) Recompute signature: Base64(HMAC_SHA256(secret, timestamp + "." + rawBody))
            /* String base = timestamp + "." + rawBody;
        String expected = HmacUtil.hmacSha256Base64(secret, base);
        if (!HmacUtil.secureEquals(expected, signature)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Bad signature");
        }*/
            String expected = HmacSigner.computeSignature(secret, timestamp, rawBody);
            if (!HmacSigner.secureEquals(signature, expected)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Bad signature");
            }
        }

        // 5) Parse JSON safely and do your business logic
        try {

            return keyService.processPayment(rawBody);

            // return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid JSON");
        }
    }

    private static String asText(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) {
            return null;
        }
        if (v.isTextual()) {
            return v.asText();
        }
        if (v.isBoolean()) {
            return String.valueOf(v.asBoolean());
        }
        if (v.isNumber()) {
            return v.numberValue().toString();
        }
        return v.toString();
    }
}
