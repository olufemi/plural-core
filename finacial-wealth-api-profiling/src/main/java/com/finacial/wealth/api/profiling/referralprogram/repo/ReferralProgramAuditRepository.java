package com.finacial.wealth.api.profiling.referralprogram.repo;

import com.finacial.wealth.api.profiling.referralprogram.entity.ReferralProgramAudit;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReferralProgramAuditRepository extends JpaRepository<ReferralProgramAudit, Long> {
    List<ReferralProgramAudit> findByProgramIdOrderByEventAtAsc(Long programId);
}
