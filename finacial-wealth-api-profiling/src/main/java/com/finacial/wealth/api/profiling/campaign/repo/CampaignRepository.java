/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.profiling.campaign.repo;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.profiling.campaign.ennum.CampaignStatus;
import com.finacial.wealth.api.profiling.campaign.entity.Campaign;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.util.Date;
import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Campaign c where c.status='ACTIVE'")
    List<Campaign> lockActiveCampaigns();

    boolean existsByStatus(CampaignStatus status);

    @Query("select c from Campaign c where c.status = :status order by c.createdAt desc")
    List<Campaign> findByStatus(@Param("status") CampaignStatus status);

    @Query("select c from Campaign c order by c.createdAt desc")
    List<Campaign> findAllNewestFirst();

    @Query("select c from Campaign c where c.status='APPROVED' and c.startAt <= :now and c.endAt > :now order by c.startAt asc")
    List<Campaign> findApprovedReadyToStart(@Param("now") Date now);

    @Query("select c from Campaign c where c.status='ACTIVE' and c.endAt <= :now")
    List<Campaign> findActiveToComplete(@Param("now") Date now);

    Campaign findFirstByStatusOrderByStartAtAsc(CampaignStatus status);

    @Query("select c from Campaign c "
            + "where c.status = :status and :now between c.startAt and c.endAt "
            + "order by c.startAt asc")
    List<Campaign> findRunning(@Param("status") CampaignStatus status, @Param("now") LocalDateTime now);
    
    List<Campaign> findByStatusOrderByStartAtAsc(CampaignStatus status);

}
