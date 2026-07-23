package com.finacial.wealth.backoffice.auth.service;

import java.util.List;

public final class DefaultPermissionCatalog {

    private DefaultPermissionCatalog() {
    }

    public static List<PermissionSpec> all() {
        return List.of(
                new PermissionSpec("TRANSACTIONS", "TRANSACTIONS", "VIEW", "transactions.view", "View transactions"),
                new PermissionSpec("TRANSACTIONS", "TRANSACTIONS", "FILTER", "transactions.filter", "Filter transactions"),
                new PermissionSpec("TRANSACTIONS", "TRANSACTIONS", "VIEW_DETAILS", "transactions.viewDetails", "View transaction details"),
                new PermissionSpec("TRANSACTIONS", "FEES", "VIEW", "transactions.viewFees", "View transaction fees"),
                new PermissionSpec("TRANSACTIONS", "CUTOFF", "VIEW_PRE", "transactions.viewPreCutoff", "View pre-cutoff transactions"),
                new PermissionSpec("TRANSACTIONS", "CUTOFF", "VIEW_POST", "transactions.viewPostCutoff", "View post-cutoff transactions"),
                new PermissionSpec("TRANSACTIONS", "PLACEMENT", "PLACE", "transactions.place", "Place transactions"),
                new PermissionSpec("TRANSACTIONS", "PLACEMENT", "CONFIRM", "transactions.confirmPlacement", "Confirm transaction placement"),
                new PermissionSpec("TRANSACTIONS", "STATUS", "UPDATE", "transactions.updateStatus", "Update transaction status"),
                new PermissionSpec("TRANSACTIONS", "STATUS", "BULK_UPDATE", "transactions.bulkUpdate", "Bulk update transactions"),
                new PermissionSpec("TRANSACTIONS", "ROLLOVER", "ROLLOVER", "transactions.rollover", "Rollover transactions"),
                new PermissionSpec("PRODUCTS", "PRODUCTS", "VIEW", "products.view", "View products"),
                new PermissionSpec("PRODUCTS", "PRODUCTS", "MANAGE", "products.manage", "Manage products"),
                new PermissionSpec("USER", "MANAGEMENT", "VIEW", "userManagement.view", "View backoffice users"),
                new PermissionSpec("USER", "MANAGEMENT", "MANAGE", "userManagement.manage", "Manage backoffice users"),
                new PermissionSpec("USER", "MANAGEMENT", "ASSIGN_ROLES", "userManagement.assignRoles", "Assign backoffice roles"),
                new PermissionSpec("USER", "MANAGEMENT", "EDIT_SUPER_ADMIN", "userManagement.editSuperAdmin", "Edit super admin users"),
                new PermissionSpec("REPORTS", "REPORTS", "VIEW", "reports.view", "View reports"),
                new PermissionSpec("REPORTS", "REPORTS", "EXPORT", "reports.export", "Export reports"),
                new PermissionSpec("SETTINGS", "SETTINGS", "MANAGE", "settings.manage", "Manage settings"),
                new PermissionSpec("AUDIT", "LOG", "EXPORT", "audit.export", "Export audit logs"),
                new PermissionSpec("INVESTMENT", "LIQUIDATION", "VIEW", "investment.liquidation.view", "View liquidation approval queues"),
                new PermissionSpec("INVESTMENT", "LIQUIDATION", "APPROVE", "investment.liquidation.approve", "Approve or reject liquidation approvals"),
                new PermissionSpec("INVESTMENT", "LIQUIDATION", "REMEDIATE", "investment.liquidation.remediate", "Remediate and resubmit liquidation approvals"),
                new PermissionSpec("INVESTMENT", "ORDER", "VIEW", "investment.order.view", "View investment and top-up order queues"),
                new PermissionSpec("INVESTMENT", "PRODUCT", "VIEW", "investment.product.view", "View investment product configuration"),
                new PermissionSpec("INVESTMENT", "PRODUCT", "MANAGE", "investment.product.manage", "Create and update investment product configuration"),
                new PermissionSpec("INVESTMENT", "PRODUCT", "APPROVE", "investment.product.approve", "Approve investment product configuration changes"),
                new PermissionSpec("CUSTOMER", "PROFILE", "VIEW", "customer.profile.view", "View customer profile and investment summaries"),
                new PermissionSpec("CUSTOMER", "PROFILE", "MANAGE", "customer.profile.manage", "Block and unblock customer profiles"),
                new PermissionSpec("APPROVAL", "INBOX", "VIEW", "approval.inbox.view", "View approval inbox"),
                new PermissionSpec("APPROVAL", "POLICY", "VIEW", "approval.policy.view", "View maker-checker approval policies"),
                new PermissionSpec("APPROVAL", "POLICY", "MANAGE", "approval.policy.manage", "Manage maker-checker approval policies"),
                new PermissionSpec("REVERSAL", "EXCEPTION", "VIEW", "reversal.exception.view", "View reversal exception queues"),
                new PermissionSpec("REVERSAL", "MANUAL", "REQUEST", "reversal.manual.request", "Create manual reversal approval requests"),
                new PermissionSpec("REVERSAL", "MANUAL", "APPROVE", "reversal.manual.approve", "Approve or reject manual reversal requests"),
                new PermissionSpec("REVERSAL", "MANUAL", "REMEDIATE", "reversal.manual.remediate", "Remediate and resubmit manual reversal requests"),
                new PermissionSpec("NOTIFICATION", "INBOX", "VIEW", "notification.view", "View and mark backoffice notifications"),
                new PermissionSpec("CONFIG", "APP_CONFIG", "VIEW", "app_config.view", "View governed app_config values"),
                new PermissionSpec("CONFIG", "APP_CONFIG", "MANAGE", "app_config.manage", "Register and update governed app_config values"),
                new PermissionSpec("REFERRAL", "PROGRAM", "VIEW", "referral.program.view", "View referral programs and referral audit"),
                new PermissionSpec("REFERRAL", "PROGRAM", "MANAGE", "referral.program.manage", "Create and manage referral programs"),
                new PermissionSpec("CAMPAIGN", "MARKETING", "VIEW", "campaign.view", "View campaigns and campaign audit"),
                new PermissionSpec("CAMPAIGN", "MARKETING", "MANAGE", "campaign.manage", "Create and manage campaigns"),
                new PermissionSpec("CAMPAIGN", "MARKETING", "APPROVE", "campaign.approve", "Approve campaigns"),
                new PermissionSpec("ROLE", "MANAGEMENT", "MANAGE", "role.manage", "Create roles and manage permissions"),
                new PermissionSpec("USER", "MANAGEMENT", "MANAGE", "user.manage", "Create and manage backoffice users"),
                new PermissionSpec("AUDIT", "LOG", "VIEW", "audit.view", "View audit logs"),
                new PermissionSpec("APPROVAL", "DECISION", "MANAGE", "approval.decision.manage", "Approve or reject approval requests"),
                new PermissionSpec("INTERBANK", "NAME_ENQUIRY", "EXECUTE", "interbank.nameEnquiry.execute", "Execute interbank name enquiry")
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
