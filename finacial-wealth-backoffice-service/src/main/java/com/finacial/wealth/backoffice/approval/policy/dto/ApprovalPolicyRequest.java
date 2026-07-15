package com.finacial.wealth.backoffice.approval.policy.dto;

import java.math.BigDecimal;

public record ApprovalPolicyRequest(
        Boolean approvalRequired,
        BigDecimal thresholdAmount,
        String thresholdCurrency,
        String checkerPermission,
        Integer slaHours,
        Boolean active
) {
}
