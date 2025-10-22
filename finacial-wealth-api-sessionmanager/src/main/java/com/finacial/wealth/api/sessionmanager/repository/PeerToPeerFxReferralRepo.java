/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.sessionmanager.repository;

import com.finacial.wealth.api.sessionmanager.entities.PeerToPeerFxReferral;
import com.finacial.wealth.api.sessionmanager.entities.WalletIndivTransactionsDetails;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author olufemioshin
 */
public interface PeerToPeerFxReferralRepo extends
        CrudRepository<PeerToPeerFxReferral, String> {

    @Query("SELECT u FROM PeerToPeerFxReferral u where u.emailAddress = :emailAddress")
    List<PeerToPeerFxReferral> findByEmailAddress(@Param("emailAddress") String emailAddress);

}
