/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.repo;

import com.financial.wealth.api.transactions.domain.DeviceChangeLimitConfig;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author olufemioshin
 */
public interface DeviceChangeLimitConfigRepo extends
        CrudRepository<DeviceChangeLimitConfig, String> {

    @Query("select ud from DeviceChangeLimitConfig ud where ud.walletNumber=:walletNumber")
    List<DeviceChangeLimitConfig> findByWalletNumberList(String walletNumber);

    @Query("select bs from DeviceChangeLimitConfig bs where bs.walletNumber=:walletNumber")
    DeviceChangeLimitConfig findByWalletNumber(String walletNumber);

}
