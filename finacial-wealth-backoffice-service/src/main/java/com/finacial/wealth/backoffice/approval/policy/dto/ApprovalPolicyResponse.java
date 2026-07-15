package com.finacial.wealth.backoffice.approval.policy.dto;

import com.finacial.wealth.backoffice.approval.policy.entity.ApprovalPolicy;
import java.math.BigDecimal;
import java.time.Instant;

public record ApprovalPolicyResponse(
        String actionCode,
        String module,
        String subModule,
        String description,
        boolean approvalRequired,
        BigDecimal thresholdAmount,
        String thresholdCurrency,
        String checkerPermission,
        Integer slaHours,
        boolean active,
        Long updatedByAdminId,
        Instant createdAt,
        Instant updatedAt
) {
    public static ApprovalPolicyResponse from(ApprovalPolicy policy) {
        return new ApprovalPolicyResponse(
                policy.getActionCode(),
                policy.getModule(),
                policy.getSubModule(),
                policy.getDescription(),
                policy.isApprovalRequired(),
                policy.getThresholdAmount(),
                policy.getThresholdCurrency(),
                policy.getCheckerPermission(),
                policy.getSlaHours(),
                policy.isActive(),
                policy.getUpdatedByAdminId(),
                policy.getCreatedAt(),
                policy.getUpdatedAt()
        );
    }
}
