/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.financial.wealth.api.transactions.services.grp.sav.fulfil;

/**
 *
 * @author olufemioshin
 */

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GroupSavingsCycleRepo extends JpaRepository<GroupSavingsCycle, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from GroupSavingsCycle c where c.groupId=:groupId and c.cycleNumber=:cycle")
    Optional<GroupSavingsCycle> lockOne(@Param("groupId") Long groupId, @Param("cycle") Integer cycle);

    @Query("select c from GroupSavingsCycle c " +
            "where c.contributionDate <= :today and c.contributionWindowEnd >= :today " +
            "and c.status in (com.financial.wealth.api.transactions.services.grp.sav.fulfil.GroupSavingsCycle$CycleStatus.PENDING," +
            "                 com.financial.wealth.api.transactions.services.grp.sav.fulfil.GroupSavingsCycle$CycleStatus.IN_PROGRESS," +
            "                 com.financial.wealth.api.transactions.services.grp.sav.fulfil.GroupSavingsCycle$CycleStatus.AWAITING_PAYOUT)")
    List<GroupSavingsCycle> findDueCycles(@Param("today") LocalDate today);
}
