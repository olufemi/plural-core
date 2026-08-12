# Plural Backoffice FE API Reference

Updated: 2026-08-11  
Service: `finacial-wealth-backoffice-service`  
Audience: Backoffice frontend developers

This is the shareable frontend API handoff for the Plural backoffice service. It is source-derived from the current backoffice controllers and includes the newer approval, investment product, customer 360, notification, app config, reversal, referral, campaign, and storage endpoints.

## Calling Convention

Use the gateway path in deployed environments:

```http
https://finacialwealth.com/bo/...
```

Most service-local controllers expose `/backoffice/...`. Through the gateway, use:

```http
/bo/backoffice/...
```

Some authentication/admin endpoints also expose shorter aliases such as `/bo/auth/...`, `/bo/admin-users`, `/bo/admin/roles`.

All protected endpoints require:

```http
Authorization: Bearer <backoffice_access_token>
Content-Type: application/json
```

For multipart upload:

```http
Authorization: Bearer <backoffice_access_token>
Content-Type: multipart/form-data
```

## Response Patterns

Backoffice responses are not fully uniform yet because some endpoints proxy other services. FE should be tolerant of these shapes:

```json
{
  "statusCode": 200,
  "description": "OK",
  "data": {}
}
```

```json
{
  "data": {},
  "description": "Successful",
  "statusCode": 200
}
```

```json
{
  "content": [],
  "pageable": {},
  "totalElements": 0
}
```

Maker-checker submissions return HTTP `202` and a pending approval payload:

```json
{
  "statusCode": 202,
  "description": "Pending approval",
  "data": {
    "approvalId": 123,
    "status": "PENDING",
    "actionCode": "INVESTMENT_PRODUCT_UPDATE",
    "entityRef": "MF_BAL001"
  }
}
```

## Maker-Checker Behaviour

The following actions may return `202 Pending approval` instead of applying immediately:

| Action | Policy code |
| --- | --- |
| Create investment product | `INVESTMENT_PRODUCT_CREATE` |
| Update investment product | `INVESTMENT_PRODUCT_UPDATE` |
| Update app_config | `APP_CONFIG_UPDATE` |
| Customer block/unblock | `CUSTOMER_BLOCK_UNBLOCK` |
| Campaign create/update/status | `CAMPAIGN_CHANGE` |
| Referral program create/update/status | `REFERRAL_PROGRAM_CHANGE` |
| Manual reversal request | approval flow through reversal module |

FE should show these as “Submitted for approval” and refresh the approvals inbox. The checker then uses `/backoffice/approvals`.

---

# Endpoint Matrix

## Authentication

### Current Admin Context and Sessions

These endpoints close the FE session/permission contract. The current implementation uses bearer tokens, not HttpOnly cookies. `sessionPolicy.cookieModeSupported=false` is intentional for the current pilot build.

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/bo/auth/me` | Returns current admin profile, effective roles, effective permissions, MFA state, and token/session policy hints. |
| `GET` | `/bo/auth/sessions` | Lists active refresh-token sessions for the current admin. |
| `POST` | `/bo/auth/sessions/{sessionId}/revoke` | Revokes one refresh-token session owned by the current admin. |
| `DELETE` | `/bo/auth/sessions/{sessionId}` | Revokes one refresh-token session owned by the current admin. |
| `POST` | `/bo/auth/sessions/revoke-all` | Revokes all refresh-token sessions for the current admin. |
| `DELETE` | `/bo/auth/sessions` | Revokes all refresh-token sessions for the current admin. |

Sample `/bo/auth/me` response:

```json
{
  "admin": {
    "id": 1,
    "email": "superadmin@finacialwealth.com",
    "fullName": "Super Admin",
    "status": "ACTIVE",
    "mfaEnabled": true,
    "lastLoginAt": "2026-07-16T08:30:00",
    "roles": ["SUPER_ADMIN"],
    "permissions": ["approval.inbox.view", "investment.product.manage"]
  },
  "effectiveRoles": ["SUPER_ADMIN"],
  "effectivePermissions": ["approval.inbox.view", "investment.product.manage"],
  "sessionPolicy": {
    "issuer": "finacial-wealth-backoffice",
    "accessTokenTtlMinutes": 15,
    "idleTimeoutSeconds": 900,
    "warningBeforeExpirySeconds": 120,
    "mfaRequired": true,
    "tokenTransport": "BEARER",
    "cookieModeSupported": false
  }
}
```

`POST /bo/auth/refresh` rotates refresh tokens. FE must replace both the access token and refresh token with the values returned by the refresh response.


| Method | Gateway path | Purpose |
| --- | --- | --- |
| `POST` | `/bo/auth/login` | Login admin user |
| `POST` | `/bo/auth/mfa/verify` | Verify MFA during login |
| `POST` | `/bo/auth/refresh` | Refresh token |
| `POST` | `/bo/auth/logout` | Logout |
| `POST` | `/bo/auth/password/change` | Change password |
| `POST` | `/bo/auth/password/recovery/start` | Start password recovery |
| `POST` | `/bo/auth/password/recovery/complete` | Complete password recovery |
| `POST` | `/bo/auth/mfa/setup` | Setup MFA |
| `POST` | `/bo/auth/mfa/confirm` | Confirm MFA setup |

Login request:

```json
{
  "email": "admin@example.com",
  "password": "Password123!"
}
```

## Admin Users, Roles, Permissions

### Admin Self-Service and Enhanced Listing

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/bo/admin-users/me` | Current admin profile. |
| `PATCH` | `/bo/admin-users/me` | Update current admin `fullName`. Roles are ignored on this self-service endpoint. |
| `GET` | `/bo/admin-users/me/activity` | Current admin login/security activity summary. |
| `GET` | `/bo/admin-users/admins?page=0&size=20&q=&status=&role=&sort=&direction=` | Paginated admin list with search, status filter, role filter, sort and direction. |

Allowed admin list sort fields: `id`, `email`, `fullName`, `status`, `createdAt`, `updatedAt`, `lastLoginAt`.

Admin list/profile response includes server-side pagination metadata from Spring `Page` and user rows shaped like:

```json
{
  "id": 1,
  "email": "ops@example.com",
  "fullName": "Operations Admin",
  "status": "ACTIVE",
  "mfaEnabled": true,
  "roles": [{"id": 2, "name": "OPERATIONS"}],
  "createdAt": "2026-07-16T09:00:00",
  "updatedAt": "2026-07-16T09:30:00",
  "lastLoginAt": "2026-07-16T10:15:00",
  "failedAttempts": 0,
  "lockedUntil": null
}
```


| Method | Gateway path | Purpose |
| --- | --- | --- |
| `POST` | `/bo/admin-users` | Create admin user |
| `GET` | `/bo/admin-users/admins?page=0&size=20&q=femi` | List admins |
| `GET` | `/bo/admin-users/admins/{adminId}` | Get admin |
| `PATCH` | `/bo/admin-users/{adminId}` | Update admin |
| `POST` | `/bo/admin-users/{adminId}/activate` | Activate admin |
| `POST` | `/bo/admin-users/{adminId}/suspend` | Suspend admin |
| `POST` | `/bo/admin-users/{adminId}/password-reset` | Generate/reset admin password |
| `GET` | `/bo/admin/roles` | List roles |
| `POST` | `/bo/admin/roles` | Create role |
| `PUT` | `/bo/admin/roles/{roleId}/permissions` | Update role permissions |
| `GET` | `/bo/admin/permissions` | List permissions |

Create admin request:

```json
{
  "email": "ops@example.com",
  "fullName": "Ops Admin",
  "password": "TempPass123!",
  "confirmPassword": "TempPass123!",
  "roles": ["OPERATIONS"]
}
```

## Approvals

### Approval Lookup and Deep-Link Filters

FE notification deep links can resolve approval items by approval id or entity reference.

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/bo/backoffice/approvals?status=&module=&subModule=&actionType=&entityRef=&page=0&size=20` | Approval inbox with optional filters. |
| `GET` | `/bo/backoffice/approvals/by-entity-ref/{entityRef}` | Finds approval requests whose `entityRef` contains the supplied value. |

`entityRef` is generated by backend and may include an operation prefix and suffix, so FE should treat it as an opaque string.


| Method | Gateway path | Purpose |
| --- | --- | --- |
| `GET` | `/bo/backoffice/approvals?status=PENDING&page=0&size=20` | List approval inbox |
| `GET` | `/bo/backoffice/approvals/{approvalId}` | Get approval details |
| `POST` | `/bo/backoffice/approvals/{approvalId}/approve` | Approve item |
| `POST` | `/bo/backoffice/approvals/{approvalId}/reject` | Reject to remediation |
| `POST` | `/bo/backoffice/approvals/{approvalId}/request-info` | Request more information; alias for remediation/reject with a clarification reason |
| `POST` | `/bo/backoffice/approvals/{approvalId}/resubmit` | Resubmit after remediation |

Checker permissions accepted by the decision endpoints include:

| Workflow | Checker permission |
| --- | --- |
| Liquidation approvals | `investment.liquidation.approve` |
| Manual reversals | `reversal.manual.approve` |
| Investment product approvals | `investment.product.approve` |
| app_config changes | `app_config.manage` |
| Referral program changes | `referral.program.manage` |
| Campaign changes | `campaign.approve` |
| Customer profile block/unblock | `customer.profile.manage` |

Reject request:

```json
{
  "reason": "Product config needs corrected cutoff time."
}
```

Resubmit request:

```json
{
  "notes": "Cutoff time corrected and resubmitted."
}
```

Approval detail includes request payload, status, maker/checker ids, events, and for investment products/app_config the before/after snapshots where available.

## Approval Policies

| Method | Gateway path | Purpose |
| --- | --- | --- |
| `GET` | `/bo/backoffice/approval-policies` | List active policies |
| `GET` | `/bo/backoffice/approval-policies/{actionCode}` | Get one policy |
| `PUT` | `/bo/backoffice/approval-policies/{actionCode}` | Enable/disable or edit policy |

Update policy request:

```json
{
  "approvalRequired": true,
  "checkerPermission": "investment.product.approve",
  "slaHours": 24,
  "active": true
}
```

## Notifications

| Method | Gateway path | Purpose |
| --- | --- | --- |
| `GET` | `/bo/backoffice/notifications?unreadOnly=false&page=0&size=20` | List admin notifications |
| `GET` | `/bo/backoffice/notifications/unread-count` | Get unread count |
| `POST` | `/bo/backoffice/notifications/{notificationId}/read` | Mark one notification read |
| `POST` | `/bo/backoffice/notifications/read-all` | Mark all read |

Use this for approval alerts, failed integrations, reversal exceptions, settlement/liquidation exceptions, report readiness, and system warnings.

## Audit

| Method | Gateway path | Purpose |
| --- | --- | --- |
| `GET` | `/bo/backoffice/audit` | List audit events |

Common filters depend on the current audit service implementation. Use it for admin actions, approvals, app_config changes, product changes, and sensitive customer operations.

Audited endpoint rows include request metadata where available: `requestId`, `requestMethod`, `requestUri`, `ip`, `userAgent`, `reason`, `outcome`, and `errorMessage`. The audit layer intentionally does not persist raw request bodies because many backoffice requests can contain secrets, tokens, or PII.

---

# Customer 360 / Profiling

| Method | Gateway path | Purpose |
| --- | --- | --- |
| `GET` | `/bo/backoffice/profiling?page=0&size=20&sort=id,desc` | List customers |
| `GET` | `/bo/backoffice/profiling/{id}` | Get customer profile |
| `GET` | `/bo/backoffice/profiling/{id}/customer-360` | Get consolidated customer support view |
| `GET` | `/bo/backoffice/profiling/{id}/investment-summary` | Customer investment summary |
| `GET` | `/bo/backoffice/profiling/{id}/orders?type=SUBSCRIPTION&status=PENDING&page=0&size=20` | Customer investment/topup orders |
| `GET` | `/bo/backoffice/profiling/{id}/liquidations?status=PENDING&page=0&size=20` | Customer liquidations |
| `GET` | `/bo/backoffice/profiling/{id}/positions?page=0&size=20` | Customer positions |
| `PATCH` | `/bo/backoffice/profiling/{id}/block` | Block customer |
| `PATCH` | `/bo/backoffice/profiling/{id}/unblock` | Unblock customer |

Block/unblock request:

```json
{
  "reason": "Compliance review"
}
```

`customer-360` is the recommended endpoint for the main customer detail screen. It is intended to aggregate profile, KYC, wallet/account, device, referral, access status, timeline, and investment sections where available.

---

# Investment Admin

## Investment Product Endpoints

| Method | Gateway path | Purpose |
| --- | --- | --- |
| `GET` | `/bo/backoffice/investments/products` | List investment products |
| `GET` | `/bo/backoffice/investments/products/{productCode}` | Get one product |
| `GET` | `/bo/backoffice/investments/products/{productCode}/history` | Get product lifecycle/config history |
| `POST` | `/bo/backoffice/investments/products` | Create product, maker-checker enabled |
| `PUT` | `/bo/backoffice/investments/products/{productCode}` | Update product, maker-checker enabled |
| `GET` | `/bo/backoffice/investments/products/export.csv` | Download product CSV |

Partial status update:

`PUT /bo/backoffice/investments/products/{productCode}` accepts a partial body for product activation/deactivation. Backoffice fetches the current product, overlays the non-null submitted fields, and routes the result through the normal maker-checker/downstream update flow.

```json
{
  "active": false
}
```

A `401 Unauthorized` response means the admin JWT is missing, expired, or invalid. The FE should send `Authorization: Bearer <fresh accessToken>` on this PUT request; a valid token without enough role/permission should return `403 Forbidden`.

Create/update product request:

```json
{
  "productCode": "MF_BAL001",
  "name": "Balanced Money Market Fund",
  "type": "MUTUAL_FUND",
  "currency": "CAD",
  "minimumInvestmentAmount": 100,
  "valuationMethod": "UNIT_PRICE_BASED",
  "unitPrice": 1.0,
  "yieldPa": 12.5,
  "yieldYtd": 4.2,
  "tenorDays": 90,
  "active": true,
  "liquidationFeeAppliedTo": "PRINCIPAL",
  "liquidationFeeType": "PERCENTAGE",
  "liquidationFeeRate": 1.5,
  "minLiquidationFee": 0,
  "liquidationFeeCap": 100,
  "lockEnabled": false,
  "lockDays": 0,
  "earlyLiquidationFeeAppliedTo": "PRINCIPAL",
  "earlyLiquidationFeeType": "PERCENTAGE",
  "earlyLiquidationFeeRate": 2.0,
  "earlyLiquidationFeeCap": 100,
  "partnerProductCode": "PARTNER-MF-001",
  "prospectusUrl": "https://example.com/prospectus.pdf",
  "metaJson": "{\"riskRating\":\"LOW\"}",
  "enableProduct": "YES",
  "percentageCurrValue": 100,
  "scheduleMode": "FIXED",
  "interestAccrueType": "DAILY",
  "interestCapitalization": "MONTHLY",
  "settlementDelayMinutes": 0,
  "tenorMinutes": 129600,
  "maturityAtEndOfDay": true,
  "settlementAt": "2026-07-15T10:00:00Z",
  "maturityAt": "2026-10-15T10:00:00Z",
  "subscriptionCutOffTime": "15:00:00"
}
```

Required fields:

| Field | Notes |
| --- | --- |
| `productCode` | Required on create; update path also carries productCode |
| `name` | Required |
| `type` | Required enum |
| `currency` | Required |
| `minimumInvestmentAmount` | Required |
| `valuationMethod` | Required enum |
| `subscriptionCutOffTime` | Required, `HH:mm:ss` |

Product create/update response when maker-checker is enabled:

```json
{
  "approvalId": 123,
  "status": "PENDING",
  "actionCode": "INVESTMENT_PRODUCT_CREATE",
  "entityRef": "MF_BAL001",
  "message": "Investment product create submitted for approval"
}
```

Product history response now includes approval history where available:

```json
{
  "statusCode": 200,
  "data": {
    "productCode": "MF_BAL001",
    "configurationAuditAvailable": true,
    "configurationAuditMessage": "Product configuration changes are available from maker-checker approval history.",
    "approvalHistory": [
      {
        "approvalId": 123,
        "actionCode": "INVESTMENT_PRODUCT_UPDATE",
        "status": "APPROVED",
        "makerAdminId": 10,
        "checkerAdminId": 11,
        "submittedAt": "2026-07-15T10:00:00Z",
        "approvedAt": "2026-07-15T10:05:00Z",
        "beforeSnapshot": {},
        "afterSnapshot": {},
        "snapshotSource": "FXPEER_CURRENT_PRODUCT"
      }
    ]
  }
}
```

## Investment Operations

| Method | Gateway path | Purpose |
| --- | --- | --- |
| `GET` | `/bo/backoffice/investments/dashboard` | Investment dashboard summary |
| `GET` | `/bo/backoffice/investments/oversight` | Oversight dashboard |
| `GET` | `/bo/backoffice/investments/performance` | Performance dashboard |
| `GET` | `/bo/backoffice/investments/orders?status=PENDING&productCode=MF_BAL001&page=0&size=20` | List orders/subscriptions/topups |
| `GET` | `/bo/backoffice/investments/liquidations?status=PENDING&productCode=MF_BAL001&page=0&size=20` | List active liquidations |
| `GET` | `/bo/backoffice/investments/liquidations/history?status=APPROVED&page=0&size=20` | List liquidation history |
| `POST` | `/bo/backoffice/investments/approve-liquidation-request` | Approve liquidation directly |
| `POST` | `/bo/backoffice/investments/deny-customer-liquidation-request` | Deny/cancel liquidation |
| `POST` | `/bo/backoffice/investments/liquidations/{orderRef}/retry` | Retry only failed liquidation requests after ops review |
| `POST` | `/bo/backoffice/investments/customers/{email}/investment-freeze` | Freeze/unfreeze customer investment top-up and redemption activity |

### Investment Product Create/Update Governance Fields

Product create/update is maker-checker aware. If `INVESTMENT_PRODUCT_CREATE` or `INVESTMENT_PRODUCT_UPDATE` is enabled, FE should expect `202 Accepted`; otherwise the change is applied immediately. New product creation now requires product governance metadata so incomplete products do not silently go live.

Create:

```http
POST /bo/backoffice/investments/products
```

Update:

```http
PUT /bo/backoffice/investments/products/{productCode}
```

Required on create:

```json
{
  "productCode": "MF_BAL001",
  "name": "Nigeria Prime Money Market Fund",
  "type": "MUTUAL_FUND",
  "currency": "NGN",
  "minimumInvestmentAmount": 5000,
  "valuationMethod": "RATE",
  "yieldPa": 18.5,
  "subscriptionCutOffTime": "15:00:00",
  "description": "Low-risk NGN money market product",
  "issuerName": "Plural",
  "fundManager": "Plural Investment Desk",
  "riskRating": "LOW",
  "minimumHoldingDays": 0,
  "maturityDefaultAction": "REDEEM_TO_WALLET"
}
```

Optional product governance fields:

| Field | Meaning |
| --- | --- |
| `maximumHoldingDays` | Maximum intended holding period in days |
| `maximumTotalRaise` | Product capacity limit; FE can display capacity/progress from this |
| `autoCloseAtCapacity` | Whether the product should be closed to new subscriptions when capacity is reached |
| `liquidationFrequencyLimit` | Number of liquidations allowed per period; must be sent with `liquidationFrequencyPeriod` |
| `liquidationFrequencyPeriod` | `MONTH`, `QUARTER`, or `YEAR` |
| `maturityDefaultAction` | `REDEEM_TO_WALLET`, `ROLLOVER_PRINCIPAL`, or `ROLLOVER_PRINCIPAL_AND_INTEREST` |

Validation failures return a normal `400` payload:

```json
{
  "status": 400,
  "code": "BAD_REQUEST",
  "message": "issuerName is required",
  "requestId": "REQ-ID",
  "path": "/backoffice/investments/products"
}
```

Product list/detail/history responses now return the governance fields as normal top-level product fields. Internally FxPeer stores them inside `metaJson.productGovernance`, but FE does not need to parse `metaJson`.

Runtime enforcement now handled by FxPeer:

- New subscriptions are rejected when `maximumTotalRaise` would be exceeded by active or in-flight subscription volume plus the requested amount.
- If `autoCloseAtCapacity=true`, the rejection message tells FE the product is closed because the capacity limit has been reached.
- Redemption requests are rejected before `minimumHoldingDays` has elapsed from the subscription/position creation date.
- Redemption requests are rejected when `liquidationFrequencyLimit` has already been used for the configured `liquidationFrequencyPeriod`.
- `maturityDefaultAction` is accepted, validated, stored, and returned for FE/config visibility; maturity scheduler execution is still a separate future automation step if the business wants automatic maturity processing.

Redemption/liquidation balance behavior:

- FxPeer reserves the requested redemption amount immediately on the investment position.
- `currentValue` remains the gross position value for backward compatibility.
- New balance fields are returned where position rows are exposed:
  - `grossInvestmentAmount`
  - `reservedRedemptionAmount`
  - `availableInvestmentAmount`
  - `settledRedemptionAmount`
  - legacy alias still present: `reservedLiquidationAmount`
- Backoffice should use `availableInvestmentAmount` when showing what remains after pending redemption holds.
- Customer email/push notifications are emitted by FxPeer and delivered by Utility for request received, redemption completed, and redemption cancelled events. Backoffice does not need to call a notification endpoint after approve/deny.

Redemption policy controls:

The operational source of truth is `app_config`, governed through Backoffice maker-checker. FxPeer reads these values at runtime. Environment variables remain as deployment fallback values only when the `app_config` rows are absent. Dev can fall back to FxPeer AUTO behavior while backoffice setup is incomplete; pilot/prod must explicitly set and approve these values.

| app_config key | Allowed value | Meaning |
| --- | --- |
| `investment.redemption.approval-mode` | `AUTO`, `MANUAL`, `THRESHOLD` | Controls whether pending redemptions are auto-settled, held for backoffice approval, or auto-settled only up to a limit |
| `investment.redemption.auto-approval-threshold.NGN` | Numeric amount, for example `50000` | Maximum NGN redemption amount auto-settled when mode is `THRESHOLD` |
| `investment.redemption.auto-approval-threshold.CAD` | Numeric amount, for example `1000` | Maximum CAD redemption amount auto-settled when mode is `THRESHOLD` |
| `investment.redemption.auto-approval-threshold` | Numeric amount, for example `50000` | Fallback maximum amount used only when no currency-specific key exists |
| `investment.redemption.scheduler-enabled` | `true` or `false` | Enables/disables the liquidation scheduler without a restart |
| `investment.redemption.scheduler-cron` | Cron expression | Scheduler cron expression; effective after service restart because Spring schedules cron at startup |

FxPeer resolves the threshold from the redemption investment product currency. For example, NGN redemptions use `investment.redemption.auto-approval-threshold.NGN`; CAD redemptions use `investment.redemption.auto-approval-threshold.CAD`. The generic threshold remains only for backward compatibility and as a fallback.

Backoffice change flow:

1. Maker calls `PATCH /bo/backoffice/app-config/{configName}` with the proposed value and reason.
2. If `APP_CONFIG_UPDATE` approval policy is enabled, the API returns `202 Accepted` with an approval request.
3. Checker approves from `/bo/backoffice/approvals/{approvalId}/approve`.
4. The value is written to `app_config`; FxPeer uses the approved value on subsequent liquidation scheduler runs.

Example threshold update request:

```json
{
  "configValue": "50000",
  "reason": "Allow automatic redemption settlement up to 50k during pilot liquidity window"
}
```

Recommended pilot-to-prod setup: use `THRESHOLD` once liquidity limits are agreed, so lower redemptions keep the fast customer experience while high-value redemptions remain pending for backoffice approval.

Liquidation decision request shape is proxied to FxPeer. Typical fields include:

```json
{
  "orderRef": "LIQ-001",
  "reason": "Approved after review"
}
```

Liquidation retry request:

```http
POST /bo/backoffice/investments/liquidations/{orderRef}/retry
```

```json
{
  "reason": "Provider timeout cleared; retry after confirming no duplicate settlement"
}
```

Retry is accepted only when the liquidation order is `FAILED` or `LIQUIDATION_FAILED`. FxPeer locks the order/position, confirms enough unreserved investment value remains, re-reserves the redemption amount, moves the order back to `LIQUIDATION_PENDING_APPROVAL`, and emits the normal redemption-request notification. Non-failed liquidations return `409`.

Investment freeze request:

```http
POST /bo/backoffice/investments/customers/{email}/investment-freeze
```

```json
{
  "frozen": true,
  "productCode": "MF_BAL001",
  "reason": "Compliance review pending"
}
```

`productCode` is optional. If omitted, all investment top-up and redemption activity for that customer is blocked. If present, only that product is blocked. The mobile API contract does not change; blocked top-up/redemption calls return `423` with `Investment activity is currently frozen for this customer.`

Liquidation list/history rows now include these ops fields:

```json
{
  "grossInvestmentAmount": 155000,
  "reservedRedemptionAmount": 55000,
  "availableInvestmentAmount": 100000,
  "reserveStatus": "HELD",
  "retryEligible": false,
  "correctionMode": "REVERSAL_ONLY",
  "notificationStatus": "REQUESTED_SENT",
  "failureReason": null
}
```


## Featured Services

| Method | Gateway path | Purpose |
| --- | --- | --- |
| `GET` | `/bo/backoffice/investments/featured-services` | Mobile featured services resolved view |
| `GET` | `/bo/backoffice/investments/featured-services/config` | Backoffice config |
| `POST` | `/bo/backoffice/investments/featured-services/config` | Save config |

---

# app_config Governance

| Method | Gateway path | Purpose |
| --- | --- | --- |
| `GET` | `/bo/backoffice/app-config?search=otp&page=0&size=20` | List app_config registry/current values |
| `GET` | `/bo/backoffice/app-config/{configName}` | Get config detail |
| `PUT` | `/bo/backoffice/app-config/{configName}/registry` | Register metadata/governance for config |
| `PATCH` | `/bo/backoffice/app-config/{configName}` | Update config value, maker-checker enabled |
| `GET` | `/bo/backoffice/app-config/{configName}/history?page=0&size=20` | Config history |
| `POST` | `/bo/backoffice/app-config/{configName}/history/{historyId}/rollback` | Rollback to history value, maker-checker enabled |

Register config metadata:

```json
{
  "ownerService": "profiling",
  "valueType": "BOOLEAN",
  "editable": true,
  "sensitive": false,
  "validationRegex": "^(true|false)$",
  "description": "Controls whether BVN face verification is enabled"
}
```

Update config value:

```json
{
  "configValue": "true",
  "reason": "Enable for pilot testing"
}
```

Rollback request:

```json
{
  "reason": "Rollback after failed pilot validation"
}
```

---

# NGN Customer Transactions

This endpoint gives backoffice one operational view of customer NGN wallet movements from the transaction service and FxPeer service. FE calls only backoffice with the admin bearer token; backoffice then calls downstream services using the internal service token.

| Method | Gateway path | Purpose |
| --- | --- | --- |
| `GET` | `/bo/backoffice/transactions/ngn` | List/search NGN customer transactions across transaction and FxPeer sources |
| `GET` | `/bo/backoffice/transactions/ngn/{transactionId}` | Get full sanitized NGN transaction detail across transaction and FxPeer sources |
| `GET` | `/bo/backoffice/transactions/ngn/export.csv` | Download filtered NGN customer transactions as an Excel-compatible CSV |
| `GET` | `/bo/backoffice/transactions/ngn/export.pdf` | Download filtered NGN customer transactions as a PDF summary |

Summary endpoint permission: one of `transactions.view`, `transactions.filter`, `ROLE_SUPER_ADMIN`, `ROLE_ADMIN`, `ROLE_OPERATIONS`, or `ROLE_FINANCE`.

Detail endpoint permission: `ROLE_SUPER_ADMIN` only for now, because it exposes matched customer accounts/account numbers and may include per-account balance detail from core banking.

Export endpoints also accept `reports.export`. For a restricted ops/reporting user who should only see cumulative NGN balances, NGN customer transactions, and export them, assign:

- `transactions.view`
- `transactions.filter`
- `reports.export`

Query parameters:

| Param | Required | Example | Notes |
| --- | --- | --- | --- |
| `source` | No | `ALL` | `ALL`, `TRANSACTIONS`, or `FXPEER`. Default is `ALL`. |
| `customer` | No | `customer@email.com` | Searches customer email, createdBy, wallet, sender/receiver, and customer names. |
| `walletId` | No | `9354185507` | Exact wallet/account number filter. |
| `transactionId` | No | `TRX-178418900001` | Exact transaction id filter. |
| `type` | No | `TRANSFER` | Matches transaction/payment/request type fields. |
| `status` | No | `SUCCESS` | Matches status-like fields available in legacy rows. |
| `q` | No | `Access Bank` | General search across wallet, parties, transaction id, narration, email, and bank name. |
| `fromDate` | No | `2026-07-01` | Inclusive UTC date filter. |
| `toDate` | No | `2026-07-31` | Inclusive UTC date filter. |
| `includeLegacyNullCurrency` | No | `true` | Default `true` so older NGN rows without `currencyCode` are still visible. |
| `page` | No | `0` | Zero-based page. |
| `size` | No | `20` | Max `200`. |

Example:

```http
GET /bo/backoffice/transactions/ngn?source=ALL&customer=customer@email.com&fromDate=2026-07-01&toDate=2026-07-31&page=0&size=20
Authorization: Bearer {{accessToken}}
```

Sample response:

```json
{
  "statusCode": 200,
  "description": "NGN transactions pulled successfully.",
  "data": {
    "content": [
      {
        "id": 3001,
        "sourceService": "TRANSACTIONS",
        "walletNo": "9354185507",
        "customerReference": "customer@email.com",
        "emailAddress": "customer@email.com",
        "transactionId": "TRX-178418900001",
        "transactionType": "DEBIT",
        "paymentType": "INTERBANK_TRANSFER",
        "requestType": "SUCCESS",
        "status": "SUCCESS",
        "amount": 25000,
        "sentAmount": "25000",
        "fees": 10,
        "sender": "9354185507",
        "senderName": "Jane Customer",
        "receiver": "0123456789",
        "receiverName": "John Receiver",
        "counterparty": "John Receiver",
        "receiverBankName": "Access Bank",
        "receiverBankCode": "044",
        "narration": "Transfer",
        "currencyCode": "NGN",
        "createdDate": "2026-07-31T10:15:00Z",
        "lastModifiedDate": "2026-07-31T10:15:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "currencyCode": "NGN",
    "source": "ALL",
    "sourceTotals": {
      "TRANSACTIONS": 1,
      "FXPEER": 0
    },
    "warnings": []
  }
}
```

If `source=ALL` and one downstream source is temporarily unavailable, backoffice returns the available source rows plus a warning. If FE selects one explicit source and that source is unavailable, backoffice returns `502`.

Export examples:

```http
GET /bo/backoffice/transactions/ngn/export.csv?source=ALL&fromDate=2026-07-01&toDate=2026-07-31&size=1000
Authorization: Bearer {{accessToken}}
```

```http
GET /bo/backoffice/transactions/ngn/export.pdf?source=ALL&fromDate=2026-07-01&toDate=2026-07-31&size=200
Authorization: Bearer {{accessToken}}
```

CSV is the recommended Excel download format for full operational exports. PDF is intended for a readable summary and is capped lower than CSV.

### NGN Transaction Detail

Use this when an Ops user clicks a transaction row and needs the full available transaction detail without FE calling downstream services directly.

Query parameters:

| Param | Required | Example | Notes |
| --- | --- | --- | --- |
| `source` | No | `ALL` | `ALL`, `TRANSACTIONS`, or `FXPEER`. Default is `ALL`. |
| `includeLegacyNullCurrency` | No | `true` | Default `true` so older NGN rows without `currencyCode` are still visible. |

Example:

```http
GET /bo/backoffice/transactions/ngn/TRX-178418900001?source=ALL
Authorization: Bearer {{accessToken}}
```

Sample response:

```json
{
  "statusCode": 200,
  "description": "NGN transaction details pulled successfully.",
  "data": {
    "transactionId": "TRX-178418900001",
    "source": "ALL",
    "currencyCode": "NGN",
    "primary": {
      "id": 3001,
      "sourceService": "TRANSACTIONS",
      "walletNo": "9354185507",
      "customerReference": "customer@email.com",
      "emailAddress": "customer@email.com",
      "transactionId": "TRX-178418900001",
      "transactionType": "DEBIT",
      "paymentType": "INTERBANK_TRANSFER",
      "requestType": "SUCCESS",
      "status": "SUCCESS",
      "amount": 25000,
      "sentAmount": "25000",
      "fees": 10,
      "sender": "9354185507",
      "senderName": "Jane Customer",
      "receiver": "0123456789",
      "receiverName": "John Receiver",
      "counterparty": "John Receiver",
      "receiverBankName": "Access Bank",
      "receiverBankCode": "044",
      "narration": "Transfer",
      "currencyCode": "NGN",
      "createdDate": "2026-07-31T10:15:00Z",
      "lastModifiedDate": "2026-07-31T10:15:00Z"
    },
    "entryCount": 1,
    "entries": [
      {
        "id": 3001,
        "sourceService": "TRANSACTIONS",
        "walletNo": "9354185507",
        "customerReference": "customer@email.com",
        "transactionId": "TRX-178418900001",
        "status": "SUCCESS",
        "amount": 25000,
        "currencyCode": "NGN",
        "createdDate": "2026-07-31T10:15:00Z"
      }
    ],
    "sources": {
      "TRANSACTIONS": {
        "sourceService": "TRANSACTIONS",
        "entryCount": 1
      },
      "FXPEER": {
        "sourceService": "FXPEER",
        "entryCount": 0
      }
    },
    "warnings": []
  }
}
```

If `source=ALL` and the transaction is absent in one source, backoffice still returns the source where the transaction exists. If it is absent in all selected sources, backoffice returns `404`.

---

# NGN Account Balances

This endpoint gives backoffice a cumulative NGN account balance view. FE calls backoffice with the admin bearer token. Backoffice resolves local Plural NGN accounts from `add_account_details`, calls the transaction service with the internal service token, and the transaction service calls SmartCore/core banking:

```http
POST /v2/balances/cumulative
Authorization: Bearer <wallet-system-token>
channel: API
```

| Method | Gateway path | Purpose |
| --- | --- | --- |
| `GET` | `/bo/backoffice/accounts/ngn/balances/cumulative/summary` | FE-facing aggregate NGN balance total with no per-customer/account balances |
| `GET` | `/bo/backoffice/accounts/ngn/balances/cumulative` | Support/investigation detail view for matched local accounts and core response |

Required permission: one of `transactions.view`, `transactions.filter`, `ROLE_SUPER_ADMIN`, `ROLE_ADMIN`, `ROLE_OPERATIONS`, or `ROLE_FINANCE`.

## FE-facing summary endpoint

Use this for the backoffice regulatory/dashboard total. FE should call it with no parameters by default:

```http
GET /bo/backoffice/accounts/ngn/balances/cumulative/summary
Authorization: Bearer {{accessToken}}
```

Optional search/filter:

```http
GET /bo/backoffice/accounts/ngn/balances/cumulative/summary?keyword=oluwayemisi.oshin@gmail.com
Authorization: Bearer {{accessToken}}
```

FE-facing query parameters for the summary endpoint:

| Param | Required | Example | Notes |
| --- | --- | --- | --- |
| `keyword` | No | `customer@email.com` | Filters NGN accounts by email, phone, wallet id, account number, or virtual account number before calling core. |

FE should show only one search field for this lookup:

- Label: `Customer or account keyword`
- Placeholder: `Email, phone, wallet, account, or virtual account`
- Leave `keyword` empty when Ops wants the cumulative balance across all local NGN accounts.

Do not expose `productCode`, `accountNumbers`, or `channel` in the normal backoffice UI. Those are backend/support-level controls. Backoffice/transaction service handles product-code resolution internally from the SmartCore wallet-system token, defaults `channel` to `API`, and resolves account numbers from local NGN accounts.

Summary sample response:

```json
{
  "statusCode": 200,
  "description": "Successful",
  "data": {
    "currency": "NGN",
    "accountCount": 250,
    "totalBalance": 125000000,
    "totalAvailableBalance": 125000000,
    "totalLedgerBalance": 125000000,
    "coreStatusCode": 200,
    "coreDescription": "Successful"
  }
}
```

The summary response must not expose individual customer accounts, account numbers, or per-account balances. It is the endpoint FE should use for the normal cumulative NGN balance card.

## Support detail endpoint

Use this only for controlled operations/support investigation where a Super Admin needs to see matched local accounts and the raw core response shape. `EXTERNAL_COMPLIANCE` users should not call this endpoint; they should use `/summary`.

```http
GET /bo/backoffice/accounts/ngn/balances/cumulative?keyword=customer@email.com
Authorization: Bearer {{accessToken}}
```

Detail sample response:

```json
{
  "statusCode": 200,
  "description": "Successful",
  "data": {
    "currency": "NGN",
    "accountCount": 2,
    "accounts": [
      {
        "id": 301,
        "accountNumber": "100001",
        "walletId": "9354185507",
        "emailAddress": "customer@email.com",
        "phoneNumber": "2348012345678",
        "currencyCode": "NGN",
        "countryCode": "NG",
        "virtualAccountNumber": "100001",
        "virtualAccountName": "Jane Customer"
      }
    ],
    "accountNumbers": ["100001", "100002"],
    "coreBankingResponse": {
      "statusCode": 200,
      "description": "Successful",
      "data": {
        "currency": "NGN",
        "totalBalance": 150000,
        "balances": []
      }
    }
  }
}
```

The `accounts` list is the local Plural NGN account set used for the request. The `accountNumbers` array in the response is informational only so Ops can see what was submitted to core banking; FE does not need to collect account numbers from the user. The `coreBankingResponse` is the SmartCore/core banking response from `/v2/balances/cumulative`.

Support-only query parameters, not for the normal FE screen:

| Param | Required | Notes |
| --- | --- | --- |
| `productCode` | No | Optional backend override for support/testing only. If omitted, transaction service resolves product code from the SmartCore wallet-system token. |
| `accountNumbers` | No | Optional repeated backend override for support/testing only. If omitted, transaction service resolves account numbers from local NGN accounts using `keyword`, or all NGN accounts when `keyword` is empty. |
| `channel` | No | Optional backend override. Defaults to `API`. |

---

# Reversals

| Method | Gateway path | Purpose |
| --- | --- | --- |
| `GET` | `/bo/backoffice/reversals/summary` | Reversal exception summary |
| `GET` | `/bo/backoffice/reversals/cases?source=TRANSACTIONS_INTERBANK&status=FAILED&page=0&size=20` | List reversal cases |
| `POST` | `/bo/backoffice/reversals/cases/{source}/{caseRef}/manual-request` | Submit manual reversal approval request |

Manual reversal request is a maker-checker submission. It does not directly move funds from the FE call. Operations should only submit it after confirming the original fulfilment really failed, using the original end-to-end transaction id and provider/status evidence.

```json
{
  "notes": "Auto reversal failed; please approve manual retry.",
  "reason": "Provider confirmed failed fulfilment after debit.",
  "evidenceReference": "NIP-STATUS-CHECK-20260720-001",
  "endToEndTransactionId": "TRX-178418900001",
  "providerReference": "PROVIDER-REF-001",
  "providerStatus": "FAILED",
  "providerStatusEvidence": "Provider status enquiry returned FAILED for the original transaction."
}
```

Manual reversal requests are accepted only for cases currently in `PENDING`, `FAILED`, or `RECON_REQUIRED`. Cases already `PROCESSING` or `SUCCESS` must not be resubmitted from FE.

When a checker approves the generated approval request, backoffice executes the reversal through an internal service-to-service call. Direct downstream retry execution is protected by `X-Backoffice-Internal-Token`; FE and Ops users should not call raw retry endpoints.

Unified reversal case rows include control/eligibility fields for the operations screen:

```json
{
  "source": "TRANSACTIONS_INTERBANK",
  "caseRef": "TRX-178418900001",
  "status": "RECON_REQUIRED",
  "reversalControlStatus": "MANUAL_INVESTIGATION",
  "canSubmitManualReversalRequest": true,
  "fulfilmentStatus": "STATUS_UNKNOWN",
  "reversalEligibility": "BLOCKED_PENDING_FULFILMENT_CONFIRMATION",
  "reversalIdempotencyKey": "TRX-178418900001-RB",
  "providerReference": "PROVIDER-REF-001",
  "providerStatusCheckedAt": "2026-07-20T09:30:00Z",
  "openManualRequests": 0
}
```

Control statuses FE can display:

| Value | Meaning |
| --- | --- |
| `DETECTED` | Exception detected, not yet classified. |
| `PENDING_MAKER_REVIEW` | Auto retry failed or pending; maker can review. |
| `MANUAL_INVESTIGATION` | Fulfilment is not confirmed failed yet; operations must verify provider status before maker request. |
| `PROCESSING` | A scheduler/admin worker has claimed the reversal and is processing it. |
| `REVERSED` | Reversal completed successfully. |

Scheduler behavior:

- Transaction reversals are picked by `pool.process.retry.success.debit.rollback.cron`, default every 2 minutes.
- FxPeer airtime rollback legs are picked by `fx.airtime.rollback.retry.cron`, default every 2 minutes.
- These are database-backed retry queues, not broker queues. FE should treat `PENDING`, `FAILED`, and `RECON_REQUIRED` as visible work items, and `PROCESSING` as temporarily locked by a backend worker.
- If a worker dies while a case is `PROCESSING`, stale claims are released back to `FAILED` after the configured stale window: `pool.process.retry.success.debit.rollback.processing-stale-minutes` or `fx.airtime.rollback.processing-stale-minutes`, default 15 minutes.

Supported `source` values currently include:

| Source | Case ref |
| --- | --- |
| `FXPEER_AIRTIME` | process id |
| `TRANSACTIONS_INTERBANK` | transaction id |
| `TRANSACTIONS_LOCAL_TRANSFER` | transaction id |
| `TRANSACTIONS` | legacy umbrella |

Product-specific wrappers still exist:

| Method | Gateway path | Purpose |
| --- | --- | --- |
| `GET` | `/bo/backoffice/interbank/reversals/summary` | Legacy interbank reversal summary |
| `GET` | `/bo/backoffice/interbank/reversals` | Legacy interbank reversal cases |
| `GET` | `/bo/backoffice/fxpeer/services/airtime-reversals/summary` | Airtime reversal summary |
| `GET` | `/bo/backoffice/fxpeer/services/airtime-reversals` | Airtime reversal cases |

FE should prefer `/backoffice/reversals` for the unified module. Direct VAS retry through `/backoffice/fxpeer/services/airtime-reversals/{processId}/retry` is disabled; use `/backoffice/reversals/cases/FXPEER_AIRTIME/{processId}/manual-request`.

---

# Campaigns

| Method | Gateway path | Purpose |
| --- | --- | --- |
| `POST` | `/bo/backoffice/campaigns/create` | Create campaign, maker-checker possible |
| `PUT` | `/bo/backoffice/campaigns/{id}` | Update campaign |
| `POST` | `/bo/backoffice/campaigns/{id}/approve` | Approve campaign |
| `POST` | `/bo/backoffice/campaigns/{id}/stop` | Stop campaign |
| `POST` | `/bo/backoffice/campaigns/{id}/cancel` | Cancel campaign |
| `POST` | `/bo/backoffice/campaigns/{id}/restart` | Restart campaign |
| `GET` | `/bo/backoffice/campaigns/get-all` | List campaigns |
| `GET` | `/bo/backoffice/campaigns/{id}` | Get campaign |
| `GET` | `/bo/backoffice/campaigns/{id}/audit` | Campaign audit |

Create campaign request:

```json
{
  "title": "Pilot Launch",
  "description": "Pilot launch campaign",
  "mediaObjectName": "campaigns/pilot.png",
  "mediaContentType": "image/png",
  "mediaSignedUrl": "https://...",
  "embeddedLink": "https://finacialwealth.com",
  "startAt": "2026-07-15T00:00:00Z",
  "endAt": "2026-08-15T00:00:00Z",
  "rotationSeconds": 6,
  "displayMode": "CAROUSEL",
  "items": []
}
```

---

# Referral Programs

| Method | Gateway path | Purpose |
| --- | --- | --- |
| `POST` | `/bo/backoffice/referral-programs/create` | Create referral program, maker-checker possible |
| `PUT` | `/bo/backoffice/referral-programs/{id}` | Update referral program |
| `POST` | `/bo/backoffice/referral-programs/{id}/activate` | Activate |
| `POST` | `/bo/backoffice/referral-programs/{id}/pause` | Pause |
| `POST` | `/bo/backoffice/referral-programs/{id}/end` | End |
| `GET` | `/bo/backoffice/referral-programs/get-all?productType=P2P` | List |
| `GET` | `/bo/backoffice/referral-programs/{id}` | Get |
| `GET` | `/bo/backoffice/referral-programs/{id}/audit` | Audit |
| `GET` | `/bo/backoffice/referral-programs/active?productType=P2P` | Get active program |

Optional header for write/status requests:

```http
X-User-Id: <adminUserId>
```

Create referral program request:

```json
{
  "programCode": "P2P_REF_001",
  "title": "P2P Referral",
  "description": "Reward customers for referrals",
  "productType": "P2P",
  "rewardTarget": "REFERRER",
  "rewardMode": "FIXED",
  "rewardValue": 10,
  "rewardCurrencyMode": "FIXED",
  "fixedCurrencyCode": "CAD",
  "minQualifyingAmount": 100,
  "minRewardAmount": 1,
  "maxRewardAmount": 50,
  "qualifyingTransactionCount": 1,
  "startAt": "2026-07-15T00:00:00Z",
  "endAt": "2026-08-15T00:00:00Z"
}
```

---

# Group Savings

These endpoints support the Group Savings visibility MVP for Backoffice FE. FE calls only Backoffice with the admin JWT; Backoffice proxies to the transaction service.

| Method | Gateway path | Purpose |
| --- | --- | --- |
| `GET` | `/bo/backoffice/group-savings/groups?page=0&size=20&status=ACTIVE&search=July` | List groups for the group management table |
| `GET` | `/bo/backoffice/group-savings/groups/{groupId}` | Get group detail with members and cycles |
| `GET` | `/bo/backoffice/group-savings/contribution-payout-monitoring?period=DAILY&fromDate=2026-07-01&toDate=2026-07-31` | Contribution/payout dashboard |
| `GET` | `/bo/backoffice/group-savings/slot-assignment-tracking?status=UPCOMING` | Slot schedule, payout history, and alerts |
| `GET` | `/bo/backoffice/group-savings/groups/{groupId}/cycle-health` | Per-cycle contribution/payout health for one group |
| `GET` | `/bo/backoffice/group-savings/cycles/{cycleId}/health` | Contribution/payout health for one cycle |
| `POST` | `/bo/backoffice/group-savings/cycles/{cycleId}/retry-failed` | Requeue failed contribution/payout records for a cycle after ops review |
| `POST` | `/bo/backoffice/group-savings/groups/{groupId}/close` | Close group. Admin/Super Admin only |
| `POST` | `/bo/backoffice/group-savings/groups/{groupId}/deletion-request` | Typed alias for requesting/admin-triggering group savings deletion. Body may include `reason` |
| `POST` | `/bo/backoffice/group-savings/delete` | Legacy delete group saving bridge |

List permissions: roles `SUPER_ADMIN`, `ADMIN`, `OPERATIONS`, or `FINANCE`.

Close/delete permissions: roles `SUPER_ADMIN` or `ADMIN`; typed deletion alias also accepts `groupSavings.group.delete`.

## Group List

Query parameters:

| Param | Required | Example | Notes |
| --- | --- | --- | --- |
| `status` | No | `ACTIVE` | Supported normalized statuses: `ALL`, `INITIATED`, `CREATED`, `ACTIVE`, `IN_PROGRESS`, `COMPLETED`, `CLOSED`. Omit or use `ALL` for all. |
| `search` | No | `July` | Searches group name, invite code, owner email, phone, wallet id, and transaction references. |
| `page` | No | `0` | Zero-based page. |
| `size` | No | `20` | Page size. |

Sample list response:

```json
{
  "statusCode": 200,
  "description": "Group savings groups fetched successfully.",
  "data": {
    "content": [
      {
        "groupId": 12,
        "groupName": "July Staff Group",
        "inviteCode": "GS-ABCD12",
        "transactionId": "178500000001",
        "transactionIdLink": "GS-LINK-178500000001",
        "ownerName": "Jane Customer",
        "ownerEmailAddress": "jane@example.com",
        "ownerPhoneNumber": "2348012345678",
        "ownerWalletId": "9354185507",
        "members": 5,
        "configuredMembers": "5",
        "cycle": "Monthly",
        "nextPayout": "2026-08-31",
        "volume": 250000,
        "status": "ACTIVE",
        "transactionStatus": "3",
        "transactionStatusDesc": "Active",
        "createdDate": "2026-07-01T09:00:00Z",
        "lastModifiedDate": "2026-07-31T09:00:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "statusOptions": ["ALL", "INITIATED", "CREATED", "ACTIVE", "IN_PROGRESS", "COMPLETED", "CLOSED"]
  }
}
```

## Group Detail

Sample detail response:

```json
{
  "statusCode": 200,
  "description": "Group savings group fetched successfully.",
  "data": {
    "groupId": 12,
    "groupName": "July Staff Group",
    "status": "ACTIVE",
    "description": "Staff contribution circle",
    "allowPublicToJoin": "false",
    "adminPayOutSlot": "1",
    "availablePayOutSlot": "2,3,4,5",
    "contributionDate": "2026-07-01",
    "contributionWindowEnd": "2026-07-05",
    "payoutDate": "2026-07-31",
    "payoutPolicy": "ORDERED",
    "contributionFrequency": "MONTHLY",
    "membersList": [
      {
        "memberName": "Jane Customer",
        "memberEmailAddress": "jane@example.com",
        "memberId": "9354185507",
        "payOutSlot": "1"
      }
    ],
    "cycles": [
      {
        "cycleNumber": 1,
        "contributionDate": "2026-07-01",
        "contributionWindowEnd": "2026-07-05",
        "payoutDate": "2026-07-31",
        "status": "PAID"
      }
    ]
  }
}
```

## Contribution And Payout Monitoring

Query parameters:

| Param | Required | Example | Notes |
| --- | --- | --- | --- |
| `period` | No | `DAILY` | Supported: `DAILY`, `WEEKLY`, `MONTHLY`. |
| `fromDate` | No | `2026-07-01` | Defaults by selected period when omitted. |
| `toDate` | No | `2026-07-31` | Defaults to current Lagos date when omitted. |
| `groupId` | No | `12` | Restrict dashboard to one group. |

Sample monitoring response:

```json
{
  "statusCode": 200,
  "description": "Contribution and payout monitoring fetched successfully.",
  "data": {
    "filters": {
      "period": "DAILY",
      "fromDate": "2026-07-01",
      "toDate": "2026-07-31",
      "groupId": null
    },
    "summary": {
      "totalContributions": 250000,
      "totalPayouts": 200000,
      "netFlow": 50000,
      "activeGroups": 8,
      "lastUpdatedAt": "2026-08-11T10:00:00Z"
    },
    "trend": [
      {
        "key": "2026-07-01",
        "label": "Tue",
        "periodStart": "2026-07-01",
        "periodEnd": "2026-07-01",
        "contributionAmount": 50000,
        "payoutAmount": 0,
        "contributionCount": 5,
        "payoutCount": 0
      }
    ],
    "alerts": [
      {
        "severity": "HIGH",
        "category": "PAYOUT",
        "status": "FAILED",
        "reference": "TX-12-3",
        "message": "Payout TX-12-3 failed - retrigger required.",
        "groupId": 12,
        "groupName": "July Staff Group"
      }
    ],
    "groupOptions": [
      {"groupId": 12, "groupName": "July Staff Group", "inviteCode": "GS-ABCD12", "transactionId": "178500000001", "status": "3"}
    ],
    "periodOptions": ["DAILY", "WEEKLY", "MONTHLY"]
  }
}
```

## Slot Assignment Tracking

Query parameters:

| Param | Required | Example | Notes |
| --- | --- | --- | --- |
| `groupId` | No | `12` | Restrict to one group. |
| `status` | No | `UPCOMING` | Supported: `UPCOMING`, `IN_PROGRESS`, `MISSED`, `COMPLETED`. |

Sample slot response:

```json
{
  "statusCode": 200,
  "description": "Slot assignment and tracking fetched successfully.",
  "data": {
    "filters": {"groupId": null, "status": "UPCOMING"},
    "slotSchedule": [
      {
        "groupId": 12,
        "groupName": "July Staff Group",
        "slotNumber": 3,
        "memberName": "Jane Customer",
        "memberWalletId": "9354185507",
        "payoutDate": "2026-08-31",
        "status": "UPCOMING",
        "cycleStatus": "PENDING",
        "reference": "GS-12-SLOT-3"
      }
    ],
    "payoutHistory": [],
    "alerts": [],
    "groupOptions": [],
    "statusOptions": ["UPCOMING", "IN_PROGRESS", "MISSED", "COMPLETED"]
  }
}
```

Cycle health response example:

```json
{
  "statusCode": 200,
  "description": "Group savings cycle health fetched successfully.",
  "data": {
    "summary": {
      "groupId": 12,
      "groupName": "July Staff Group",
      "expectedMembers": 5,
      "cycleCount": 5,
      "completedCycles": 2,
      "cyclesNeedingAttention": 1,
      "lastUpdatedAt": "2026-07-18T09:15:30Z"
    },
    "cycles": [
      {
        "cycleId": 31,
        "groupId": 12,
        "cycleNumber": 3,
        "cycleStatus": "IN_PROGRESS",
        "expectedContributionCount": 5,
        "actualContributionCount": 5,
        "settledContributionCount": 4,
        "pendingContributionCount": 0,
        "processingContributionCount": 0,
        "failedContributionCount": 1,
        "settledContributionAmount": 40000.00,
        "schedulerEligible": true,
        "healthStatus": "ATTENTION",
        "recommendedAction": "Review downstream ledger state, then call retry-failed if debit/credit did not settle.",
        "payout": {
          "payoutId": 17,
          "receiverWalletId": "9354185507",
          "amount": 50000.00,
          "status": "PENDING",
          "idempotencyRef": "12:3:payout",
          "providerRef": null
        },
        "contributions": [
          {
            "contributionId": 101,
            "memberWalletId": "9354185507",
            "amount": 10000.00,
            "status": "FAILED",
            "idempotencyRef": "12:3:9354185507",
            "providerRef": null
          }
        ]
      }
    ],
    "actionHints": [
      "Use retry-failed only after validating the failed debit/credit did not already settle downstream.",
      "If schedulerEligible=false, operations must extend the cycle window or handle settlement manually before expecting the scheduler to pick it up."
    ]
  }
}
```

Retry failed cycle response example:

```json
{
  "statusCode": 200,
  "description": "Group savings failed cycle records requeued successfully.",
  "data": {
    "requeuedContributions": 1,
    "requeuedPayout": false,
    "schedulerEligible": true,
    "note": "Failed records were reset to PENDING. Confirm downstream ledger state before retrying to avoid duplicate debit or credit."
  }
}
```

Delete request is proxied to transactions service and follows the existing transaction service request shape.

Deletion request:

```json
{
  "reason": "Duplicate test group created during migration",
  "expectedStatus": "DRAFT"
}
```

---

# Storage / Campaign Assets

| Method | Gateway path | Purpose |
| --- | --- | --- |
| `POST` | `/bo/backoffice/storage/uploadSlide` | Upload slide file, multipart |
| `POST` | `/bo/backoffice/storage/uploadPicture` | Upload picture by JSON/base64-style payload |
| `GET` | `/bo/backoffice/storage/slides` | List slides |
| `DELETE` | `/bo/backoffice/storage/slides?objectName=<objectName>` | Delete one slide |
| `DELETE` | `/bo/backoffice/storage/slides/all` | Delete all slides |

---

# FX Peer / Interbank Utility

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/bo/backoffice/interbank/name-enquiry` | Typed interbank name enquiry. Requires `interbank.nameEnquiry.execute` or privileged role. |
| `PATCH` | `/bo/backoffice/fxpeer/offers/{offerId}` | Typed FxPeer offer update alias. Body contains editable offer fields. |

Interbank name enquiry request:

```json
{
  "bankCode": "058",
  "accountNumber": "0123456789",
  "country": "NG"
}
```

FxPeer offer update request:

```json
{
  "rate": 1520.25,
  "availableAmount": 5000,
  "minAmount": 100,
  "maxAmount": 10000,
  "status": "ACTIVE",
  "active": true,
  "sourceCurrency": "CAD",
  "targetCurrency": "NGN",
  "reason": "Corrected erroneous rate after customer verification"
}
```

`reason` is required for FX offer updates and group deletion requests.


| Method | Gateway path | Purpose |
| --- | --- | --- |
| `POST` | `/bo/backoffice/fxpeer/offers/update` | Update FX peer offer |
| `POST` | `/bo/backoffice/interbank/name-enquiry` | Interbank name enquiry |

---

# P2P FX Operations

These endpoints are secured by the backoffice JWT. They bridge existing FXPeer service contracts so FE does not call mobile/customer FXPeer APIs directly.

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/bo/backoffice/p2p-fx/market/offers?ccySell=CAD&ccyRecv=NGN&page=0&size=20&sort=bestRate` | Browse market offers with filters and seller stats where available |
| `GET` | `/bo/backoffice/p2p-fx/seller-offers?sellerId=123&status=ACTIVE&page=0&size=20` | List one seller's offers for admin oversight |
| `GET` | `/bo/backoffice/p2p-fx/seller-offers/{offerId}?sellerId=123` | Get one seller offer |
| `PATCH` | `/bo/backoffice/p2p-fx/seller-offers/{offerId}/rate?sellerId=123&rate=1520.25&reason=Corrected%20rate` | Admin rate intervention; reason is required |
| `POST` | `/bo/backoffice/p2p-fx/seller-offers/{offerId}/cancel?sellerId=123&reason=Compliance%20hold` | Admin offer cancellation; reason is required |
| `POST` | `/bo/backoffice/p2p-fx/orders/{orderId}/escrow/init?reason=Manual%20ops%20init` | Initialize escrow for an order |
| `GET` | `/bo/backoffice/p2p-fx/escrows/{escrowId}` | Get escrow detail |
| `POST` | `/bo/backoffice/p2p-fx/escrows/{escrowId}/fund/buyer?reason=Manual%20funding` | Fund buyer escrow leg. Requires `Idempotency-Key` header |
| `POST` | `/bo/backoffice/p2p-fx/escrows/{escrowId}/fund/seller?reason=Manual%20funding` | Fund seller escrow leg. Requires `Idempotency-Key` header |
| `POST` | `/bo/backoffice/p2p-fx/escrows/{escrowId}/release/buyer?reason=Dispute%20resolved` | Release buyer escrow leg |
| `POST` | `/bo/backoffice/p2p-fx/escrows/{escrowId}/release/seller?reason=Trade%20completed` | Release seller escrow leg |
| `GET` | `/bo/backoffice/p2p-fx/sellers/{sellerId}/ratings?page=0&size=20` | List seller ratings |
| `GET` | `/bo/backoffice/p2p-fx/sellers/{sellerId}/stats` | Get seller trade/rating stats |
| `GET` | `/bo/backoffice/p2p-fx/orders/{orderId}/receipt/buyer` | View buyer receipt HTML |
| `GET` | `/bo/backoffice/p2p-fx/orders/{orderId}/receipt/seller` | View seller receipt HTML |

Notes:

- Escrow funding/release actions are sensitive money-movement operations. FE must capture a clear `reason` and send a unique `Idempotency-Key` where required.
- These endpoints expose existing downstream capabilities. Full dispute workflow, AML flags, corridor controls, reconciliation workspace, and FX reports are still production-hardening items.

---

# VAS / Sochitel Operations

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/bo/backoffice/fxpeer/services/featured` | List featured VAS services |
| `GET` | `/bo/backoffice/fxpeer/services/categories` | List VAS categories |
| `POST` | `/bo/backoffice/fxpeer/services/products` | Lookup VAS products |
| `POST` | `/bo/backoffice/fxpeer/services/products/by-category` | Lookup VAS products by category |
| `POST` | `/bo/backoffice/fxpeer/services/products/by-country` | Lookup VAS products by country |
| `GET` | `/bo/backoffice/fxpeer/services/airtime-reversals/summary` | Airtime reversal summary |
| `GET` | `/bo/backoffice/fxpeer/services/airtime-reversals?status=PENDING` | Airtime reversal cases |
| `POST` | `/bo/backoffice/reversals/cases/FXPEER_AIRTIME/{processId}/manual-request` | Submit airtime reversal for maker-checker approval |

VAS product lookup request bodies:

`POST /bo/backoffice/fxpeer/services/products`

```json
{
  "currencyCode": "NGN"
}
```

`countryCode` is also accepted as an alias for `currencyCode` for FE compatibility:

```json
{
  "countryCode": "NGN"
}
```

`POST /bo/backoffice/fxpeer/services/products/by-category`

```json
{
  "currencyCode": "NGN",
  "categoryId": "AIRTIME"
}
```

`POST /bo/backoffice/fxpeer/services/products/by-country`

```json
{
  "currencyCode": "NGN"
}
```

`countryCode` is also accepted as an alias for `currencyCode`.

Notes:

- Backoffice now has provider/catalog visibility wrappers and reversal retry support.
- Provider reconciliation, duplicate-detection dashboard, refund workflow, and full VAS reporting still need downstream support or new service workflow design.

---

# Reports

Report/export endpoints are contract-ready for FE. In this pilot build jobs are completed immediately and held in service memory; persistent/background export storage can be added later without changing the FE contract.

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/bo/backoffice/reports/catalog` | List report definitions and supported filters. |
| `POST` | `/bo/backoffice/reports/jobs` | Create an export job. |
| `GET` | `/bo/backoffice/reports/jobs` | List export jobs. |
| `GET` | `/bo/backoffice/reports/jobs/{jobId}` | Get export job status. |
| `GET` | `/bo/backoffice/reports/jobs/{jobId}/download` | Download job CSV. |
| `GET` | `/bo/backoffice/reports/schedules` | List report schedules. |
| `POST` | `/bo/backoffice/reports/schedules` | Create a report schedule. |
| `DELETE` | `/bo/backoffice/reports/schedules/{scheduleId}` | Delete a report schedule. |
| `POST` | `/bo/backoffice/audit/export-jobs` | Create an audit export job. |
| `GET` | `/bo/backoffice/audit/export-jobs` | List audit export jobs. |
| `GET` | `/bo/backoffice/audit/export-jobs/{jobId}` | Get audit export job status. |
| `GET` | `/bo/backoffice/audit/export-jobs/{jobId}/download` | Download audit export CSV. |

Sample create report job request:

```json
{
  "reportCode": "audit-log",
  "format": "CSV",
  "parameters": {},
  "filters": {
    "fromDate": "2026-07-01",
    "toDate": "2026-07-16"
  }
}
```

Sample job response:

```json
{
  "jobId": "bo-report-abc123def456",
  "reportCode": "audit-log",
  "status": "READY",
  "format": "CSV",
  "parameters": {},
  "filters": {
    "fromDate": "2026-07-01",
    "toDate": "2026-07-16"
  },
  "requestedByAdminId": 1,
  "requestedAt": "2026-07-16T08:30:00Z",
  "completedAt": "2026-07-16T08:30:00Z",
  "downloadUrl": "/bo/backoffice/reports/jobs/bo-report-abc123def456/download"
}
```

Sample create schedule request:

```json
{
  "reportCode": "audit-log",
  "frequency": "DAILY",
  "format": "CSV",
  "filters": {
    "fromDate": "2026-07-01"
  },
  "parameters": {},
  "recipients": ["ops@finacialwealth.com"],
  "active": true
}
```


| Method | Gateway path | Purpose |
| --- | --- | --- |
| `GET` | `/bo/reports/sample-transactions.csv` | Sample CSV export |
| `GET` | `/bo/backoffice/investments/products/export.csv` | Investment products CSV export |

---

# Global Search, Saved Views, And Ops Health

These endpoints close the FE dependency for cross-module search, per-admin saved filters/views, and an operations dependency-health screen. They all require a valid backoffice JWT.

## Global Dashboard

| Method | Gateway path | Purpose |
| --- | --- | --- |
| `GET` | `/bo/backoffice/dashboard?range=TODAY` | Role-sensitive landing dashboard summary. |

Optional query params: `range`, `fromDate`, `toDate`.

The endpoint composes local approval/admin/notification counts with Profiling customer count, FxPeer investment performance, Transactions NGN cumulative balance summary, and dependency health. Each section carries its own `status`, so a temporary downstream issue should not blank the whole dashboard.

Sample response:

```json
{
  "status": 200,
  "description": "Backoffice dashboard retrieved successfully",
  "generatedAt": "2026-08-11T10:00:00Z",
  "data": {
    "range": "TODAY",
    "fromDate": null,
    "toDate": null,
    "summaryCards": [
      {
        "key": "pendingApprovals",
        "label": "Pending approvals",
        "value": 12,
        "valueType": "count",
        "deepLink": "/approvals",
        "status": "AVAILABLE"
      },
      {
        "key": "unreadNotifications",
        "label": "Unread notifications",
        "value": 3,
        "valueType": "count",
        "deepLink": "/notifications",
        "status": "AVAILABLE"
      },
      {
        "key": "ngnCumulativeBalance",
        "label": "NGN cumulative balance",
        "value": 12500000,
        "valueType": "amount",
        "deepLink": "/operations/transactions",
        "status": "AVAILABLE"
      }
    ],
    "sections": {
      "approvals": {
        "name": "approvals",
        "status": "AVAILABLE",
        "data": {"pendingCount": 12},
        "durationMs": 5
      },
      "ngnBalances": {
        "name": "ngnBalances",
        "status": "AVAILABLE",
        "data": {
          "status": 200,
          "description": "NGN cumulative account balance summary retrieved successfully",
          "data": {
            "currency": "NGN",
            "accountCount": 240,
            "cumulativeBalance": 12500000
          }
        },
        "durationMs": 210
      }
    },
    "quickLinks": [
      {"label": "Approvals", "path": "/approvals"},
      {"label": "NGN Transactions", "path": "/operations/transactions"}
    ]
  }
}
```

## Global Search

| Method | Gateway path | Purpose |
| --- | --- | --- |
| `GET` | `/bo/backoffice/search?q=MF_BAL001&types=PRODUCT,APPROVAL&page=0&size=10` | Search across customers, products, approvals, and group savings. |

Supported `types`: `CUSTOMER`, `PRODUCT`, `APPROVAL`, `GROUP`. If omitted, all supported types are queried.

Sample response:

```json
{
  "query": "MF_BAL001",
  "types": ["PRODUCT", "APPROVAL"],
  "page": 0,
  "size": 10,
  "sections": [
    {
      "type": "PRODUCT",
      "status": "AVAILABLE",
      "data": {
        "statusCode": 200,
        "data": []
      },
      "durationMs": 47
    },
    {
      "type": "APPROVAL",
      "status": "AVAILABLE",
      "data": {
        "content": [
          {
            "id": 31,
            "module": "INVESTMENT",
            "subModule": "PRODUCT",
            "entityType": "FXPEER_INVESTMENT_PRODUCT",
            "entityRef": "INVESTMENT_PRODUCT_UPDATE:MF_BAL001",
            "actionType": "UPDATE",
            "status": "PENDING"
          }
        ],
        "page": 0,
        "size": 10
      },
      "durationMs": 12
    }
  ]
}
```

If one downstream dependency is unavailable, that section returns `status: "UNAVAILABLE"` while the rest of the search still completes.

## Saved Views

| Method | Gateway path | Purpose |
| --- | --- | --- |
| `GET` | `/bo/backoffice/saved-views?moduleKey=INVESTMENTS` | List the logged-in admin's saved views for a module. |
| `POST` | `/bo/backoffice/saved-views` | Save a filter/table view for the logged-in admin. |
| `POST` | `/bo/backoffice/saved-views/{viewId}/default` | Mark a saved view as the module default. |
| `DELETE` | `/bo/backoffice/saved-views/{viewId}` | Delete a saved view owned by the logged-in admin. |

Sample create request:

```json
{
  "moduleKey": "INVESTMENTS",
  "name": "Pending Liquidations",
  "defaultView": true,
  "filters": {
    "status": "LIQUIDATION_PENDING_APPROVAL",
    "productCode": "MF_BAL001",
    "columns": ["customerName", "amount", "status", "createdAt"]
  }
}
```

Sample response:

```json
{
  "id": 7,
  "moduleKey": "INVESTMENTS",
  "name": "Pending Liquidations",
  "filters": {
    "status": "LIQUIDATION_PENDING_APPROVAL",
    "productCode": "MF_BAL001"
  },
  "defaultView": true,
  "createdAt": "2026-07-16T09:30:00Z",
  "updatedAt": "2026-07-16T09:30:00Z"
}
```

## Integration Health

| Method | Gateway path | Purpose |
| --- | --- | --- |
| `GET` | `/bo/backoffice/system/integration-health` | Probe backoffice DB, profiling, FXPeer, and transactions dependencies. |

Sample response:

```json
{
  "status": "DEGRADED",
  "checkedAt": "2026-07-16T09:35:00Z",
  "dependencies": [
    {
      "name": "BACKOFFICE_DB",
      "status": "UP",
      "details": {
        "adminUsers": 3
      },
      "durationMs": 3
    },
    {
      "name": "FXPEER_EXCHANGE_SERVICE",
      "status": "DOWN",
      "error": "FeignException",
      "message": "Dependency probe failed.",
      "durationMs": 1012
    }
  ]
}
```

---

# Deployment Notes for FE

- Product create/update now returns `202` when approval is enabled. Do not expect the product list to update until checker approval is completed.
- `GET /investments/products/{productCode}/history` now includes `approvalHistory` when maker-checker history exists.
- Refresh-token calls rotate the refresh token; FE must persist the new refresh token returned by `/bo/auth/refresh`.
- Use `/backoffice/profiling/{id}/customer-360` for customer detail where possible.
- Use `/backoffice/notifications/unread-count` for the top-bar badge and poll/list `/backoffice/notifications`.
- Use `/backoffice/reversals` for unified reversal screens instead of product-specific wrappers.
- Use `/backoffice/search` for the global search bar and `/backoffice/saved-views` for admin-owned table filters/views.
- Use `/backoffice/system/integration-health` for the operations dependency health screen before raising downstream tickets.
- `app_config` edits should show “pending approval” if the endpoint returns `202`.
