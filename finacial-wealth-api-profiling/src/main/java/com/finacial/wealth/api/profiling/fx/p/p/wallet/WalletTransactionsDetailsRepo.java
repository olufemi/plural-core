/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.profiling.fx.p.p.wallet;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    @Query("SELECT u FROM WalletTransactionsDetails u where u.emailAddress = :emailAddress")
    List<WalletTransactionsDetails> findByEmailAddress(@Param("emailAddress") String emailAddress);

    @Query("select ot from WalletTransactionsDetails ot where ot.emailAddress=:emailAddress AND ot.currencyToSell=:currencyToSell")
    Optional<WalletTransactionsDetails> findByEmailAddressAndCurrencyToSell(String emailAddress, String currencyToSell);

    @Query("select coalesce(sum(w.availableQuantity), 0) "
            + "from WalletTransactionsDetails w "
            + "where w.emailAddress = :emailAddress and w.currencyToSell = :currencyToSell")
    BigDecimal sumEscrowAvailableByEmailAndCurrency(@Param("emailAddress") String emailAddress,
            @Param("currencyToSell") String currencyToSell);

}
