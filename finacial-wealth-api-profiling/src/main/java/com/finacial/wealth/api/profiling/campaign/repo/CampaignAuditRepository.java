/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.profiling.campaign.repo;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.profiling.campaign.entity.CampaignAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CampaignAuditRepository extends JpaRepository<CampaignAudit, Long> {
    List<CampaignAudit> findByCampaignIdOrderByEventAtAsc(Long campaignId);
}
