package com.finacial.wealth.api.fxpeer.exchange.inter.airtime.security;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface AirtimeRollbackLogRepository extends JpaRepository<AirtimeRollbackLog, Long> {

    AirtimeRollbackLog findFirstByProcessIdAndLegKey(String processId, String legKey);

    List<AirtimeRollbackLog> findByProcessIdOrderByIdAsc(String processId);

    List<AirtimeRollbackLog> findByStatusIn(Collection<String> statuses);

    @Transactional
    @Modifying
    @Query("UPDATE AirtimeRollbackLog logItem SET logItem.status = 'PROCESSING', "
            + "logItem.processingClaimedAt = :claimedAt, "
            + "logItem.processingClaimedBy = :claimedBy, "
            + "logItem.lastModifiedDate = :claimedAt "
            + "WHERE logItem.id = :id AND logItem.status IN :statuses")
    int claimForProcessing(@Param("id") Long id,
            @Param("claimedAt") java.time.Instant claimedAt,
            @Param("claimedBy") String claimedBy,
            @Param("statuses") Collection<String> statuses);

    @Transactional
    @Modifying
    @Query("UPDATE AirtimeRollbackLog logItem SET logItem.status = 'FAILED', "
            + "logItem.processingClaimedAt = null, "
            + "logItem.processingClaimedBy = null, "
            + "logItem.lastError = :errorMessage, "
            + "logItem.lastModifiedDate = :releasedAt "
            + "WHERE logItem.status = 'PROCESSING' "
            + "AND logItem.processingClaimedAt IS NOT NULL "
            + "AND logItem.processingClaimedAt < :cutoff")
    int releaseStaleProcessingClaims(@Param("cutoff") java.time.Instant cutoff,
            @Param("releasedAt") java.time.Instant releasedAt,
            @Param("errorMessage") String errorMessage);
}
