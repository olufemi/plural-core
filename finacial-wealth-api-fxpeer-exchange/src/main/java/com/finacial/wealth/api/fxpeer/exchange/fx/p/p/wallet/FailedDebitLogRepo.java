/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.fx.p.p.wallet;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author olufemioshin
 */
public interface FailedDebitLogRepo extends
        CrudRepository<FailedDebitLog, String> {
    
    List<FailedDebitLog> findByResolvedFalse();

}
