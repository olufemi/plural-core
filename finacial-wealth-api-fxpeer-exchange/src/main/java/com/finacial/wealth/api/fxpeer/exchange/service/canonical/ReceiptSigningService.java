/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.service.canonical;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.domain.ReceiptSigningKeyEntity;
import com.finacial.wealth.api.fxpeer.exchange.repo.ReceiptSigningKeyRepo;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import om.finacial.wealth.api.fxpeer.exchange.service.canonical.model.ReceiptSignRequest;
import om.finacial.wealth.api.fxpeer.exchange.service.canonical.model.ReceiptSignResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReceiptSigningService {

    private final ReceiptSigningKeyRepo repo;
    private final KeyMaterialService keyMaterialService;

    @Transactional(readOnly = true)
    public ReceiptSignResponse signold(ReceiptSignRequest req) {
        System.out.println("ReceiptSigningService from transaction ::::::::::::::::::::: " + new Gson().toJson(req));
        validate(req);

        var active = repo.findFirstByStatusOrderByCreatedAtDesc(ReceiptSigningKeyEntity.KeyStatus.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("No ACTIVE receipt signing key"));

        System.out.println("[receipt] ACTIVE kid=" + active.getKid()
                + " pubSpkiLen=" + safeLen(active.getPublicSpkiBase64())
                + " encPrivLen=" + safeLen(active.getPrivatePkcs8EncryptedBase64()));
        System.out.println("[receipt] encPriv head=" + safeHead(active.getPrivatePkcs8EncryptedBase64()));

        var privateKey = keyMaterialService.loadPrivateKey(active.getPrivatePkcs8EncryptedBase64());

        String canonical = ReceiptSignatureUtil.canonicalV1(req);
        String sigB64 = ReceiptSignatureUtil.signRawRsBase64(privateKey, canonical);

        return new ReceiptSignResponse(1, active.getKid(), sigB64, "ES256", "R||S");
    }

    @Transactional(readOnly = true)
    public ReceiptSignResponse sign(ReceiptSignRequest req) {
        try {
            System.out.println("[receipt] sign req=" + new Gson().toJson(req));

            validate(req);

            var active = repo.findFirstByStatusOrderByCreatedAtDesc(ReceiptSigningKeyEntity.KeyStatus.ACTIVE)
                    .orElseThrow(() -> new IllegalStateException("No ACTIVE receipt signing key"));

            var privateKey = keyMaterialService.loadPrivateKey(active.getPrivatePkcs8EncryptedBase64());

            String canonical = ReceiptSignatureUtil.canonicalV1(req);
            System.out.println("[receipt] canonical=" + canonical); // temporarily

            String sigB64 = ReceiptSignatureUtil.signRawRsBase64(privateKey, canonical);
            System.out.println("[receipt] sign OK kid=" + active.getKid() + " sigLen=" + sigB64.length());

            return new ReceiptSignResponse(1, active.getKid(), sigB64, "ES256", "R||S");

        } catch (Exception e) {
            System.out.println("[receipt] SIGN FAILED: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace(); // TEMP: so you see the real line
            throw e; // keep behavior for now
        }
    }

    private int safeLen(String s) {
        return s == null ? 0 : s.length();
    }

    private String safeHead(String s) {
        if (s == null) {
            return "null";
        }
        String t = s.trim();
        return t.substring(0, Math.min(16, t.length()));
    }

    private void validate(ReceiptSignRequest r) {
        if (blank(r.getTxId())) {
            throw new IllegalArgumentException("txId is required");
        }
        if (blank(r.getAmountMinor())) {
            throw new IllegalArgumentException("amountMinor is required");
        }
        if (blank(r.getCurrency())) {
            throw new IllegalArgumentException("currency is required");
        }
        if (blank(r.getSenderId())) {
            throw new IllegalArgumentException("senderId is required");
        }
        if (blank(r.getReceiverId())) {
            throw new IllegalArgumentException("receiverId is required");
        }
        if (blank(r.getTimestampUtcIso())) {
            throw new IllegalArgumentException("timestampUtcIso is required");
        }
        if (blank(r.getStatus())) {
            throw new IllegalArgumentException("status is required");
        }
    }

    private boolean blank(String s) {
        return s == null || s.trim().isEmpty();
    }

}
