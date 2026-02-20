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
import lombok.RequiredArgsConstructor;
import om.finacial.wealth.api.fxpeer.exchange.service.canonical.model.ReceiptKeysResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReceiptKeyService {

    private final ReceiptSigningKeyRepo repo;

    @Transactional(readOnly = true)
    public ReceiptKeysResponse getReceiptKeys() {
        var active = repo.findFirstByStatusOrderByCreatedAtDesc(ReceiptSigningKeyEntity.KeyStatus.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("No ACTIVE receipt signing key"));

        var keys = repo.findAllForDistribution().stream()
                .map(k -> new ReceiptKeysResponse.KeyItem(k.getKid(), k.getPublicSpkiBase64()))
                .toList();

        return new ReceiptKeysResponse(active.getKid(), keys);
    }
}