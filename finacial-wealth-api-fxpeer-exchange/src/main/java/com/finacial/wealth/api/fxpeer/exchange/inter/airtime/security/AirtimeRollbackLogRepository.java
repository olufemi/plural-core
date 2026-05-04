package com.finacial.wealth.api.fxpeer.exchange.inter.airtime.security;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AirtimeRollbackLogRepository extends JpaRepository<AirtimeRollbackLog, Long> {

    AirtimeRollbackLog findFirstByProcessIdAndLegKey(String processId, String legKey);

    List<AirtimeRollbackLog> findByProcessIdOrderByIdAsc(String processId);

    List<AirtimeRollbackLog> findByStatusIn(Collection<String> statuses);

    List<AirtimeRollbackLog> findByStatus(String status);
}
