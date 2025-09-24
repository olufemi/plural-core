/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.repo;

import com.financial.wealth.api.transactions.domain.ReceiverFailedTransInfo;
import com.financial.wealth.api.transactions.domain.ReceiverHistoryInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author olufemioshin
 */
@Repository
public interface ReceiverHistoryInfoRepo  extends
        CrudRepository<ReceiverHistoryInfo, String> {
    
}
