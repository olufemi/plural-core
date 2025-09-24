/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.utility.repository;

import com.finacial.wealth.api.utility.domains.SendAndSaveDetails;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author olufemioshin
 */
public interface SendAndSaveDetailsRepo extends
        CrudRepository<SendAndSaveDetails, String> {

    @Query("select config from SendAndSaveDetails config where config.walletNumber=:walletNumber")
    List<SendAndSaveDetails> findByWalletNumber(String walletNumber);

    @Query("select config from SendAndSaveDetails config where config.walletNumber=:walletNumber")
    SendAndSaveDetails findByWalletNumberSingle(String walletNumber);

}
