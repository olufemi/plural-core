package com.finacial.wealth.backoffice.approval.repo;

import com.finacial.wealth.backoffice.approval.entity.ApprovalModule;
import com.finacial.wealth.backoffice.approval.entity.ApprovalStatus;
import com.finacial.wealth.backoffice.approval.entity.ApprovalSubModule;
import com.finacial.wealth.backoffice.approval.entity.BoApprovalRequest;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoApprovalRequestRepository extends JpaRepository<BoApprovalRequest, Long> {

    Optional<BoApprovalRequest> findByEntityTypeAndEntityRef(
            com.finacial.wealth.backoffice.approval.entity.ApprovalEntityType entityType,
            String entityRef
    );

    Page<BoApprovalRequest> findByModuleAndSubModuleAndStatusIn(
            ApprovalModule module,
            ApprovalSubModule subModule,
            Collection<ApprovalStatus> statuses,
            Pageable pageable
    );

    List<BoApprovalRequest> findByStatusIn(Collection<ApprovalStatus> statuses);
}
