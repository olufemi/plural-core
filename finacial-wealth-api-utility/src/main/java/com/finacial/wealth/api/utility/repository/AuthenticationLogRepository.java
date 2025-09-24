package com.finacial.wealth.api.utility.repository;

import com.finacial.wealth.api.utility.domains.SessionServiceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;



public interface AuthenticationLogRepository extends JpaRepository<SessionServiceLog, Long>, JpaSpecificationExecutor<SessionServiceLog> {

}
