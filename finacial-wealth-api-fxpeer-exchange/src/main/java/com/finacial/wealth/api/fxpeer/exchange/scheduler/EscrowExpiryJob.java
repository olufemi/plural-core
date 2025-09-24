/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.scheduler;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.common.EscrowStatus;
import com.finacial.wealth.api.fxpeer.exchange.escrow.Escrow;
import com.finacial.wealth.api.fxpeer.exchange.escrow.EscrowRepository;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class EscrowExpiryJob {

    private final EscrowRepository repo;

    public EscrowExpiryJob(EscrowRepository repo) {
        this.repo = repo;
    }

    @Scheduled(fixedDelayString = "PT1M")
    @SchedulerLock(name = "escrowExpiryJob")
    public void expireUnfunded() {
        List<Escrow> staleBuyer = repo.findAllByStatusAndExpiresAtBefore(EscrowStatus.PENDING_BUYER, Instant.now());
        List<Escrow> staleSeller = repo.findAllByStatusAndExpiresAtBefore(EscrowStatus.PENDING_SELLER, Instant.now());
        staleBuyer.forEach(e -> {
            e.setStatus(EscrowStatus.REFUNDED);
            repo.save(e);
        });
        staleSeller.forEach(e -> {
            e.setStatus(EscrowStatus.REFUNDED);
            repo.save(e);
        });
// NOTE: actual refunds should call ledger.credit to return funds if any leg funded (Phase 2 detail)
    }
}
