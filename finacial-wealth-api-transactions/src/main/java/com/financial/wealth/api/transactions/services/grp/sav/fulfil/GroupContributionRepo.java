/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.financial.wealth.api.transactions.services.grp.sav.fulfil;

/**
 *
 * @author olufemioshin
 */
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface GroupContributionRepo extends JpaRepository<GroupContribution, Long> {

    Optional<GroupContribution> findByIdempotencyRef(String ref);

    List<GroupContribution> findByGroupIdAndCycleNumber(Long groupId, Integer cycle);

    long countByGroupIdAndCycleNumberAndStatus(Long groupId, Integer cycle, GroupContribution.Status status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from GroupContribution c where c.groupId=:groupId and c.cycleNumber=:cycle")
    List<GroupContribution> lockByGroupIdAndCycleNumber(@Param("groupId") Long groupId, @Param("cycle") Integer cycle);
}
