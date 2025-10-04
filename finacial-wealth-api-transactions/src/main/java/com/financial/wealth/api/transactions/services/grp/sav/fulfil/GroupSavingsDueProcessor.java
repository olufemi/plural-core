/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.services.grp.sav.fulfil;

/**
 *
 * @author olufemioshin
 */
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.wealth.api.transactions.domain.GroupSavingsData;
import com.financial.wealth.api.transactions.models.AddMembersModels;
import com.financial.wealth.api.transactions.repo.GroupSavingsDataRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "groupsavings.processor.enabled", havingValue = "true", matchIfMissing = true)
public class GroupSavingsDueProcessor {

    private final GroupSavingsCycleRepo cycleRepo;
    private final GroupContributionRepo contribRepo;
    private final GroupPayoutRepo payoutRepo;
    private final GroupSavingsDataRepo groupRepo; // your existing repo
    private final WalletFacade wallet;

    @Value("${groupsavings.timezone:Africa/Lagos}")
    private String tz;

    /*
    So it fires every day at 03:10:00 (3:10 AM), server/JVM local timezone by default.

In Spring @Scheduled: @Scheduled(cron = "0 10 3 * * *")

With a timezone (WAT): @Scheduled(cron = "0 10 3 * * *", zone = "Africa/Lagos")

Classic Linux 5-field equivalent (no seconds): 10 3 * * *
     */
    @Scheduled(cron = "${groupsavings.processor.cron}", zone = "${groupsavings.timezone:Africa/Lagos}")
    @SchedulerLock(name = "GroupSavingsDueProcessor.run", lockAtMostFor = "10m", lockAtLeastFor = "30s")
    @Transactional
    public void run() {
        LocalDate today = LocalDate.now(ZoneId.of(tz));
        List<GroupSavingsCycle> due = cycleRepo.findDueCycles(today);
        for (GroupSavingsCycle cycle : due) {
            try {
                processCycle(cycle);
            } catch (Exception ex) {
                log.error("Cycle processing failed groupId={}, cycle={}, err={}",
                        cycle.getGroupId(), cycle.getCycleNumber(), ex.toString(), ex);
            }
        }
    }

    private void processCycle(GroupSavingsCycle cycle) throws Exception {
        GroupSavingsData group = groupRepo.findById(cycle.getGroupId())
                .orElseThrow(() -> new IllegalStateException("Group not found " + cycle.getGroupId()));

        GroupSavingsCycle locked = cycleRepo.lockOne(group.getId(), cycle.getCycleNumber())
                .orElseThrow(() -> new IllegalStateException("Cycle not found/lock failed"));

        if (locked.getStatus() == GroupSavingsCycle.CycleStatus.PAID
                || locked.getStatus() == GroupSavingsCycle.CycleStatus.EXPIRED) {
            return;
        }

        LocalDate today = LocalDate.now(ZoneId.of(tz));
        if (today.isAfter(locked.getContributionWindowEnd())) {
            locked.setStatus(GroupSavingsCycle.CycleStatus.EXPIRED);
            locked.setLastUpdatedAt(Instant.now());
            cycleRepo.save(locked);
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        List<AddMembersModels> members = mapper.readValue(group.getAddedMembersModels(), new TypeReference<List<AddMembersModels>>() {
        });
        BigDecimal perMemberAmount = group.getGroupSavingAmount(); // or your per-member formula

        // Ensure contributions (idempotent)
        for (AddMembersModels m : members) {
            String ref = idemRef(group.getId(), locked.getCycleNumber(), m.getMemberId());

            GroupContribution gc = contribRepo.findByIdempotencyRef(ref).orElse(null);
            if (gc == null) {
                gc = new GroupContribution();
                gc.setGroupId(group.getId());
                gc.setCycleNumber(locked.getCycleNumber());
                gc.setMemberWalletId(m.getMemberId());
                gc.setAmount(perMemberAmount);
                gc.setIdempotencyRef(ref);
                contribRepo.save(gc);
            }

            if (gc.getStatus() == GroupContribution.Status.PENDING
                    || gc.getStatus() == GroupContribution.Status.FAILED) {
                try {
                    gc.setStatus(GroupContribution.Status.PROCESSING);
                    gc.setLastUpdatedAt(Instant.now());
                    contribRepo.save(gc);

                    String providerRef = wallet.debit(m.getMemberId(), perMemberAmount, ref);
                    gc.setProviderRef(providerRef);
                    gc.setStatus(GroupContribution.Status.SETTLED);
                    gc.setLastUpdatedAt(Instant.now());
                    contribRepo.save(gc);
                } catch (Exception ex) {
                    log.warn("Debit failed member={} group={} cycle={} err={}",
                            m.getMemberId(), group.getId(), locked.getCycleNumber(), ex.toString());
                    gc.setStatus(GroupContribution.Status.FAILED);
                    gc.setLastUpdatedAt(Instant.now());
                    contribRepo.save(gc);
                }
            }
        }

        long settled = contribRepo.countByGroupIdAndCycleNumberAndStatus(
                group.getId(), locked.getCycleNumber(), GroupContribution.Status.SETTLED);
        if (settled < members.size()) {
            locked.setStatus(GroupSavingsCycle.CycleStatus.IN_PROGRESS);
            locked.setLastUpdatedAt(Instant.now());
            cycleRepo.save(locked);
            return;
        }

        boolean shouldPayoutNow = shouldPayout(group.getPayoutPolicy().toString(), locked.getCycleNumber(), group.getCycleNumber());
        if (!shouldPayoutNow) {
            locked.setStatus(GroupSavingsCycle.CycleStatus.AWAITING_PAYOUT);
            locked.setLastUpdatedAt(Instant.now());
            cycleRepo.save(locked);
            return;
        }

        String receiverWalletId = SlotResolver.resolveReceiverWallet(group, locked.getCycleNumber());
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < members.size(); i++) {
            total = total.add(perMemberAmount);
        }

        GroupPayout payout = payoutRepo.findByGroupIdAndCycleNumber(group.getId(), locked.getCycleNumber()).orElse(null);
        if (payout == null) {
            payout = new GroupPayout();
            payout.setGroupId(group.getId());
            payout.setCycleNumber(locked.getCycleNumber());
            payout.setReceiverWalletId(receiverWalletId);
            payout.setAmount(total);
            payout.setIdempotencyRef(group.getId() + ":" + locked.getCycleNumber() + ":payout");
            payoutRepo.save(payout);
        }

        if (payout.getStatus() == GroupPayout.Status.SETTLED) {
            locked.setStatus(GroupSavingsCycle.CycleStatus.PAID);
            locked.setLastUpdatedAt(Instant.now());
            cycleRepo.save(locked);
            return;
        }

        try {
            payout.setStatus(GroupPayout.Status.PROCESSING);
            payout.setLastUpdatedAt(Instant.now());
            payoutRepo.save(payout);

            String providerRef = wallet.credit(receiverWalletId, total, payout.getIdempotencyRef());
            payout.setProviderRef(providerRef);
            payout.setStatus(GroupPayout.Status.SETTLED);
            payout.setLastUpdatedAt(Instant.now());
            payoutRepo.save(payout);

            locked.setStatus(GroupSavingsCycle.CycleStatus.PAID);
            locked.setLastUpdatedAt(Instant.now());
            cycleRepo.save(locked);
        } catch (Exception ex) {
            log.error("Payout failed group={} cycle={} err={}", group.getId(), locked.getCycleNumber(), ex.toString());
            payout.setStatus(GroupPayout.Status.FAILED);
            payout.setLastUpdatedAt(Instant.now());
            payoutRepo.save(payout);
        }
    }

    private boolean shouldPayout(String payoutPolicyName, int currentCycle, int maxCycle) {
        // PayoutPolicy = EACH_CYCLE | PERIOD_END | AFTER_ALL_CONTRIBUTIONS
        if ("EACH_CYCLE".equalsIgnoreCase(payoutPolicyName)) {
            return true;
        }
        if ("PERIOD_END".equalsIgnoreCase(payoutPolicyName)) {
            return true;
        }
        if ("AFTER_ALL_CONTRIBUTIONS".equalsIgnoreCase(payoutPolicyName)) {
            return currentCycle == maxCycle;
        }
        throw new IllegalArgumentException("Unsupported policy " + payoutPolicyName);
    }

    private String idemRef(Long groupId, int cycle, String memberWalletId) {
        return groupId + ":" + cycle + ":" + memberWalletId;
    }
}
