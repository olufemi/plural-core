/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.financial.wealth.api.transactions.repo;

import com.financial.wealth.api.transactions.domain.CreateQuoteResLog;
import com.financial.wealth.api.transactions.domain.FailedCreditLog;
import com.financial.wealth.api.transactions.domain.FailedDebitLog;
import com.financial.wealth.api.transactions.domain.SuccessDebitLog;
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
