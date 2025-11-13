/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.fx.p.p.wallet;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author olufemioshin
 */
public interface WalletIndivTransactionsDetailsRepo extends
        CrudRepository<WalletIndivTransactionsDetails, String> {

    @Query("SELECT u FROM WalletIndivTransactionsDetails u where u.correlationId = :correlationId")
    List<WalletIndivTransactionsDetails> findByCorrelationId(@Param("correlationId") String correlationId);

    @Query("SELECT u FROM WalletIndivTransactionsDetails u where u.sellerEmailAddress = :sellerEmailAddress")
    List<WalletIndivTransactionsDetails> findBySellerEmailAddress(@Param("sellerEmailAddress") String sellerEmailAddress);

    @Query("SELECT u FROM WalletIndivTransactionsDetails u where u.buyerEmailAddress = :buyerEmailAddress")
    List<WalletIndivTransactionsDetails> findByBuyerEmailAddress(@Param("buyerEmailAddress") String buyerEmailAddress);

}
