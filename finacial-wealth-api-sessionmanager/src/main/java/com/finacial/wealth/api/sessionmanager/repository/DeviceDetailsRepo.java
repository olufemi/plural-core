/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.repo;

import com.financial.wealth.api.transactions.domain.DeviceDetails;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author olufemioshin
 */
public interface DeviceDetailsRepo extends
        CrudRepository<DeviceDetails, String> {

    Optional<DeviceDetails> findByToken(String token);

    List<DeviceDetails> findAllByWalletId(String walletId);

}
