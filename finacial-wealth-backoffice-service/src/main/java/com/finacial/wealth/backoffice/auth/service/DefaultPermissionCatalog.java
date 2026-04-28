package com.finacial.wealth.backoffice.auth.service;

import java.util.List;

public final class DefaultPermissionCatalog {

    private DefaultPermissionCatalog() {
    }

    public static List<PermissionSpec> all() {
        return List.of(
                new PermissionSpec("INVESTMENT", "LIQUIDATION", "VIEW", "investment.liquidation.view", "View liquidation approval queues"),
                new PermissionSpec("INVESTMENT", "LIQUIDATION", "APPROVE", "investment.liquidation.approve", "Approve or reject liquidation approvals"),
                new PermissionSpec("INVESTMENT", "LIQUIDATION", "REMEDIATE", "investment.liquidation.remediate", "Remediate and resubmit liquidation approvals"),
                new PermissionSpec("INVESTMENT", "ORDER", "VIEW", "investment.order.view", "View investment and top-up order queues"),
                new PermissionSpec("CUSTOMER", "PROFILE", "VIEW", "customer.profile.view", "View customer profile and investment summaries"),
                new PermissionSpec("APPROVAL", "INBOX", "VIEW", "approval.inbox.view", "View approval inbox"),
                new PermissionSpec("REVERSAL", "EXCEPTION", "VIEW", "reversal.exception.view", "View reversal exception queues"),
                new PermissionSpec("REVERSAL", "MANUAL", "REQUEST", "reversal.manual.request", "Create manual reversal approval requests"),
                new PermissionSpec("REVERSAL", "MANUAL", "APPROVE", "reversal.manual.approve", "Approve or reject manual reversal requests"),
                new PermissionSpec("REVERSAL", "MANUAL", "REMEDIATE", "reversal.manual.remediate", "Remediate and resubmit manual reversal requests"),
                new PermissionSpec("ROLE", "MANAGEMENT", "MANAGE", "role.manage", "Create roles and manage permissions"),
                new PermissionSpec("USER", "MANAGEMENT", "MANAGE", "user.manage", "Create and manage backoffice users"),
                new PermissionSpec("AUDIT", "LOG", "VIEW", "audit.view", "View audit logs")
        );
    }

    public record PermissionSpec(
            String module,
            String subModule,
            String action,
            String code,
            String description
    ) {
    }
}
