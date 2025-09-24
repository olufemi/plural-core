/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.repo;

import com.financial.wealth.api.transactions.domain.FinWealthPaymentTransaction;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 *
 * @author olufemioshin
 */
public interface FinWealthPaymentTransactionRepo extends
        CrudRepository<FinWealthPaymentTransaction, String> {

    @Query("SELECT o FROM FinWealthPaymentTransaction o where o.sender = :walletNo OR o.receiver = :walletNo")
    List<FinWealthPaymentTransaction> findByWalletNoList(@Param("walletNo") String walletNo);

    @Query("select bs from FinWealthPaymentTransaction bs where bs.walletNo=:walletNo")
    FinWealthPaymentTransaction findByWalletNoDe(String walletNo);

    Optional<FinWealthPaymentTransaction> findByWalletNo(String walletNo);

    @Query("SELECT o FROM FinWealthPaymentTransaction o where o.paymentType = :paymentType order by o.id desc")
    List<FinWealthPaymentTransaction> findByWalletNoListPage(Pageable pageable, String paymentType);

    @Query("select bs from FinWealthPaymentTransaction bs where bs.transactionId=:transactionId")
    List<FinWealthPaymentTransaction> findByTransationidNoDe(String transactionId);

    @Query("select bs from FinWealthPaymentTransaction bs where bs.transactionId=:transactionId")
    FinWealthPaymentTransaction findByTransationId(String transactionId);

    @Query("SELECT SUM(m.sentAmount) FROM FinWealthPaymentTransaction m where m.paymentType = :paymentType and m.reversals =:reversals")
    List<FinWealthPaymentTransaction> getGrandTotalReversals(String paymentType, String reversals);

    @Query("SELECT SUM(m.sentAmount) FROM FinWealthPaymentTransaction m where m.paymentType = :paymentType and m.reversals =:reversals and month(m.lastModifiedDate) = month(current_date)")
    List<FinWealthPaymentTransaction> getTotalCurrentMonthReversal(String paymentType, String reversals);

}
