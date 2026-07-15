# Backoffice Requirements Implementation Tracker

Date: 2026-07-15

## How To Use This Tracker

The DOCX remains the official business requirement. This file is the working engineering tracker that maps the DOCX sections to the current backoffice backend implementation.

Source documents:

- Official requirement: `/Users/olufemioshin/Downloads/Plural Backoffice Admin Portal Requirements.docx`
- Searchable mirror: `/Users/olufemioshin/Downloads/Plural_Backoffice_Admin_Portal_Requirements.md`

Implementation reviewed:

- `/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-backoffice-service`

Status meanings:

- `Implemented`: backend capability exists and broadly matches the requirement.
- `Partial`: useful backend foundation exists, but some requirement coverage is missing.
- `Not Implemented`: no meaningful backend support found in backoffice service.
- `External/Unclear`: capability may exist in another service, but backoffice orchestration/visibility is not confirmed.

## Executive Check

Today’s implementation is acceptable as a lean pilot foundation, not as the full DOCX requirement set.

The strongest implemented areas are admin auth, RBAC foundation, audit foundation, approval inbox, investment operations, customer lookup, referral management, reversal exception workflow, notifications inbox, and guarded `app_config` management.

The biggest gaps before a full production/global backoffice are complete maker-checker coverage, route-by-route permission hardening, finance/treasury, compliance/risk, full P2P FX operations, full VAS/Sochitel operations, full customer 360, full group savings lifecycle, and production-grade reporting.

## Requirement Matrix

| DOCX Section / Page | Requirement Area | Current Status | Current Evidence | What Is OK Today | Remaining Gap / Next Action |
| --- | --- | --- | --- | --- | --- |
| 1.3 page 2, 1.4 page 2 | Backoffice purpose and scope across Group Savings, Investment, P2P FX, VAS, Wallet, Compliance, Finance, Customer Support | Partial | Multiple controllers exist for investment, group savings, customer, referral, reversals, notifications, app config | Backoffice has a real operational backend foundation | Coverage is uneven across modules; production scope needs phased delivery |
| 2 page 4 | Personas: Super Admin, Ops, Product Manager, Finance, Support, Maker, Checker | Partial | `AdminUserController`, `AdminRoleController`, `AdminPermissionController`, roles and permissions tables | Admin user and role management exists | Need final pilot/prod role matrix, persona-to-permission mapping, country/currency scopes |
| 3.1 page 4 | RBAC-01 custom roles and granular permissions | Partial | `BoPermission`, `BoAdminRole`, `bo_role_permission`, `DefaultPermissionCatalog` | Permission model exists and some new permissions are seeded | Need broader permission catalog for every module/action |
| 3.1 page 4 | RBAC-02 multiple roles union | Partial | Role/permission service and JWT permission claim support | Foundation exists | Confirm effective permission merge behavior with test coverage |
| 3.1 page 4 | RBAC-03 segregation of duties | Partial | Approval flow prevents maker/checker style workflow in specific paths | Foundation exists for approval workflow | Need configurable same-user prevention and SoD matrix for all sensitive actions |
| 3.1 page 4 | RBAC-04 country/currency/business-unit scoping | Not Implemented | No strong scoped permission model observed | Not needed for narrow pilot if operations team is small | Required for global rollout |
| 3.1 page 4 | RBAC-05 temporary elevation | Not Implemented | No temporary role expiry model observed | Can defer for pilot if access is tightly controlled | Add timed emergency access and audit |
| 3.1 page 4 | RBAC-06 server-side enforcement | Partial | Many controllers use `@PreAuthorize`; examples include approvals, app config, notifications, investment, group savings | Server-side enforcement is present | Some controllers still use broad roles or no visible method-level permissions; route audit required |
| 3.1 page 4 | RBAC-07 approval rights by module/submodule | Partial | Approval permissions exist for liquidation, reversals, product config, app_config, referral/campaign changes, and customer block/unblock | Good pilot foundation for selected workflows | Extend to rates, wallet/treasury, VAS, finance, and admin-user high-risk changes |
| 3.1 page 4 | RBAC-08 admin account create/update/activate/reset | Implemented / Partial | `AdminUserController` supports create, update, activate, suspend, password reset | Core admin management exists | Confirm FE coverage, audit detail, and maker-checker policy for high-privilege admin changes |
| 3.2 page 5 | MC-01 configurable maker-checker per action/threshold | Partial | `ApprovalService`, `BoApprovalRequest`, `BoApprovalEvent`, `bo_approval_policy` | Approval engine and policy registry exist for current high-risk pilot actions | Need threshold enforcement and broader coverage across wallet/treasury, VAS, finance, admin-user high-risk actions |
| 3.2 page 5 | MC-02 pending queue | Implemented / Partial | `ApprovalController` list/detail endpoints | Queue exists for current approval types | Broaden eligible checker filtering by module/action and priority/SLA |
| 3.2 page 5 | MC-03 approve/reject/request more information | Partial | approve/reject/resubmit endpoints | Approve, reject, remediation/resubmit exist | Add explicit request-more-information status if business wants that separate from remediation |
| 3.2 page 5 | MC-05 history retention/export | Partial | Approval event table exists | History exists per approval | Need retention policy, export, and complete report coverage |
| 3.2 page 5 | MC-06 checker notifications | Partial | Notification service called from approval service | In-app notification foundation exists | Add push/email/Slack adapters only where required; FE needs notification UI |
| 3.3 page 5 | AUTH-01 mandatory MFA | Partial | `MfaController`, login MFA verify flow | MFA capability exists | Confirm enforced for every admin, no bypass in prod, recovery process controlled |
| 3.3 page 5 | AUTH-03 configurable session timeout | Partial / Unclear | Auth refresh/logout exists | Basic session flow exists | Confirm idle timeout and high-privilege shorter timeout in config/code |
| 3.3 page 5 | AUTH-04 view-only impersonation | Not Implemented | No customer impersonation flow found | Can defer if compliance prefers no impersonation | Decide allowed/prohibited; if allowed, implement reason capture and audit |
| 3.4 page 6 | AUD-01 audit every sensitive/config action with actor, role, timestamp, IP/device, before/after | Partial | `AuditAspect`, `AdminAuditLog`, `BoAuditLog`, app config history | Audit foundation and app_config change history exist | Verify before/after snapshots on every high-risk endpoint |
| 3.4 page 6 | AUD-02 tamper-evident audit | Not Implemented | No hash-chain/write-once storage observed | Not a pilot blocker if logs are retained securely | Add hash chain or write-once strategy before regulated production |
| 3.4 page 6 | AUD-03/AUD-04 audit search/export/retention | Partial | Audit controller exists | Basic audit access exists | Add export, filters, retention policy, and immutable archive |
| 3.5 page 6 | Dashboard and global search | Not Implemented / Minimal | Investment dashboard exists; no global dashboard/search | Investment-only dashboard can support narrow pilot | Add global operations dashboard and cross-module search |
| 3.6 page 6 | Reporting analytics framework | Partial | `ReportController`, `CsvWriter`, investment CSV export | Basic CSV/export path exists | Build report catalog, filters, scheduled reports, XLSX/PDF where needed |
| 3.7 page 7 | Admin profile/self-service | Partial | password change/recovery, MFA setup | Basic self-service security exists | Add admin profile, notification preferences, login activity, trusted devices |
| 4 pages 7-9 | Group Savings lifecycle, membership, contribution, payout, default, dispute, reporting | Partial | `BoGroupSavingsController` group list/detail/close/delete, monitoring, slot tracking | Visibility and selected actions exist | Full lifecycle, vetting, payout override, disputes, risk/defaults, reports are missing |
| 5.1 page 10 | INV-01 to INV-12 investment product configuration | Partial | `BoInvestmentController` product list/detail/create/update, `InvestmentProductUpsertRequest` | Product admin exists and recent mandatory field validation was improved | Need maker-checker, rate/yield history, deletion/deactivation rules, full validation coverage |
| 5.2 page 12 | Investment customer/account management | Partial | customer investment summary/orders/liquidations/positions via profiling controller | Useful customer-investment views exist | Need full investment account controls and richer freeze/unfreeze/account-status workflow beyond profile block/unblock |
| 5.3 page 13 | Rate, yield, unit price management | Partial / External | Product fields and performance endpoint exist | Some rate/product values visible | Need rate/NAV scheduling, approval, history, and reconciliation |
| 5.4 page 13 | Orders, subscriptions, top-up, cutoff, T+1 | Partial | investment orders endpoint and product `subscriptionCutOffTime` support | Order visibility exists | Need daily cutoff batches, after-cutoff roll-forward, idempotency visibility, failed debit retry view |
| 5.5 page 15 | Portfolio, balances, accrual | Partial | positions/performance/dashboard endpoints | Some visibility exists | Need accrual run view, capitalization history, failed/skipped run exception handling |
| 5.6 page 16 | Maturity and rollover | External/Unclear | No complete backoffice maturity workflow observed | Can defer if pilot products do not mature immediately | Add maturity forecast, auto rollover/mature actions, audit |
| 5.7 page 17 | Liquidation workflow | Partial / Strong Pilot Foundation | liquidation list/history, approve, deny, approval workflow | Liquidation approval path exists | Need full capital-balance validation evidence, fee/penalty governance, high-value secondary approval |
| 5.8 page 18 | Investment risk controls | Partial / Unclear | oversight/dashboard endpoints exist | Some risk visibility may exist downstream | Need concentration limits, exposure rules, alerts |
| 5.9 page 18 | Investment dashboard analytics | Partial | `/backoffice/investments/dashboard`, `/performance`, `/oversight` | Useful pilot dashboard backend exists | Confirm KPI completeness against FE/business |
| 5.10 page 19 | Investment reports | Partial | product CSV export and reports sample | Basic export exists | Build full investment reporting suite |
| 6 pages 19-20 | P2P FX listing, escrow, lifecycle, fraud, reporting | Minimal | `BoFxPeerOfferController` update endpoint only | Only very narrow offer update support | Build listing dashboard, fulfilled trades, escrow monitor, dispute, AML flags, reports |
| 7 pages 21-22 | Global VAS / Sochitel order management, retry, refund, reconciliation, reporting | Minimal | airtime reversal summary/list through `BoFxPeerServicesController` | Some reversal visibility exists | Add provider catalog, order monitor, retry/refund/reconciliation, Sochitel logs, VAS reports |
| 8 page 22 | Finance and Treasury: wallet ledger, fee/revenue, settlement | Minimal | reversal exception flow, interbank reversal views, transaction client | Some exception handling exists | Add wallet ledger, GL/posting visibility, settlement, reconciliation, treasury reports |
| 9 pages 23-25 | Customer directory, search, 360, wallet/transaction visibility, enrollment, support actions | Partial | `ProfilingManagementController` customer list/detail, investment views, block/unblock with optional maker-checker policy | Customer lookup and selected governed actions exist | Add full KYC docs/status, wallets/accounts, devices, timeline, service enrollments, support notes |
| 10 pages 25-27 | Referral program management | Partial | `ReferralProgramManagementController` create/update/activate/pause/end/list/detail/audit/active | Good admin API foundation exists | Add maker-checker for campaign changes, reward/payout tracking, fraud controls, reports |
| 11 page 23 | TECH-01 integration reliability / API layer standards | Partial | Feign clients and request id filter exist | Integration wrappers exist | Add consistent retry/timeouts/circuit breakers/error normalization |
| 11 page 23 | TECH-03 provider abstraction via configuration | Partial | app_config governance, provider clients | Configuration governance started | Need provider-specific editable key registry and environment promotion process |
| 11 page 23 | TECH-05 environment separation | Partial | app properties and go-live checklist mention env separation | Config separation exists in files | Validate secrets, app_config rows, DB URLs, provider credentials per environment |
| 12 pages 24-25 | Reporting suite summary | Partial / Not Implemented | CSV foundations only | Basic exports exist | Build report backlog by priority: investment, customer/KYC, VAS, FX, finance, audit |
| 15 page 25 | Phased rollout and MVP | Partial | Current implementation covers parts of Phase 1 foundation and investment | Pilot can proceed with a clearly limited scope | Align pilot scope explicitly and mark non-pilot items as production/later |
| 1.3 page 2, 2 page 4, 3.4 page 6, 11 page 23, 15 page 25 | App config/data management | Partial | `AppConfigManagementController`, registry, validation, masking, history, rollback endpoint, V4 migration | This is a good safety layer for heavily used `app_config` table | Add environment tags, approved editable-key inventory, and startup validation for release-critical keys |
| 1.5 page 2, 3.5 page 6 | Notifications | Partial | `BackofficeNotificationController`, `BackofficeNotificationService`, V3 migration | In-app admin alerts foundation exists | FE inbox/badge needed; Firebase/email/Slack adapters optional but useful for urgent alerts |

## Pilot Recommendation

For pilot, focus the backoffice scope on:

- Admin authentication, MFA, roles, permissions
- Approval inbox for liquidation and manual reversal flows
- Investment product/list/detail/order/liquidation views
- Customer lookup and customer investment views
- Referral program management
- Reversal exception handling
- Notifications inbox
- Guarded `app_config` management
- Basic reports/export

Treat these as production blockers before full global launch:

- Complete route-level permission audit
- Maker-checker policy matrix and broader enforcement
- Tamper-evident audit or durable audit archive
- Finance/treasury module
- Compliance/risk module
- Full P2P FX operations
- Full VAS/Sochitel operations
- Full customer 360
- Full reporting suite
- `app_config` rollback and maker-checker

## Immediate Engineering Plan

1. Finish route audit: every controller method must have module, permission, audit event, approval requirement, and FE screen owner.
   - 2026-07-15 progress: added method-level permissions for referral program routes, marketing campaign routes, and admin-user read routes.
2. Replace broad role checks on sensitive endpoints with granular permission codes.
   - 2026-07-15 progress: added `referral.program.view`, `referral.program.manage`, `campaign.view`, `campaign.manage`, and `campaign.approve` permissions with Flyway seed migration.
3. Add maker-checker policy table/config and enforce it first on product config, referral campaign changes, customer block/unblock, `app_config`, reversals, and liquidations.
   - 2026-07-15 progress: added approval policy registry/API and seeded high-risk action policies. Existing reversal and liquidation policies are marked required; product/referral/campaign/customer/app_config policies are seeded as disabled to avoid breaking pilot until approval-submit flows are enabled and tested.
   - 2026-07-15 progress: wired `APP_CONFIG_UPDATE`, `INVESTMENT_PRODUCT_CREATE`, and `INVESTMENT_PRODUCT_UPDATE` to the approval engine. With policy disabled, existing direct update behavior remains. With policy enabled, the endpoint returns `202 Accepted`, creates an approval inbox item, and only applies the change when a checker approves it.
   - 2026-07-15 progress: wired `REFERRAL_PROGRAM_CHANGE` and `CAMPAIGN_CHANGE` to the approval engine for create/update/status actions. Policies remain disabled by default for pilot compatibility and can be enabled by SQL/script when the approval inbox flow is ready.
   - 2026-07-15 progress: wired `CUSTOMER_BLOCK_UNBLOCK` to the approval engine for customer profile block/unblock. Added `customer.profile.manage` permission and a Flyway migration for existing environments.
   - 2026-07-15 progress: added optional go-live script `BACKOFFICE_APPROVAL_POLICY_ENABLE_PROD.sql` to enable maker-checker for the currently wired product, app_config, referral, campaign, and customer block/unblock policies outside Flyway.
4. Add `app_config` rollback/apply-previous endpoint and environment tags.
   - 2026-07-15 progress: added `POST /backoffice/app-config/{configName}/history/{historyId}/rollback`. It applies the previous value from history directly when policy is disabled, or submits the rollback as an `APP_CONFIG_UPDATE` approval when policy is enabled. Sensitive masked history cannot be rolled back because the old secret value is not stored.
5. Harden investment product create/update validation and make FE error responses deterministic.
6. Expand customer 360 with KYC/BVN/face status, wallets/accounts, devices, and service enrollment views.
7. Decide whether VAS and P2P FX are pilot or production-later; if pilot, build operational dashboards before go-live.
