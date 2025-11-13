/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.fx.p.p.wallet;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author olufemioshin
 */
public interface FailedCreditLogRepo extends
        CrudRepository<FailedCreditLog, String> {
    
    List<FailedCreditLog> findByResolvedFalse();
    
    @Query("SELECT config from FailedCreditLog config where config.transactionId=:transactionId")
    List<FailedCreditLog> findByTransactionId(String transactionId);
    
    @Query("SELECT config from FailedCreditLog config where config.transactionId=:transactionId")
    FailedCreditLog findByTransactionIdUpdate(String transactionId);

}
