/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.campaign.service;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.profiling.campaign.ennum.CampaignStatus;
import com.finacial.wealth.api.profiling.campaign.entity.Campaign;
import com.finacial.wealth.api.profiling.campaign.entity.CampaignAudit;
import com.finacial.wealth.api.profiling.campaign.repo.CampaignAuditRepository;
import com.finacial.wealth.api.profiling.campaign.repo.CampaignRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Component
public class CampaignScheduler {

    private final CampaignRepository campaignRepo;
    private final CampaignAuditRepository auditRepo;

    public CampaignScheduler(CampaignRepository campaignRepo, CampaignAuditRepository auditRepo) {
        this.campaignRepo = campaignRepo;
        this.auditRepo = auditRepo;
    }

    @Scheduled(cron = "0 * * * * *") // every minute
    @Transactional
    public void tick() {
        Date now = new Date();

        // 1) complete expired actives
        List<Campaign> toComplete = campaignRepo.findActiveToComplete(now);
        for (Campaign c : toComplete) {
            c.setStatus(CampaignStatus.COMPLETED);
            campaignRepo.save(c);
            audit(c.getId(), "AUTO_END", "SYSTEM", "Auto-ended at endAt");
        }

        // 2) start approved ready-to-start if none active
        List<Campaign> activeLocked = campaignRepo.lockActiveCampaigns();
        if (activeLocked != null && !activeLocked.isEmpty()) {
            return; // already have an active campaign
        }

        List<Campaign> ready = campaignRepo.findApprovedReadyToStart(now);
        if (ready == null || ready.isEmpty()) return;

        // choose earliest start
        Campaign pick = ready.get(0);
        pick.setStatus(CampaignStatus.ACTIVE);
        campaignRepo.save(pick);
        audit(pick.getId(), "AUTO_START", "SYSTEM", "Auto-started at scheduled time");
    }

    private void audit(Long campaignId, String action, String actor, String note) {
        CampaignAudit a = new CampaignAudit();
        a.setCampaignId(campaignId);
        a.setAction(action);
        a.setActor(actor);
        a.setEventAt(new Date());
        a.setNote(note);
        auditRepo.save(a);
    }
}

