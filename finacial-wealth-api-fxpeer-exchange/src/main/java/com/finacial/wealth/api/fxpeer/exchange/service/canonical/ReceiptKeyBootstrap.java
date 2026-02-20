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
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.ECGenParameterSpec;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import org.springframework.stereotype.Service;

@Component
@RequiredArgsConstructor
@Service
public class ReceiptKeyBootstrap implements ApplicationRunner {

    private final ReceiptSigningKeyRepo repo;
    private final KeyMaterialCryptoService keyCrypto;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (repo.findFirstByStatusOrderByCreatedAtDesc(ReceiptSigningKeyEntity.KeyStatus.ACTIVE).isPresent()) return;

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(new ECGenParameterSpec("secp256r1"));
        KeyPair kp = kpg.generateKeyPair();

        String kid = "k_" + DateTimeFormatter.BASIC_ISO_DATE.format(LocalDate.now()) + "_01";
        String publicSpkiB64 = Base64.getEncoder().encodeToString(kp.getPublic().getEncoded());

        String privateEncB64 = keyCrypto.encrypt(kp.getPrivate().getEncoded());

        ReceiptSigningKeyEntity e = new ReceiptSigningKeyEntity();
        e.setKid(kid);
        e.setStatus(ReceiptSigningKeyEntity.KeyStatus.ACTIVE);
        e.setPublicSpkiBase64(publicSpkiB64);
        e.setPrivatePkcs8EncryptedBase64(privateEncB64);
        e.setCreatedAt(Instant.now());

        repo.save(e);
    }
}
