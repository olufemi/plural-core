/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author olufemioshin
 */
public interface AddAccountDetailsRepo extends JpaRepository<AddAccountDetails, Long> {

    boolean existsByAccountNumber(String phoneNumber);

    boolean existsByWalletId(String walletId);

    @Query("SELECT u FROM AddAccountDetails u where u.walletId = :walletId")
    List<AddAccountDetails> findByWalletIdrData(@Param("walletId") String walletId);

}
