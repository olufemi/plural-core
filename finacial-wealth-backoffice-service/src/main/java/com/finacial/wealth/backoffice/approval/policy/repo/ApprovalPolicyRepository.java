package com.finacial.wealth.backoffice.approval.policy.repo;

import com.finacial.wealth.backoffice.approval.policy.entity.ApprovalPolicy;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalPolicyRepository extends JpaRepository<ApprovalPolicy, String> {
    List<ApprovalPolicy> findByActiveTrueOrderByModuleAscSubModuleAscActionCodeAsc();
}
