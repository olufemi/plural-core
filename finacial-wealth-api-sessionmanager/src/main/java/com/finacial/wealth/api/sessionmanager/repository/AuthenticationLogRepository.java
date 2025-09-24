package com.finacial.wealth.api.sessionmanager.repository;


import com.finacial.wealth.api.sessionmanager.entities.SessionServiceLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;



public interface AuthenticationLogRepository extends JpaRepository<SessionServiceLog, Long>, JpaSpecificationExecutor<SessionServiceLog> {

}
