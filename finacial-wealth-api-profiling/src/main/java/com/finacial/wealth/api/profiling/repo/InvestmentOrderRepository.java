/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.profiling.repo;

import com.finacial.wealth.api.profiling.domain.InvestmentOrder;
import com.finacial.wealth.api.profiling.models.InvestmentOrderStatus;
import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author olufemioshin
 */
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvestmentOrderRepository extends JpaRepository<InvestmentOrder, Long> {

    Optional<InvestmentOrder> findByOrderRef(String orderRef);

    Optional<InvestmentOrder> findByIdempotencyKey(String idempotencyKey);

    Optional<InvestmentOrder> findByOrderRefAndEmailAddress(String orderRef, String emailAddress);

    List<InvestmentOrder> findByEmailAddress(String emailAddress);

    // NEW: sum of amountBalance per customer + wallet + currency + status
    @Query("select coalesce(sum(o.amountBalance), 0) "
            + "from InvestmentOrder o "
            + "where o.emailAddress     = :emailAddress "
            + "  and o.walletId         = :walletId "
            + "  and o.product.currency = :currency "
            + "  and o.status           = :status")
    BigDecimal sumInvestmentBalanceByEmailWalletAndCurrency(
            @Param("emailAddress") String emailAddress,
            @Param("walletId") String walletId,
            @Param("currency") String currency,
            @Param("status") InvestmentOrderStatus status
    );

    @Query("select o "
            + "from InvestmentOrder o "
            + "where o.emailAddress     = :emailAddress "
            + "  and o.walletId         = :walletId "
            + "  and o.product.currency = :currency "
            + "  and o.status           = :status")
    List<InvestmentOrder> findByEmailAdressWaaletIdCurrencyStatus(
            @Param("emailAddress") String emailAddress,
            @Param("walletId") String walletId,
            @Param("currency") String currency,
            @Param("status") InvestmentOrderStatus status
    );

}
