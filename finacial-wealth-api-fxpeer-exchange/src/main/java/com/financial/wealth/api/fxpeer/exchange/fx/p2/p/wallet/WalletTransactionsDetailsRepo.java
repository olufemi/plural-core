/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.fx.p2.p.wallet;

import com.finacial.wealth.api.fxpeer.exchange.order.Order;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 *
 * @author olufemioshin
 */
@Repository
public interface WalletTransactionsDetailsRepo extends
        JpaRepository<WalletTransactionsDetails, Long> {

    @Query("SELECT u FROM WalletTransactionsDetails u where u.correlationId = :correlationId")
    List<WalletTransactionsDetails> findByCorrelationId(@Param("correlationId") String correlationId);

    @Query("SELECT u FROM WalletTransactionsDetails u where u.correlationId = :correlationId")
    WalletTransactionsDetails findByCorrelationIdUpdated(@Param("correlationId") String correlationId);

}
