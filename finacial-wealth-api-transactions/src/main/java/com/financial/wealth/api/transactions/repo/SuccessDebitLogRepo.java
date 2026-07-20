/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.financial.wealth.api.transactions.repo;

import com.financial.wealth.api.transactions.domain.CreateQuoteResLog;
import com.financial.wealth.api.transactions.domain.FailedCreditLog;
import com.financial.wealth.api.transactions.domain.FailedDebitLog;
import com.financial.wealth.api.transactions.domain.SuccessDebitLog;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author olufemioshin
 */
public interface SuccessDebitLogRepo extends
        CrudRepository<SuccessDebitLog, String> {

    List<SuccessDebitLog> findByResolvedFalse();
    
    List<SuccessDebitLog> findByMarkForRollBack(int markForRollBack);

    List<SuccessDebitLog> findByReversalStatus(String reversalStatus);

    List<SuccessDebitLog> findByReversalStatusIn(Collection<String> reversalStatuses);

    List<SuccessDebitLog> findByReversalStatusNot(String reversalStatus);

    SuccessDebitLog findFirstByTransactionId(String transactionId);

    @Query("SELECT config from SuccessDebitLog config where config.transactionId=:transactionId")
    List<SuccessDebitLog> findByTransactionId(String transactionId);

    @Query("SELECT config from SuccessDebitLog config where config.transactionId=:transactionId")
    SuccessDebitLog findByTransactionIdUpdate(String transactionId);

    @Transactional
    @Modifying
    @Query("UPDATE SuccessDebitLog config SET config.reversalStatus = 'PROCESSING', "
            + "config.processingClaimedAt = :claimedAt, "
            + "config.processingClaimedBy = :claimedBy, "
            + "config.lastModifiedDate = :claimedAt "
            + "WHERE config.id = :id AND config.reversalStatus IN :statuses")
    int claimForReversalProcessing(@Param("id") Long id,
            @Param("claimedAt") java.time.Instant claimedAt,
            @Param("claimedBy") String claimedBy,
            @Param("statuses") Collection<String> statuses);

    @Transactional
    @Modifying
    @Query("UPDATE SuccessDebitLog config SET config.reversalStatus = 'FAILED', "
            + "config.processingClaimedAt = null, "
            + "config.processingClaimedBy = null, "
            + "config.reversalLastError = :errorMessage, "
            + "config.lastModifiedDate = :releasedAt "
            + "WHERE config.reversalStatus = 'PROCESSING' "
            + "AND config.processingClaimedAt IS NOT NULL "
            + "AND config.processingClaimedAt < :cutoff")
    int releaseStaleProcessingClaims(@Param("cutoff") java.time.Instant cutoff,
            @Param("releasedAt") java.time.Instant releasedAt,
            @Param("errorMessage") String errorMessage);

}
