/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.breezepay.payout;

import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author olufemioshin
 */
public interface PaymentsFailedTransInfoRepo  extends
        CrudRepository<PaymentsFailedTransInfo, String>{
    
}
