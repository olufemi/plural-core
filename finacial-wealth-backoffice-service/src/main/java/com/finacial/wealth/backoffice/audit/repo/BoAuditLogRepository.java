package com.finacial.wealth.backoffice.audit.repo;

import com.finacial.wealth.backoffice.audit.entity.BoAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoAuditLogRepository extends JpaRepository<BoAuditLog, Long> {}
