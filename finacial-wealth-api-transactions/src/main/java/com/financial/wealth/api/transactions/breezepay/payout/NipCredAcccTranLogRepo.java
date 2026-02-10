/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.financial.wealth.api.transactions.breezepay.payout;

import com.financial.wealth.api.transactions.domain.FinWealthPaymentTransaction;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author olufemioshin
 */
public interface NipCredAcccTranLogRepo extends
        CrudRepository<NipCredAcccTranLog, String>{
    
     Optional<NipCredAcccTranLog> findByCreditAccount(String creditAccount);

    
}
