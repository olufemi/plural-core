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
public interface SuccessDebitLogRepo extends
        CrudRepository<SuccessDebitLog, String> {

    List<SuccessDebitLog> findByResolvedFalse();
    
    List<SuccessDebitLog> findByMarkForRollBack(int markForRollBack);

    @Query("SELECT config from SuccessDebitLog config where config.transactionId=:transactionId")
    List<SuccessDebitLog> findByTransactionId(String transactionId);

    @Query("SELECT config from SuccessDebitLog config where config.transactionId=:transactionId")
    SuccessDebitLog findByTransactionIdUpdate(String transactionId);

}
