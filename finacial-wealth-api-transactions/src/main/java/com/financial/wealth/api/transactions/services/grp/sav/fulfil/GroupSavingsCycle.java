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
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "group_savings_cycle",
        uniqueConstraints = @UniqueConstraint(name = "uk_group_cycle", columnNames = {"groupId", "cycleNumber"}))
@Data
public class GroupSavingsCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long groupId; // maps to GroupSavingsData.id

    @Column(nullable = false)
    private Integer cycleNumber; // 1..N (N = numberOfMembers)

    @Column(nullable = false)
    private LocalDate contributionDate;

    @Column(nullable = false)
    private LocalDate contributionWindowEnd;

    @Column
    private LocalDate payoutDate; // may be null until finalization (AFTER_ALL_CONTRIBUTIONS)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CycleStatus status = CycleStatus.PENDING;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column
    private Instant lastUpdatedAt;

    public enum CycleStatus {
        PENDING,
        IN_PROGRESS,
        AWAITING_PAYOUT,
        PAID,
        EXPIRED
    }
}

