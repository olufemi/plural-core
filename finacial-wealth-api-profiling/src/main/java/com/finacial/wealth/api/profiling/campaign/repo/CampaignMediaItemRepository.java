/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.profiling.campaign.repo;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.profiling.campaign.entity.CampaignMediaItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CampaignMediaItemRepository extends JpaRepository<CampaignMediaItem, Long> {

    List<CampaignMediaItem> findByCampaignIdOrderByOrderNoAsc(Long campaignId);

    void deleteByCampaignId(Long campaignId);
}
