package com.finacial.wealth.backoffice.approval.repo;

import com.finacial.wealth.backoffice.approval.entity.BoApprovalEvent;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoApprovalEventRepository extends JpaRepository<BoApprovalEvent, Long> {

    List<BoApprovalEvent> findByApprovalRequestIdOrderByCreatedAtDesc(Long approvalRequestId);
}
