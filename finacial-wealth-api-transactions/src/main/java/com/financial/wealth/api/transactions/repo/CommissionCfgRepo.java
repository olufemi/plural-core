/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.repo;

import com.financial.wealth.api.transactions.domain.CommissionCfg;
import java.util.List;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author olufemioshin
 */
public interface CommissionCfgRepo extends CrudRepository<CommissionCfg, Long> {
    
    @Query("SELECT c FROM CommissionCfg c WHERE c.transType = :transType")
    List<CommissionCfg> findAllByTransactionType(String transType);
    
}
