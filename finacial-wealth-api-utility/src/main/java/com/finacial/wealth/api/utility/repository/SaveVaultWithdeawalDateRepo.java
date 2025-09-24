/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.utility.repository;


import com.finacial.wealth.api.utility.domains.SaveVaultWithdeawalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author olufemioshin
 */
public interface SaveVaultWithdeawalDateRepo extends
        CrudRepository<SaveVaultWithdeawalDate, String> {

    @Query("select config from SaveVaultWithdeawalDate config where config.walletNumber=:walletNumber")
    List<SaveVaultWithdeawalDate> findByWalletNumber(String walletNumber);

    @Query("select config from SaveVaultWithdeawalDate config where config.walletNumber=:walletNumber")
    SaveVaultWithdeawalDate findByWalletNumberUpdate(String walletNumber);

    @Query("select config from SaveVaultWithdeawalDate config where config.walletNumber=:walletNumber and config.transId=:transId")
    SaveVaultWithdeawalDate findByWalletNumberTransId(String walletNumber, String transId);

    @Query("select config from SaveVaultWithdeawalDate config where config.scheduleSignature=:scheduleSignature")
    List<SaveVaultWithdeawalDate> findByScheduleSignature(String scheduleSignature);

    @Query("select config from SaveVaultWithdeawalDate config where config.transId=:transId")
    List<SaveVaultWithdeawalDate> findByTransId(String transId);

    @Query("select config from SaveVaultWithdeawalDate config where config.transId=:transId")
    SaveVaultWithdeawalDate findByTransIdUp(String transId);

}
