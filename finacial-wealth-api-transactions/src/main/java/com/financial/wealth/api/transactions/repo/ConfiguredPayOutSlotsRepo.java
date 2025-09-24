/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.repo;

import com.financial.wealth.api.transactions.domain.ConfiguredPayOutSlots;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author olufemioshin
 */
public interface ConfiguredPayOutSlotsRepo extends JpaRepository<ConfiguredPayOutSlots, Long> {

    @Override
    List<ConfiguredPayOutSlots> findAll();
    
}
