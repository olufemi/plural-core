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

import java.util.Optional;

public interface GroupPayoutRepo extends JpaRepository<GroupPayout, Long> {
    Optional<GroupPayout> findByGroupIdAndCycleNumber(Long groupId, Integer cycle);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from GroupPayout p where p.groupId=:groupId and p.cycleNumber=:cycle")
    Optional<GroupPayout> lockByGroupIdAndCycleNumber(@Param("groupId") Long groupId, @Param("cycle") Integer cycle);
}
