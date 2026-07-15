# Plural Backoffice Admin Portal - Requirements Gap Analysis

Date: 2026-07-15

## Scope

This review compares the requirements in:

- `/Users/olufemioshin/Downloads/Plural_Backoffice_Admin_Portal_Requirements.md`
- `/Users/olufemioshin/Downloads/Plural Backoffice Admin Portal Requirements.docx`

against the current implementation under:

- `/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-backoffice-service`

The focus is the current backoffice service API/backend coverage, not the FE screens.

For the day-to-day build checklist, use:

- `BACKOFFICE_REQUIREMENTS_IMPLEMENTATION_TRACKER.md`

That tracker keeps the DOCX page/section mapping, implementation status, evidence, and next action per requirement area.

## Executive Summary

The backoffice service already has a useful operational foundation: admin authentication, MFA support, role/permission management, audit logging, maker-checker approval workflow, investment admin endpoints, referral program endpoints, customer profile lookup, group savings monitoring, storage upload endpoints, reversal exception handling, and some reporting/export support.

However, the requirement document describes a much broader enterprise backoffice portal. The largest gaps are module depth and operational coverage: global dashboard/search, finance/treasury, compliance/risk tooling, full group savings lifecycle, full P2P FX operations, full Sochitel/VAS operations, broader customer 360 support, stronger reporting, and consistent maker-checker/RBAC/audit enforcement across all sensitive actions.

For pilot, the current implementation can support a lean operations portal if scope is intentionally limited. For production/global rollout, we should treat this as partially implemented and plan the remaining modules in phases.

## Current Implementation Evidence

Key implemented areas observed in the service:

- Auth/session/MFA: `auth/controller/AuthController.java`, `auth/controller/MfaController.java`, `auth/service/BackofficeAuthService.java`
- RBAC/permissions: `auth/controller/AdminRoleController.java`, `auth/controller/AdminPermissionController.java`, `auth/service/AdminRolePermissionService.java`, `auth/service/DefaultPermissionCatalog.java`
- Admin user management: `auth/controller/AdminUserController.java`
- Audit: `auth/controller/AuditController.java`, `audit/AuditAspect.java`, `audit/entity/AdminAuditLog.java`, `auth/service/AdminAuditService.java`
- Maker-checker: `approval/controller/ApprovalController.java`, `approval/service/ApprovalService.java`, `approval/entity/BoApprovalRequest.java`
- Investment admin: `controller/BoInvestmentController.java`, `integrations/fxpeer/FxPeerExchangeClient.java`
- Group savings admin: `controller/BoGroupSavingsController.java`
- Customer/profile admin: `controller/ProfilingManagementController.java`, `integrations/profiling/ProfilingClient.java`
- Referral program admin: `controller/ReferralProgramManagementController.java`, `integrations/profiling/ReferralProgramManagementClient.java`
- VAS/airtime reversal support: `controller/BoFxPeerServicesController.java`
- Reversal exception support: `controller/BoReversalController.java`, `reversal/service/ReversalExceptionService.java`
- Reporting sample/export: `reports/ReportController.java`, `reports/CsvWriter.java`
- Storage upload: `controller/BackofficeStorageController.java`

## Gap Analysis By Requirement Area

| Requirement Area | Current Implementation | Status | Missing / Improvement Needed |
| --- | --- | --- | --- |
| RBAC and permissions | Permission catalog, roles, permissions, JWT permission claims, role controllers, some `@PreAuthorize` usage. | Partial | Need full route coverage audit, country/currency/department scoping, temporary elevation, delegated access, and consistent action-level permission codes for every sensitive endpoint. |
| Maker-checker | Approval inbox with approve/reject/resubmit. Supports liquidation, reversals, investment product create/update, app_config update, referral/campaign changes, and customer block/unblock when policies are enabled. | Partial | Need threshold rules and enforced coverage for rate changes, wallet/treasury actions, VAS refunds/retries, user/admin management, and all high-risk operations. |
| Authentication and session security | Login, MFA verify/setup, refresh, logout, password change/recovery. | Partial | Need confirm MFA policy for all admins, idle timeout/high-privilege timeout enforcement, password policy, device/session list, forced logout, IP allowlist, and admin lockout/risk rules. |
| Audit trail | Audit controller and audit logging support exist. | Partial | Need ensure all business endpoints write full before/after snapshots, actor, IP, user-agent, request id, approval id, downstream response, export, retention policy, and tamper-resistant storage. |
| Dashboard and global search | No broad global dashboard/search implementation found. Investment-specific dashboard exists. | Not implemented / minimal | Need cross-module dashboard, operational KPIs, global customer/order/product/search index, saved filters, and alert widgets. |
| Reporting framework | Sample CSV endpoint and investment product CSV export exist. | Partial | Need standardized report builder, filters, CSV/XLSX/PDF exports, scheduled reports, finance reports, audit reports, VAS reports, FX reports, referral reports, and permissioned downloads. |
| Admin profile/self-service | Password change/recovery and MFA setup exist. | Partial | Need admin profile details, notification preferences, login activity, trusted devices, profile update controls, and self-service MFA reset policy. |
| Group Savings Admin | Group list/detail, close group, delete, contribution/payout monitoring, slot assignment tracking. | Partial | Missing group creation/editing, rules management, pause/resume, member vetting/removal, contribution enforcement, payout overrides, dispute management, default/risk monitoring, group reports, and maker-checker for sensitive actions. |
| Investment Admin | Product list/detail/history, create/update product, featured services config, liquidation approve/deny, orders, liquidations, history, performance, dashboard, oversight, CSV export. | Strong partial | Direct product detail API is now in place. Need validation hardening, maker-checker for product/rate changes, before/after config audit, rate/yield calendar, maturity/rollover controls, custodian settlement, NAV/unit pricing history, risk limits, portfolio exposure views, bulk exports, and reconciliation reports. |
| P2P FX Exchange Admin | Offer update endpoint exists. | Minimal | Missing trade lifecycle dashboard, order book/offer listing, rate spread controls, escrow monitoring, dispute handling, fraud/AML checks, trade cancellation/refund flows, settlement/reconciliation, corridor controls, and FX reports. |
| Global VAS / Sochitel Admin | Airtime reversal summary/cases and retry client methods exist. | Minimal | Missing provider/catalog/product management, Sochitel transaction monitoring, request/response logs, retries/refunds, reconciliation, price/margin config, provider status, SLA/error dashboards, and full VAS reporting. |
| Finance and Treasury | Reversal exception cases and manual reversal approval path exist. | Minimal | Missing wallet ledger views, GL posting visibility, settlement files, bank/custodian reconciliation, fee/revenue reporting, treasury balances, payout controls, chargeback/refund center, and financial close reports. |
| Customer Management | Customer list/detail, `/backoffice/profiling/{id}/customer-360`, investment summaries/orders/liquidations/positions, block/unblock with optional maker-checker policy. Customer 360 now includes profile, KYC/BVN status summary, primary wallet, additional currency accounts, linked device summary, referral context, user access status, basic activity timeline, and investment sections with graceful downstream fallback. | Strong partial | Still missing wallet ledger/balance source, full transaction history, support notes, communication log, risk flags, service enrollments, full KYC document archive, view-only impersonation, and re-verification workflows. |
| Referral Program Management | Create/update/activate/pause/end/list/detail/audit/active endpoints exist. | Partial | Need verify route-level permissions are complete, add maker-checker for campaign changes, qualification/payout tracking, fraud controls, attribution history, reward ledger, export/reporting, and campaign performance dashboard. |
| Technical integrations | Feign clients to FxPeer, Profiling, Transactions. | Partial | Need integration health endpoints, timeout/retry/circuit breaker standards, downstream error normalization, correlation IDs, observability, and complete clients for SmartCore, Sochitel, notifications, email/SMS, KYC/identity, and finance/ledger sources. |
| Notifications | Backoffice DB notification inbox now exists with unread count, list, mark-read, and mark-all-read endpoints. Approval events, manual reversal approval requests, selected downstream integration failures, and CSV report readiness now create notifications. | Partial | FE still needs inbox/badge/read-state UI. Firebase/web-push/email/Slack are not yet wired as delivery channels. Need alerts from source services for settlement failures, VAS provider failures, SmartCore failures, scheduled-report readiness, and rate-limited integration health events. |
| Compliance / AML / Risk | Not found as a dedicated module. | Not implemented | Need suspicious activity flags, high-risk customer/trade monitoring, audit investigation views, sanctions/PEP/KYC status where applicable, case management, and escalation workflow. |
| App config/data management | Guarded `app_config` governance baseline now exists: list/detail, explicit key registry, editable/sensitive flags, value-type validation, regex validation, audit, masked sensitive values, change history, rollback/apply-previous endpoint, permissions, optional maker-checker, and admin notifications on update. | Partial | FE still needs management screens. Need environment tagging, startup validation for release-critical keys, and final whitelist of editable pilot/prod keys. |

## Key Risks Before Pilot

1. Sensitive actions are not uniformly protected by maker-checker.
2. Permission coverage appears uneven across controllers; this needs a route-by-route audit.
3. Some modules rely on downstream service wrappers but do not yet expose the full operational workflow expected by the requirement document.
4. Finance/treasury, VAS, P2P FX, and compliance are too thin for full production operations.
5. Reporting is not yet a complete business reporting suite.
6. Admin audit exists, but we still need to confirm every high-risk endpoint logs enough business context.
7. The `app_config` table is heavily used across the platform; direct edits are now guarded by registry, validation, history, and audit, but maker-checker and rollback still need to be added before broad production use.

## Recommended Implementation Plan

### Phase 0 - Scope Confirmation and Route Audit

Target: 2-3 days.

- Confirm the pilot MVP modules: likely Investment, Customer Management, Referral, Reversal Exceptions, and selected Group Savings visibility.
- Produce route inventory for every backoffice endpoint with: module, permission, maker-checker requirement, audit event, downstream service, and FE screen owner.
- Confirm which service owns each required domain: Profiling, FxPeer, Transactions, SmartCore, Identity, Notification, Sochitel, email/SMS.
- Confirm which requirements are for pilot versus production.

### Phase 1 - Control Plane Hardening

Target: 1-2 weeks.

- Complete permission catalog and `@PreAuthorize` coverage across all controllers.
- Add a maker-checker policy matrix: action code, module, threshold, required checker role, same-user prevention, remediation rules.
- Ensure all sensitive operations create approval requests instead of executing immediately.
- Standardize audit events with request id, actor, before/after snapshot, downstream response, IP, user-agent, approval id, and correlation id.
- Add admin session policy: MFA required, idle timeout, refresh-token revocation, lockout rules, and password policy.

### Phase 2 - Pilot Operations MVP

Target: 2-3 weeks.

- Investment: product create/update validation, product detail API, liquidation workflow, order/liquidation search, performance dashboard, CSV export.
- Customer: FE should consume `/backoffice/profiling/{id}/customer-360` for the pilot 360 screen. It currently returns profile, KYC/BVN status summary, primary wallet, additional accounts, devices, referral, user access, basic timeline, and investment exposure. Block/unblock now has optional maker-checker policy support. Remaining pilot/prod gap is transaction ledger/balance, support notes, communication log, risk flags, service enrollments, and richer KYC archive.
- Referral: permission hardening, campaign maker-checker, reward/payout tracking, campaign audit/export.
- Reversal exceptions: complete manual request/approval/remediation workflow and reporting.
- Notifications: FE should consume `/backoffice/notifications`, `/backoffice/notifications/unread-count`, `/backoffice/notifications/{id}/read`, and `/backoffice/notifications/read-all`; BE remains responsible for generating notification events. Firebase/email/Slack should be treated as optional delivery adapters, not the source of truth.
- App config: FE should consume `/backoffice/app-config`, `/backoffice/app-config/{configName}`, `/backoffice/app-config/{configName}/registry`, `/backoffice/app-config/{configName}`, `/backoffice/app-config/{configName}/history`, and `/backoffice/app-config/{configName}/history/{historyId}/rollback`; BE now supports whitelist/registry, validation, audit, history, rollback, sensitive value masking, and optional maker-checker. Next layer is environment tags and approved editable-key inventory.

### Phase 3 - Money Movement, Finance, and Compliance

Target: 2-3 weeks.

- Wallet/ledger search and transaction drill-down.
- Settlement, reconciliation, GL/posting visibility, and finance reports.
- Refund/reversal governance with clear status lifecycle.
- Compliance/risk dashboard: suspicious activity, high-value operations, blocked customers, failed verification/KYC cases.
- Exportable finance and audit reports.

### Phase 4 - Product Modules Expansion

Target: 3-5 weeks.

- Group Savings: full lifecycle, members, contributions, payouts, disputes, defaults, reports.
- P2P FX: trade lifecycle, escrow, rates, spreads, dispute, AML flags, settlement, reporting.
- VAS/Sochitel: provider/product catalog, transaction monitor, retry/refund, reconciliation, pricing/margins, provider health.
- SmartCore operational views where backoffice needs account/core-banking visibility.

### Phase 5 - Production Readiness

Target: 1-2 weeks after module completion.

- Full UAT with FE, BE, Ops, Finance, Compliance.
- Access review for every role/persona.
- Seed data and `app_config` verification.
- Integration endpoint and credential verification for SmartCore, Sochitel, Identity, email/SMS, SendGrid, notification, payment/core services.
- Observability: logs, metrics, alerts, dashboards, trace/correlation IDs.
- Runbook: deployment, rollback, incident response, manual reconciliation, approval recovery, failed downstream compensation.

## Clarifications Needed

Please confirm these before implementation planning is locked:

1. Which modules must be included in pilot: Investment, Referral, Customer Management, Reversals, Group Savings, P2P FX, VAS, Finance/Treasury?
2. Should every create/update/delete/admin action go through maker-checker, or only high-risk actions?
3. What are the exact admin roles/personas for pilot and production?
4. Which service is the source of truth for wallet ledger, GL/posting, settlement, and treasury data?
5. Is Sochitel already integrated in FxPeer/another service, or should backoffice include first-class Sochitel integration work?
6. Do we want backoffice to manage `app_config` directly, and if yes, which keys are allowed to be edited?
7. What reports are mandatory for day-one pilot versus production?
8. Should customer support have view-only impersonation, or is that prohibited for compliance?
9. Are country/currency scopes required from day one for global rollout?
10. Should backoffice include SmartCore account/core-banking operational views in the same portal MVP?

## Suggested Immediate Next Actions

1. Approve pilot scope and mark each requirement as Pilot, Production, or Later.
2. Run a security/permission route audit on current backoffice endpoints.
3. Add missing permission annotations and maker-checker coverage for existing sensitive endpoints.
4. Complete safe `app_config` management by adding maker-checker, rollback, environment tagging, and the approved editable-key whitelist for pilot/prod.
5. Prioritize the missing customer 360, investment hardening, reversal governance, and referral audit/reporting work for pilot.
