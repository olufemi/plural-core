/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.repo;

import com.financial.wealth.api.transactions.domain.FinWealthPaymentTransaction;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public interface FinWealthPaymentTransactionRepo
        extends JpaRepository<FinWealthPaymentTransaction, String> {

    @Query("SELECT o FROM FinWealthPaymentTransaction o WHERE o.sender = :walletNo OR o.receiver = :walletNo")
    List<FinWealthPaymentTransaction> findByWalletNoList(@Param("walletNo") String walletNo);

    @Query("SELECT bs FROM FinWealthPaymentTransaction bs WHERE bs.walletNo = :walletNo")
    FinWealthPaymentTransaction findByWalletNoDe(@Param("walletNo") String walletNo);

    Optional<FinWealthPaymentTransaction> findByWalletNo(String walletNo);

    // paging by paymentType (recommended)
    Page<FinWealthPaymentTransaction> findByPaymentTypeOrderByIdDesc(String paymentType, Pageable pageable);

    @Query("SELECT bs FROM FinWealthPaymentTransaction bs WHERE bs.transactionId = :transactionId")
    List<FinWealthPaymentTransaction> findByTransationidNoDe(@Param("transactionId") String transactionId);

    @Query("SELECT bs FROM FinWealthPaymentTransaction bs WHERE bs.transactionId = :transactionId")
    FinWealthPaymentTransaction findByTransationId(@Param("transactionId") String transactionId);

    @Query("SELECT COALESCE(SUM(m.sentAmount), 0) FROM FinWealthPaymentTransaction m " +
           "WHERE m.paymentType = :paymentType AND m.reversals = :reversals")
    BigDecimal getGrandTotalReversals(@Param("paymentType") String paymentType,
                                     @Param("reversals") String reversals);

    @Query("SELECT COALESCE(SUM(m.sentAmount), 0) FROM FinWealthPaymentTransaction m " +
           "WHERE m.paymentType = :paymentType AND m.reversals = :reversals " +
           "AND FUNCTION('month', m.lastModifiedDate) = FUNCTION('month', CURRENT_DATE)")
    BigDecimal getTotalCurrentMonthReversal(@Param("paymentType") String paymentType,
                                           @Param("reversals") String reversals);

    Page<FinWealthPaymentTransaction> findByWalletNoOrderByCreatedDateDesc(String walletNo, Pageable pageable);

    boolean existsByTransactionId(String transactionId);

    Optional<FinWealthPaymentTransaction> findFirstByWalletNoOrderByCreatedDateDesc(String walletNo);

    List<FinWealthPaymentTransaction> findByTransactionId(String transactionId);
}

