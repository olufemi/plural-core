/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.domain;

/**
 *
 * @author olufemioshin
 */
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "receipt_signing_keys")
@Getter @Setter @NoArgsConstructor
public class ReceiptSigningKeyEntity {

    @Id
    @Column(length = 64)
    private String kid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private KeyStatus status; // ACTIVE, RETIRED

    @Lob
    @Column(name = "public_spki_b64", nullable = false, length = 5000)
    private String publicSpkiBase64;

    @Lob
    @Column(name = "private_pkcs8_enc_b64", nullable = false, length = 8000)
    private String privatePkcs8EncryptedBase64;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant rotatedAt;

    public enum KeyStatus { ACTIVE, RETIRED }
}
