/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.services.grp.sav.fulfil;

/**
 *
 * @author olufemioshin
 */
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "group_contribution",
        indexes = {
            @Index(name = "idx_group_cycle", columnList = "groupId,cycleNumber"),
            @Index(name = "idx_unique_ref", columnList = "idempotencyRef", unique = true)
        })
@Data
public class GroupContribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long groupId;

    @Column(nullable = false)
    private Integer cycleNumber;

    @Column(nullable = false, length = 64)
    private String memberWalletId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;

    @Column(nullable = false, length = 120)
    private String idempotencyRef; // groupId:cycle:memberWalletId

    @Column(length = 120)
    private String providerRef; // wallet/ledger provider tx id

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column
    private Instant lastUpdatedAt;

    public enum Status {
        PENDING, PROCESSING, SETTLED, FAILED
    }
}
