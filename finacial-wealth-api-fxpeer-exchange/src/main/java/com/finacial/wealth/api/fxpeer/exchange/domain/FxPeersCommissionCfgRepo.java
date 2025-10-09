/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.domain;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author olufemioshin
 */
public interface FxPeersCommissionCfgRepo extends CrudRepository<FxPeersCommissionCfg, Long> {
    
    @Query("SELECT c FROM FxPeersCommissionCfg c WHERE c.transType = :transType")
    List<FxPeersCommissionCfg> findAllByTransactionType(String transType);
    
     @Query("SELECT c FROM FxPeersCommissionCfg c WHERE c.transType = :transType and  c.currencyCode = :currencyCode")
    List<FxPeersCommissionCfg> findAllByTransactionTypeAndCurrencyCode(String transType, String currencyCode);
    
}
