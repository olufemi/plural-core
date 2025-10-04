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
@Table(name = "group_payout",
        uniqueConstraints = @UniqueConstraint(name = "uk_group_cycle_payout", columnNames = {"groupId", "cycleNumber"}))
@Data
public class GroupPayout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long groupId;

    @Column(nullable = false)
    private Integer cycleNumber;

    @Column(nullable = false, length = 64)
    private String receiverWalletId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;

    @Column(nullable = false, length = 120)
    private String idempotencyRef; // groupId:cycle:payout

    @Column(length = 120)
    private String providerRef; // wallet/ledger credit tx id

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column
    private Instant lastUpdatedAt;

    public enum Status {
        PENDING, PROCESSING, SETTLED, FAILED
    }
}
