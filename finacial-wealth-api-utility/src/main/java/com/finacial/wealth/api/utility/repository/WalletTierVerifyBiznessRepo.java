/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.utility.repository;


import com.finacial.wealth.api.utility.domains.WalletTierVerifyBizness;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author olufemioshin
 */
public interface WalletTierVerifyBiznessRepo extends
        CrudRepository<WalletTierVerifyBizness, String> {

    List<WalletTierVerifyBizness> findByWalletNo(String walletNo);

    @Query("select bs from WalletTierVerifyBizness bs where bs.walletNo=:walletNo")
    WalletTierVerifyBizness findByUpdateWalletNo(String walletNo);

}
