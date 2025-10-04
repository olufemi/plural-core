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

import java.util.Optional;

public interface GroupPayoutRepo extends JpaRepository<GroupPayout, Long> {
    Optional<GroupPayout> findByGroupIdAndCycleNumber(Long groupId, Integer cycle);
}
