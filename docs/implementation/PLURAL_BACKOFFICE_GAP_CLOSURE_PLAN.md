# Plural Backoffice Gap Closure Plan

Date: 2026-08-11

Source gap register:

- `Plural_Backoffice_Implementation_Gaps_43_Red_3_Amber.md`

Reviewed local codebase:

- Backend/API: `finacial-wealth-backoffice-service`
- Shared backend tracker: `BACKOFFICE_REQUIREMENTS_IMPLEMENTATION_TRACKER.md`
- FE source: not present in this local workspace. No `package.json`, React/Vite/Next config, route file, API client, or component tree was found under `/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core`.

Because the Backoffice FE source is not available locally, this plan records backend/API evidence and defines the frontend work/contracts that can be implemented once the FE repository is supplied. Do not mark an item "frontend implemented" until the FE project is inspected.

## Status Legend

- Implemented: backend/API capability exists and can support FE implementation.
- Partially implemented: useful backend/API support exists, but the full requirement is not covered.
- Not implemented: no meaningful backend/API support was found.
- Frontend complete/API pending: reserved for the FE repo after UI/routes/state/mocks are built but backend is not ready. None can be truthfully marked this way from this workspace because the FE source is absent.

## Evidence References

- Auth/MFA: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/auth/controller/AuthController.java`, `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/auth/controller/MfaController.java`
- Roles/users/permissions: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/auth/controller/AdminUserController.java`, `AdminRoleController.java`, `AdminPermissionController.java`
- Approvals: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/approval/controller/ApprovalController.java`, `approval/service/ApprovalService.java`
- Investments: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/BoInvestmentController.java`
- Group savings: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/BoGroupSavingsController.java`
- Customer 360/profile: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/ProfilingManagementController.java`
- Notifications: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/notification/controller/BackofficeNotificationController.java`
- Reports: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/reports/ReportController.java`
- Global dashboard: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/dashboard/BackofficeDashboardController.java`, `dashboard/BackofficeDashboardService.java`
- Global search/saved views: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/search/BackofficeGlobalSearchController.java`, `savedview/controller/BoSavedViewController.java`
- App config governance: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/configmanagement/controller/AppConfigManagementController.java`
- API handoff: `finacial-wealth-backoffice-service/BACKOFFICE_API_REFERENCE.md`

## Requirement Matrix

| ID | Current status | Evidence | Missing frontend work | Backend/API dependency | Batch | Acceptance criteria |
| --- | --- | --- | --- | --- | --- | --- |
| AUTH-01 | Implemented backend / FE unknown | Auth and MFA controllers expose login, MFA verify/setup/confirm, session policy; `bo.security.mfa-required` controls mandatory MFA. | Login step handling for `MFA_SETUP_REQUIRED` and `MFA_REQUIRED`, MFA setup screen, recovery UX, session expiry warning. | Prod must set `BO_MFA_REQUIRED=true` after super-admin enrollment runbook. | B1 | Admin cannot access protected screens without valid auth/MFA when required; FE shows actionable MFA states. |
| GS-01 | Implemented backend / FE unknown | `BoGroupSavingsController` exposes list/detail endpoints on `/backoffice/group-savings` and `/bo/backoffice/group-savings`; `GroupSavingsAdminService.listGroups` supports status/search/page/size and normalized row fields. | Group savings list page with search/filter by status, currency, contribution size, creator; detail navigation. | None for visibility MVP. Currency/contribution-size filters can be added later if FE requires backend-side filtering beyond the current response fields. | B2 | Ops can search/filter groups and open detail without raw API tooling. |
| GS-02 | Implemented backend / FE unknown | `GET /bo/backoffice/group-savings/groups/{groupId}` returns group detail, members, payout policy, contribution frequency, and cycles. | Group configuration detail panels: contribution, frequency, currency, payout model, members, schedule. | None for visibility MVP. | B2 | Detail screen shows complete config or explicit unavailable fields. |
| GS-03 | Not implemented | No group template/limit config controller found. | Platform constraints config UI and validation can be mocked. | New backend endpoints for group templates/limits with maker-checker. Proposed: `GET/PUT /bo/backoffice/group-savings/config`. | B7 | Admin can view/edit constraints through approval flow once API exists. |
| GS-04 | Partially implemented | Close/delete endpoints exist; pause/flag not confirmed. | Action drawer with reason capture for pause/force-close/flag. | Add pause/flag endpoints and approval type. Proposed: `POST /bo/backoffice/group-savings/groups/{id}/pause`, `/flag`, `/force-close`. | B7 | Sensitive group action requires reason and creates approval. |
| GS-05 | Not implemented | No group timeline endpoint found. | Timeline tab component. | Add timeline endpoint. Proposed: `GET /bo/backoffice/group-savings/groups/{id}/timeline`. | B7 | State changes render actor, reason, timestamp, approval id. |
| GS-9 | Implemented backend / FE unknown | Contribution/payout monitoring, slot assignment tracking, group cycle health, single cycle health, and retry-failed endpoints exist; cycle health includes contribution rows, payout state, `schedulerEligible`, `healthStatus`, and `recommendedAction`. | Contribution schedule table with paid/unpaid status, monitoring charts, alerts, slot schedule, payout history. | None for visibility MVP. Dedicated `/contribution-schedule` can be added only if FE needs a different read model from cycle health. | B2 | Per-member schedule status visible with empty/loading/error states. |
| GS-10 | Not implemented | No payout override endpoint found. | Payout override workflow with reason and checker state. | Add scheduled payout/override endpoints. | B7 | Override cannot execute directly without maker-checker. |
| GS-11 | Not implemented | No payout hold endpoint found. | Payout hold action and status badge. | Add hold/release API. | B7 | Hold records compliance/dispute reason and actor. |
| GS-14 | Not implemented | No risk score endpoint found. | Risk score column and member profile widget. | Add cross-group risk endpoint from transactions/group savings source. | B8 | Late/missed contribution score visible and filterable. |
| GS-15 | Not implemented | No late fee rules endpoint found. | Late fee rules config form. | Add rules endpoint with maker-checker. | B8 | Fee rules support flat/percentage/tiered validation. |
| GS-16 | Not implemented | No repeated defaulter restriction endpoint found. | Restrictions panel and unblock/request workflow. | Add defaulter restrictions API. | B8 | Repeated defaulters can be viewed and restricted with audit. |
| GS-17 | Not implemented | No reserve/insurance dashboard endpoint found. | Reserve dashboard cards/table. | Add reserve fund balance/utilization/payout API. | B8 | Dashboard totals reconcile with payout history. |
| GS-18 | Not implemented | No dispute queue endpoint found. | Dispute intake queue page. | Add dispute case APIs. | B8 | Ops can list/assign dispute cases. |
| GS-19 | Not implemented | No dispute evidence/resolution endpoint found. | Evidence upload/view and resolution action UI. | Add attachment/resolution APIs. | B8 | Evidence and resolution action are persisted/audited. |
| GS-20 | Not implemented | No dispute SLA endpoint found. | SLA config and breach indicators. | Add SLA config/escalation APIs. | B8 | SLA breaches are visible and escalated. |
| INV-05 | Implemented backend / FE unknown | Product create/update accepts `maximumTotalRaise` and `autoCloseAtCapacity` as product governance metadata returned in product responses. | Product form fields for maximum total raise and capacity progress. | Subscription-time hard auto-close still needs production data validation if capacity progress source differs from product metadata. | B3 | Product capacity fields are visible/updateable without schema migration. |
| INV-08 | Implemented backend / FE unknown | Redemption app config governance exists; product create/update now accepts `liquidationFrequencyLimit` and `liquidationFrequencyPeriod`. | Product/config UI for liquidation frequency limit. | Runtime enforcement should use these fields when liquidation rule finalization is enabled. | B3 | Limit is configurable through product maker-checker flow. |
| INV-13 | Implemented backend / FE unknown | Customer 360/profile endpoints expose profile, orders, liquidations, and positions; investment operations list supports product/status/date filters. | Investment account search/filter page. | Add richer search only if FE needs a single cross-investment account endpoint after testing. | B3 | FE can build customer investment account/history views from available endpoints. |
| INV-16 | Implemented | Backoffice exposes customer/product investment freeze; FxPeer enforces on top-up and redemption using app_config flags. | None for backend; FE should add freeze/unfreeze action. | `POST /bo/backoffice/investments/customers/{email}/investment-freeze`. | B4 | Frozen customer cannot top-up/redeem investments; action writes controlled app_config flag. |
| INV-18 | Implemented backend / FE unknown | Customer orders/liquidations/positions endpoints are exposed through Backoffice customer 360 and FxPeer admin customer routes. | Customer investment profile history tabs. | None for pilot if current status fields satisfy FE. | B3 | Customer profile can show subscription, top-up, liquidation, and holding lifecycle rows. |
| INV-20 | Implemented backend / FE unknown | Product history endpoint returns current product, lifecycle events, and maker-checker approval history where available. | Rate/unit price history table/chart. | Dedicated NAV/rate time-series endpoint may be added later if Product wants chart-level rate history beyond audit/current state. | B3 | Product history remains available after closure. |
| INV-21 | Implemented backend / FE unknown | `GET /bo/backoffice/investments/orders` exists. | Orders table with filters/status/date/customer/product. | None for pilot if current fields are enough. | B1 | FE can list subscriptions/top-ups with accurate timestamps. |
| INV-24 | Not implemented | No after-cutoff rollover management endpoint found. | Cutoff batch view and rollover action state can be designed. | Backend batch rollover scheduler/API. | B5 | After-cutoff requests move to next business day batch. |
| INV-25 | Not implemented | No non-business-day rollover endpoint found. | Holiday/weekend calendar view. | Business calendar service/config and rollover API. | B5 | Friday/non-business day requests roll correctly. |
| INV-26 | Partially implemented | Dashboard/orders exist; before/after cutoff totals not confirmed. | Daily cutoff totals widget/table. | Add aggregate endpoint by day/product/currency/cutoff bucket. | B5 | Counts and values by cutoff bucket are displayed. |
| INV-27 | Partially implemented | Approval and order endpoints exist; bulk status update not confirmed. | Multi-select table actions. | Add individual/bulk update endpoint with maker-checker where sensitive. | B5 | Status updates are audited and permissioned. |
| INV-29 | Not implemented | No auto top-up visibility endpoint found. | Auto top-up monitoring screen. | Add scheduled auto top-up/retry endpoint. | B6 | Failed auto debit and retry count visible. |
| INV-31 | Implemented | Liquidation and position rows expose gross, reserved, available, and reserveStatus fields. | FE should display reserved/available amounts in liquidation and customer position screens. | Existing liquidation/position list endpoints. | B4 | Held funds are visible immediately and released/debited on outcome. |
| INV-33 | Implemented | Liquidation rows expose `correctionMode=REVERSAL_ONLY` for settled items; unified reversal maker-checker remains the correction path. | FE should route settled corrections to reversal workflow and hide direct edit controls. | Existing `/bo/backoffice/reversals` endpoints. | B4 | Completed transaction corrections only through reversal links. |
| INV-34 | Implemented | Liquidation rows expose notificationStatus and failureReason; FxPeer emits request/completed/cancelled redemption notifications. | FE should display notificationStatus/failureReason on liquidation detail. | Delivery-log endpoint remains optional future enhancement. | B4 | Customer receives request/completed/cancelled notification; ops can see status. |
| INV-37 | Partially implemented | Customer 360/investment summary endpoints exist. | Total investment panel with currency breakdown and holding rows. | Ensure API returns gross/reserved/available amounts per currency. | B1 | FE uses `availableInvestmentAmount` for post-redemption available value. |
| INV-40 | Not implemented | No accrual run endpoint found. | Accrual run list/detail page can be scaffolded with mocks. | Add accrual run endpoint. Proposed: `GET /bo/backoffice/investments/accrual-runs`. | B6 | Runs show date, product, holdings affected, total accrued. |
| INV-44 | Not implemented | No statement generation endpoint found. | Statement request form and download state. | Add statement endpoint. Proposed: `POST /bo/backoffice/investments/customers/{id}/statement`. | B6 | Statement downloads for date range. |
| INV-45 | Not implemented | No maturity calendar endpoint found. | Maturity calendar/list page. | Add maturity calendar API. | B6 | Treasury can see upcoming maturities by product/date/amount. |
| INV-46 | Implemented backend / FE unknown | Product create/update now accepts and returns `maturityDefaultAction` with allowed values `REDEEM_TO_WALLET`, `ROLLOVER_PRINCIPAL`, and `ROLLOVER_PRINCIPAL_AND_INTEREST`. | Product form toggle and badges. | Runtime maturity action execution must be confirmed when maturity scheduler batch is implemented. | B3 | Product default maturity behavior is visible and updateable. |
| INV-47 | Partially implemented | Liquidation workflow endpoints exist. | Dedicated liquidation queue/detail workflow. | Confirm all early-exit paths route through liquidation APIs. | B1 | Early exits appear in queue and require configured approval. |
| LIQ-11 | Implemented | Backoffice retry endpoint proxies FxPeer retry that only accepts FAILED/LIQUIDATION_FAILED, locks records, and re-reserves funds. | FE should show retry action only when `retryEligible=true`. | `POST /bo/backoffice/investments/liquidations/{orderRef}/retry`. | B4 | Admin can retry only failed liquidation transactions. |
| LIQ-13 | Partially implemented | Product minimum fields exist; enforcement not confirmed in backoffice. | Show minimum remaining balance in liquidation decision. | Backend must reject invalid partial liquidation. | B4 | Partial liquidation below minimum is blocked with 400. |
| LIQ-14 | Partially implemented | Same dependency as INV-08. | Show frequency usage/countdown in liquidation queue. | Backend enforcement. | B4 | Frequency limit prevents excess liquidation requests. |
| Exposure Caps Requirement | Not implemented | No exposure cap endpoint found. | Exposure cap config screen can be designed. | Add exposure cap model/endpoints with maker-checker. | B6 | Per-user/product and issuer/platform caps enforced. |
| FIN-02 | Partially implemented | App config governance and some product fee fields exist; no consolidated fee schedule module. | Fee schedule admin pages. | Add finance fee schedule endpoints. | B9 | Fees configurable by service/country/currency/tier. |
| FIN-03 | Not implemented | Reports exist, but no consolidated revenue dashboard found. | Revenue dashboard page. | Add revenue aggregate API across modules. | B9 | Revenue visible by module/country/currency. |
| DASH-01 | Implemented backend / FE unknown | `BackofficeDashboardController` exposes `/bo/backoffice/dashboard`; service composes approvals, notifications, admin users, customers, investment performance, NGN balance summary, and integration health with per-section availability. | Role-sensitive landing dashboard. | None for pilot dashboard MVP; future finance/dispute widgets need their own APIs. | B1 | Landing dashboard shows pending approvals, notifications, customer/admin counts, NGN balance summary, investment summary, and dependency health without failing the whole page when one downstream section is unavailable. |
| DASH-02 | Implemented backend / FE unknown | `BackofficeGlobalSearchController` exposes `/bo/backoffice/search`. | Global search bar, result grouping, keyboard navigation, deep links. | Extend ranking/types as needed. | B1 | Search resolves user, wallet, group, trade/order/transaction references where backend supports source. |
| INV-02 | Implemented backend / FE unknown | Backoffice and FxPeer product upsert request now support and validate product identity/governance fields: `description`, `issuerName`, `fundManager`, `riskRating`, `minimumHoldingDays`, and `maturityDefaultAction` on create. | Product create/edit form completeness and validation. | None for create validation; existing product remediation can be completed gradually through update flow. | B3 | Required product identity fields are validated client and server side for new products. |

## Dependency-Aware Batches

Each batch contains no more than five related requirements.

### B1 - FE integration of already available pilot APIs

Requirements: AUTH-01, DASH-01, DASH-02, INV-21, INV-37

Why first: these are high-value and mostly API-ready. They improve the real FE experience without waiting for large new backend domains.

Frontend work:

- Auth/MFA state machine for login, setup, verify, recovery, token refresh, session expiry warning.
- Role-sensitive dashboard shell using available investment, approvals, notifications and transactions endpoints.
- Global search bar using `GET /bo/backoffice/search`.
- Investment orders table using `GET /bo/backoffice/investments/orders`.
- Customer investment summary that displays gross, reserved redemption, and available investment values clearly.

Acceptance:

- User can log in with required MFA.
- Dashboard and orders page show loading/error/empty states.
- Global search opens correct deep links.
- Investment cards do not mislead customers/admins when redemption amount is reserved.

### B2 - Group Savings visibility MVP

Requirements: GS-01, GS-02, GS-9

Frontend work:

- Group savings list/detail route.
- Contribution/payout monitoring views.
- Cycle health and per-member status tables.

Backend dependencies:

- Backend route and response contract are now available through Backoffice.
- Add a dedicated normalized contribution schedule endpoint only if FE finds cycle-health too detailed for its table model.

### B3 - Investment product/admin hardening

Requirements: INV-02, INV-05, INV-08, INV-13, INV-18, INV-20, INV-46

Frontend work:

- Complete product form fields and validation.
- Product history/rate history display.
- Customer investment account search.
- Liquidation limit/capacity fields in config screens.

Backend dependencies:

- Product governance create validation is now implemented in Backoffice before maker-checker submission and in FxPeer before persistence.
- Capacity/frequency/maturity defaults are now accepted and returned as typed product fields while stored under `metaJson.productGovernance` to avoid a deployment migration.
- Runtime capacity enforcement is now implemented in FxPeer: subscriptions are rejected once `maximumTotalRaise` would be exceeded, with `autoCloseAtCapacity` reflected in the rejection message.
- Runtime liquidation governance is now implemented in FxPeer: `minimumHoldingDays`, `liquidationFrequencyLimit`, and `liquidationFrequencyPeriod` are enforced before redemption is queued.
- `maturityDefaultAction` is accepted, validated, stored, and returned; automatic maturity execution remains a separate scheduler automation if the business wants it.

### B4 - Liquidation/reversal safety controls

Requirements: INV-16, INV-31, INV-33, INV-34, LIQ-11

Status: Implemented in Backoffice + FxPeer. No database migration required; customer/product freeze uses controlled `app_config` keys. Compile passed for both services.

Frontend work:

- Freeze/unfreeze request UI.
- Held/reserved wallet/investment display.
- Failed/reversed notification status.
- Retry failed liquidation UX with reason.

Backend dependencies:

- Add investment-specific freeze.
- Add failed liquidation retry endpoint.
- Confirm wallet hold/settlement source of truth.

### B5 - Cutoff and operations batching

Requirements: INV-24, INV-25, INV-26, INV-27

Frontend work:

- Cutoff dashboard.
- Before/after cutoff totals.
- Business day rollover review screen.
- Bulk status selection/action UI.

Backend dependencies:

- Batch rollover APIs.
- Business calendar configuration.
- Bulk status update endpoint with maker-checker.

### B6 - Treasury-facing investment planning

Requirements: INV-29, INV-40, INV-44, INV-45, Exposure Caps Requirement

Frontend work:

- Auto top-up monitor.
- Accrual run list/detail.
- Statement generation/download.
- Maturity calendar.
- Exposure cap dashboard/config UI.

Backend dependencies:

- New auto top-up, accrual, statement, maturity and exposure APIs.

### B7 - Group Savings controls and governance

Requirements: GS-03, GS-04, GS-05, GS-10, GS-11

Frontend work:

- Group constraints config UI.
- Pause/flag/force-close/payout hold/override workflows.
- Timeline component.

Backend dependencies:

- New group governance endpoints and maker-checker action types.

### B8 - Group Savings risk/disputes

Requirements: GS-14, GS-15, GS-16, GS-17, GS-18

Frontend work:

- Risk score view.
- Late fee config.
- Defaulter restriction list/actions.
- Reserve fund dashboard.
- Dispute queue.

Backend dependencies:

- Group risk, fee, restriction, reserve and dispute APIs.

### B9 - Finance and revenue

Requirements: FIN-02, FIN-03, GS-19, GS-20

Frontend work:

- Fee schedule screens.
- Revenue dashboard.
- Dispute evidence/resolution/SLA screens.

Backend dependencies:

- Finance fee/revenue APIs.
- Dispute evidence and SLA APIs.

## Proposed Missing API Contracts

### Global Dashboard

`GET /bo/backoffice/dashboard`

Response:

```json
{
  "status": 200,
  "description": "Backoffice dashboard retrieved successfully",
  "generatedAt": "2026-08-11T10:00:00Z",
  "data": {
    "range": "TODAY",
    "summaryCards": [
      {"key": "pendingApprovals", "label": "Pending approvals", "value": 12, "valueType": "count", "deepLink": "/approvals", "status": "AVAILABLE"},
      {"key": "unreadNotifications", "label": "Unread notifications", "value": 3, "valueType": "count", "deepLink": "/notifications", "status": "AVAILABLE"},
      {"key": "ngnCumulativeBalance", "label": "NGN cumulative balance", "value": 12500000, "valueType": "amount", "deepLink": "/operations/transactions", "status": "AVAILABLE"}
    ],
    "sections": {
      "approvals": {"status": "AVAILABLE", "data": {"pendingCount": 12}},
      "notifications": {"status": "AVAILABLE", "data": {"unreadCount": 3}},
      "integrationHealth": {"status": "AVAILABLE", "data": {"status": "UP", "dependencies": []}}
    },
    "quickLinks": [
      {"label": "Approvals", "path": "/approvals"},
      {"label": "NGN Transactions", "path": "/operations/transactions"}
    ]
  }
}
```

### Group Savings Config

`GET /bo/backoffice/group-savings/config`

`PUT /bo/backoffice/group-savings/config`

Request:

```json
{
  "maximumGroupSize": 20,
  "maximumContributionByTier": {"TIER_1": 50000, "TIER_2": 250000},
  "allowedFrequencies": ["WEEKLY", "MONTHLY"],
  "reason": "Pilot constraints update"
}
```

### Investment Accrual Runs

`GET /bo/backoffice/investments/accrual-runs?productCode=&from=&to=&page=0&size=20`

Response:

```json
{
  "status": 200,
  "data": {
    "content": [
      {
        "runId": "ACC-20260811-001",
        "productCode": "MF_BAL001",
        "runDate": "2026-08-11",
        "holdingsAffected": 120,
        "totalAccruedAmount": 840000.50,
        "currency": "NGN",
        "status": "COMPLETED"
      }
    ]
  }
}
```

### Investment Statement

`POST /bo/backoffice/investments/customers/{customerReference}/statement`

Request:

```json
{
  "fromDate": "2026-07-01",
  "toDate": "2026-07-31",
  "currency": "NGN",
  "format": "PDF"
}
```

Response:

```json
{
  "status": 202,
  "data": {
    "jobId": "RPT-123",
    "status": "QUEUED",
    "downloadUrl": null
  }
}
```

### Finance Revenue Dashboard

`GET /bo/backoffice/finance/revenue?from=&to=&country=&currency=`

Response:

```json
{
  "status": 200,
  "data": {
    "totalRevenue": 1250000,
    "currency": "NGN",
    "byModule": [
      {"module": "INVESTMENT", "amount": 700000},
      {"module": "VAS", "amount": 300000},
      {"module": "P2P_FX", "amount": 250000}
    ]
  }
}
```

## Recommended Batch 1

Start with B1.

Reason:

- It is the safest front-end-first batch.
- It uses APIs already present in the backoffice service.
- It directly addresses the issues MacAnthony has been seeing: auth/MFA/session behavior, global navigation/search, orders visibility, and investment values after redemption.
- It avoids pretending FE can implement enforcement-only requirements like wallet holds, maker-checker enforcement, accruals or immutable transactions without backend support.

## Blocking Questions

1. Where is the Backoffice FE repository? The local Plural backend tree does not contain a frontend project.
2. Should the FE consume only `/bo/...` public gateway paths, or also support stripped local service paths such as `/backoffice/...` for local dev?
3. Which pilot roles must see the first dashboard: `SUPER_ADMIN`, `EXTERNAL_COMPLIANCE`, `OPERATIONS`, `FINANCE`, `PRODUCT_MANAGER`?
4. Should Group Savings be pilot scope or production-later?
5. For requirements that need new backend enforcement, should we implement backend APIs before FE screens or allow FE mocks behind feature flags?
