# finacial-wealth-backoffice-service

## Frontend API Handoff

This document is the frontend-facing handoff for the backoffice service endpoints currently exposed for:

- authentication
- investments and liquidation queues
- customer 360 views
- approvals
- roles and permissions

Swagger should still be used as the live reference for trying requests, but this README is the quicker screen-by-screen integration guide.

## Base Notes

- Public production base URL: `https://finacialwealth.com`.
- Public gateway prefix: use `/bo` for frontend calls. The gateway may strip `/bo` before forwarding to the service.
- Direct service/local paths may appear without `/bo`; the examples below use public production paths.
- Auth style: JWT Bearer token in `Authorization: Bearer <accessToken>`.
- Swagger/OpenAPI: available on the service and now grouped by `Investments`, `Customers`, `Approvals`, `Roles`, and `Permissions`.
- Backend enforcement: the backend is the source of truth for authorization.
- Frontend gating: you can optionally decode the JWT and inspect the `permissions` claim for UI visibility, but always handle `403` from the API.

## Seed Login

- Email: `superadmin@finacialwealth.com`
- Password: `Password123!`

## Auth Flow

Auth routes are available under both `/bo/auth` and `/auth`. Use one base consistently from the frontend. Examples below use `/bo/auth`.

### 1. Login

`POST /bo/auth/login`

Request body:

```json
{
  "email": "superadmin@finacialwealth.com",
  "password": "Password123!"
}
```

Possible response A: MFA not required

```json
{
  "accessToken": "jwt-access-token",
  "refreshToken": "refresh-token",
  "emailAddress": "superadmin@finacialwealth.com",
  "fullName": "Super Admin",
  "userRole": "SUPER_ADMIN",
  "adminId": 1
}
```

Possible response B: MFA required

```json
{
  "status": "MFA_REQUIRED",
  "mfaToken": "challenge-id"
}
```

Use the returned `mfaToken` value as the `challengeId` field in the next `POST /bo/auth/mfa/verify` request.

Possible response C: MFA setup required

```json
{
  "status": "MFA_SETUP_REQUIRED",
  "mfaToken": null
}
```

### 2. Verify MFA

`POST /bo/auth/mfa/verify`

Request body:

```json
{
  "challengeId": "challenge-id",
  "code": "123456"
}
```

Response:

```json
{
  "accessToken": "jwt-access-token",
  "refreshToken": "refresh-token",
  "emailAddress": "superadmin@finacialwealth.com",
  "fullName": "Super Admin",
  "userRole": "SUPER_ADMIN",
  "adminId": 1
}
```

### 3. Change Password

`POST /bo/auth/password/change`

Headers:

```text
Authorization: Bearer <accessToken>
```

Request body:

```json
{
  "currentPassword": "Password123!",
  "newPassword": "Password@1234",
  "confirmPassword": "Password@1234"
}
```

Response: `200 OK` with empty body.

Password rules for `newPassword`:
- minimum 12 characters
- at least 1 uppercase letter
- at least 1 lowercase letter
- at least 1 number
- at least 1 special character
- no spaces

### 4. Start Password Recovery

`POST /bo/auth/password/recovery/start`

Request body:

```json
{
  "email": "superadmin@finacialwealth.com"
}
```

Possible response A: MFA-based recovery can proceed

```json
{
  "status": "MFA_REQUIRED",
  "challengeId": "4d20f5d8f24b6c0f3f4d8f7a7f2a1e0d...",
  "emailAddress": "s***n@finacialwealth.com",
  "message": "Enter the 6-digit code from your authenticator app to complete password recovery."
}
```

Possible response B: account needs super-admin help

```json
{
  "status": "CONTACT_SUPER_ADMIN",
  "challengeId": null,
  "emailAddress": "s***n@finacialwealth.com",
  "message": "MFA recovery is not available for this account yet. Contact a super admin for a manual reset."
}
```

### 5. Complete Password Recovery

`POST /bo/auth/password/recovery/complete`

Request body:

```json
{
  "challengeId": "4d20f5d8f24b6c0f3f4d8f7a7f2a1e0d...",
  "code": "123456",
  "newPassword": "Password@1234",
  "confirmPassword": "Password@1234"
}
```

Response: `200 OK` with empty body. On success the account is unlocked and existing refresh tokens are revoked. The same password rules listed above for change-password also apply here.

### 6. Refresh Token

`POST /bo/auth/refresh?refreshToken=<token>`

Response shape is the same as login success.

### 7. Logout

`POST /bo/auth/logout?refreshToken=<token>`

Response: `200 OK` with empty body.

## Common Response Shapes

The service currently uses more than one response envelope.

### A. Plain DTO responses

Used mainly by auth and role endpoints.

Example:

```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "emailAddress": "user@company.com",
  "fullName": "Jane Doe",
  "userRole": "OPERATIONS",
  "adminId": 12
}
```

### B. `ApiResponse<T>`

Used by profiling-backed customer endpoints.

Shape:

```json
{
  "code": "00",
  "message": "Successful",
  "data": {}
}
```

### C. Exchange-style queue responses

Used by investment queue and customer investment endpoints proxied from exchange.

Typical shape:

```json
{
  "statusCode": 200,
  "description": "Fetched successfully",
  "data": {
    "content": [],
    "page": 0,
    "size": 20,
    "totalElements": 0,
    "totalPages": 0
  }
}
```

### D. Approval responses

Used by `/bo/backoffice/approvals`.

List shape:

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0
}
```

Detail shape:

```json
{
  "id": 15,
  "module": "INVESTMENT",
  "subModule": "LIQUIDATION",
  "entityType": "FXPEER_LIQUIDATION",
  "entityRef": "LQ-123",
  "actionType": "APPROVE",
  "status": "PENDING",
  "makerAdminId": null,
  "checkerAdminId": null,
  "requesterEmail": "customer@email.com",
  "rejectionReason": null,
  "remediationNotes": null,
  "createdAt": "2026-04-15T10:30:00Z",
  "updatedAt": "2026-04-15T10:30:00Z",
  "submittedAt": "2026-04-15T10:30:00Z",
  "approvedAt": null,
  "rejectedAt": null,
  "resubmittedAt": null,
  "payload": {},
  "events": []
}
```

## Permissions and UI Access

Important permission codes currently in use:

- `approval.inbox.view`
- `investment.liquidation.approve`
- `investment.liquidation.remediate`
- `investment.order.view`
- `investment.liquidation.view`
- `customer.profile.view`
- `role.manage`

Notes:

- `SUPER_ADMIN` can access everything.
- Some older endpoints still use role checks like `SUPER_ADMIN`, `ADMIN`, `OPERATIONS`, `FINANCE`.
- New approval and role endpoints use fine-grained permissions.

## Screen Map

### 1. Investment Products

#### List products

`GET /bo/backoffice/investments/products`

Notes:

- Requires a valid bearer token.
- Returns the product catalog from the exchange service.

#### Create product

`POST /bo/backoffice/investments/products`

Request body example:

```json
{
  "productCode": "MMF003",
  "name": "Prime Money Market Fund",
  "type": "MONEY_MARKET",
  "currency": "NGN",
  "minimumInvestmentAmount": 5000,
  "valuationMethod": "RATE",
  "yieldPa": 10.0,
  "liquidationFeeAppliedTo": "TOTAL_VALUE",
  "liquidationFeeType": "RATE",
  "liquidationFeeRate": 1.5,
  "minLiquidationFee": 1.0,
  "subscriptionCutOffTime": "00:00:00"
}
```

#### Update product

`PUT /bo/backoffice/investments/products/{productCode}`

Use the same body as create. The path `productCode` wins over any body value.

#### Export products

`GET /bo/backoffice/investments/products/export.csv`

Response: CSV file download.

### 2. Liquidation Queue

#### Active liquidation table

`GET /bo/backoffice/investments/liquidations`

Query params:

- `status`
- `productCode`
- `fromDate` in `YYYY-MM-DD`
- `toDate` in `YYYY-MM-DD`
- `page`
- `size`

Row fields currently returned:

- `requester`
- `orderRef`
- `parentOrderRef`
- `productCode`
- `productName`
- `amount`
- `currency`
- `requestedAt`
- `updatedAt`
- `status`
- `liquidationType`
- `fees`
- `netAmount`

Typical statuses for the active queue:

- `LIQUIDATION_PENDING_APPROVAL`
- `LIQUIDATION_PROCESSING`

Typical `liquidationType` values:

- `FULL`
- `PARTIAL`

#### Historical liquidation table

`GET /bo/backoffice/investments/liquidations/history`

Same query params and row shape as the active table.

Typical history statuses:

- `SETTLED`
- `LIQUIDATION_FAILED`
- `CANCELLED`
- `FAILED`

### 3. Investment and Topup Queue

#### Orders table

`GET /bo/backoffice/investments/orders`

Query params:

- `type`
- `status`
- `productCode`
- `cutoffBucket`
- `fromDate` in `YYYY-MM-DD`
- `toDate` in `YYYY-MM-DD`
- `page`
- `size`

Supported `type` values:

- `SUBSCRIPTION`
- `TOPUP`

Supported `cutoffBucket` values:

- `BEFORE_CUTOFF`
- `AFTER_CUTOFF_NEXT_DAY`

Row fields currently returned:

- `requester`
- `orderRef`
- `parentOrderRef`
- `type`
- `productCode`
- `productName`
- `amount`
- `currency`
- `requestedAt`
- `updatedAt`
- `status`
- `cutoffBucket`
- `effectiveBusinessDate`

Frontend note:

- Use `cutoffBucket` to separate same-day eligible orders from next-business-day queue items.

### 3b. Performance Dashboard

#### Performance dashboard

`GET /bo/backoffice/investments/performance`

Query params:

- `productCode`
- `fromDate` in `YYYY-MM-DD`
- `toDate` in `YYYY-MM-DD`

Response shape:

```json
{
  "statusCode": 200,
  "description": "Performance dashboard fetched successfully.",
  "data": {
    "filters": {
      "productCode": "MMF003",
      "fromDate": "2026-02-01",
      "toDate": "2026-04-25"
    },
    "summary": {
      "aum": 21000000,
      "startAum": 18000000,
      "netChange": 3000000,
      "netChangePct": 16.67
    },
    "aumTrend": [
      { "date": "2026-02-01", "aum": 18000000 }
    ],
    "productSnapshots": [
      {
        "productCode": "MMF003",
        "productName": "Prime Money Market Fund",
        "startAum": 18000000,
        "aum": 21000000,
        "netChange": 3000000,
        "netChangePct": 16.67,
        "yieldPa": 10.0,
        "yieldYtd": 3.2
      }
    ],
    "recentActivity": [
      {
        "activityType": "INVESTMENT_TOPUP",
        "productCode": "MMF003",
        "productName": "Prime Money Market Fund",
        "orderRef": "TOPUP-123",
        "amount": 50000,
        "description": "Investment topup successful for product: TOPUP-123",
        "createdAt": "2026-04-20T10:30:00"
      }
    ],
    "products": [
      {
        "productCode": "MMF003",
        "productName": "Prime Money Market Fund",
        "currency": "NGN",
        "yieldPa": 10.0,
        "yieldYtd": 3.2
      }
    ]
  }
}
```

Frontend notes:

- Leave `productCode` empty to show all products.
- Leave dates empty to default to the latest three-month window.
- Use the `products` array to populate the product filter dropdown.
- Use `aumTrend` for the line chart and `recentActivity` for the activity card/list.

### 3c. Oversight Dashboard

#### Oversight dashboard

`GET /bo/backoffice/investments/oversight`

Query params:

- `productCode`
- `fromDate` in `YYYY-MM-DD`
- `toDate` in `YYYY-MM-DD`
- `actionType` one of `ALLOCATION`, `TOPUP`, `LIQUIDATION`
- `status` one of `EXECUTED`, `PENDING`, `FAILED`, `CANCELLED`
- `size` max number of rows, default `20`, max `200`

Response shape:

```json
{
  "statusCode": 200,
  "description": "Investment oversight dashboard fetched successfully.",
  "data": {
    "filters": {
      "productCode": "MMF003",
      "fromDate": "2026-04-01",
      "toDate": "2026-04-25",
      "actionType": null,
      "status": "EXECUTED",
      "size": 20
    },
    "summary": {
      "fundsUnderInvestment": 125000000.00,
      "interestAccrued": 6750000.00,
      "activePositions": 14,
      "actionCount": 20
    },
    "actions": [
      {
        "reference": "OV-1001",
        "productCode": "MMF003",
        "productName": "Prime Money Market Fund",
        "type": "ALLOCATION",
        "rawType": "SUBSCRIPTION",
        "amount": 10000000.00,
        "returns": 350000.00,
        "date": "2026-04-25T10:15:00Z",
        "status": "EXECUTED",
        "rawStatus": "SETTLED",
        "walletId": "1590746834",
        "customerEmail": "customer@example.com"
      }
    ],
    "products": [
      {
        "productCode": "MMF003",
        "name": "Prime Money Market Fund",
        "currency": "NGN"
      }
    ],
    "actionTypes": ["ALL", "ALLOCATION", "TOPUP", "LIQUIDATION"],
    "statuses": ["ALL", "EXECUTED", "PENDING", "FAILED", "CANCELLED"]
  }
}
```

Frontend notes:

- Use `summary.fundsUnderInvestment` for the first headline card.
- Use `summary.interestAccrued` for the second headline card.
- Use `actions` directly for the investment actions table.
- Use `products`, `actionTypes`, and `statuses` to populate dropdowns without hardcoding values.

### 4. Customer Module

#### List customers

`GET /bo/backoffice/profiling?page=0&size=20&sort=id,desc`

Response envelope:

```json
{
  "code": "00",
  "message": "Successful",
  "data": {
    "content": []
  }
}
```

Customer object includes fields such as:

- `id`
- `customerId`
- `fullName`
- `firstName`
- `lastName`
- `email`
- `phoneNumber`
- `accountNumber`
- `walletId`
- `isUserBlocked`
- `created`

#### Customer profile

`GET /bo/backoffice/profiling/{id}`

Path note:

- Use the `id` returned by the list customers endpoint.
- Do not use `customerId`, `uuid`, or `walletId` for this route.

#### Customer investment summary

`GET /bo/backoffice/profiling/{id}/investment-summary`

Response shape:

```json
{
  "customer": {},
  "orders": {},
  "liquidations": {},
  "positions": {}
}
```

This is the best endpoint for the main customer detail landing page.

#### Customer orders

`GET /bo/backoffice/profiling/{id}/orders`

Query params:

- `type`
- `status`
- `page`
- `size`

#### Customer liquidations

`GET /bo/backoffice/profiling/{id}/liquidations`

Query params:

- `status`
- `page`
- `size`

#### Customer positions

`GET /bo/backoffice/profiling/{id}/positions`

Query params:

- `page`
- `size`

Position row fields currently returned:

- `requester`
- `orderRef`
- `productCode`
- `productName`
- `walletId`
- `units`
- `investedAmount`
- `currentValue`
- `accruedInterest`
- `totalAccruedInterest`
- `reservedLiquidationAmount`
- `status`
- `interestStartDate`
- `createdAt`
- `updatedAt`
- `maturityAt`
- `settlementAt`

#### Block customer

`PATCH /bo/backoffice/profiling/{id}/block`

#### Unblock customer

`PATCH /bo/backoffice/profiling/{id}/unblock`

Use the same request body already used by profiling block/unblock screens.

### 5. Approval Module

These are the maker-checker endpoints used for liquidation approvals and manual reversal approvals.

#### List approval inbox

`GET /bo/backoffice/approvals`

Query params:

- `status`
- `page`
- `size`

If `status` is omitted, the backend currently returns:

- `PENDING`
- `IN_REMEDIATION`
- `RESUBMITTED`

#### Get approval detail

`GET /bo/backoffice/approvals/{approvalId}`

Use this for the approval detail drawer or page.

#### Approve

`POST /bo/backoffice/approvals/{approvalId}/approve`

No request body required.

#### Reject

`POST /bo/backoffice/approvals/{approvalId}/reject`

Request body:

```json
{
  "reason": "Amount exceeds threshold for same-day processing"
}
```

#### Resubmit after remediation

`POST /bo/backoffice/approvals/{approvalId}/resubmit`

Request body:

```json
{
  "notes": "Customer details validated and discrepancy resolved"
}
```

#### Approval Statuses

Current approval statuses:

- `PENDING`
- `IN_REMEDIATION`
- `RESUBMITTED`
- `APPROVED`

Notes for frontend behavior:

- `PENDING` and `RESUBMITTED` are decision-ready states.
- `IN_REMEDIATION` means a checker rejected the item and it is waiting for maker-side correction and resubmission.
- `APPROVED` is terminal for the current slice.

#### Approval Event Types

You may see the following event types in the detail response:

- `SYNCED`
- `REQUESTED`
- `REJECTED`
- `RESUBMITTED`
- `APPROVED`

### 6. Reversal Exception Module

Backoffice now exposes a unified reversal exception queue across `fxpeer` airtime reversals and `transactions` debit-reversal cases. Manual remediation is approval-gated and should only be used for cases already in `FAILED` or `PENDING`.

#### Reversal summary

`GET /bo/backoffice/reversals/summary`

This returns combined totals plus per-source totals for:

- `FXPEER_AIRTIME`
- `TRANSACTIONS`

#### Reversal cases

`GET /bo/backoffice/reversals/cases`

Query params:

- `source` optional: `FXPEER_AIRTIME` or `TRANSACTIONS`
- `status` optional: `PENDING`, `FAILED`, `SUCCESS`
- `page`
- `size`

Normalized response fields per row:

- `source`
- `caseRef`
- `status`
- `requestedAt`
- `completedAt`
- `retryCount`
- `lastError`
- `serviceType`
- `operator`
- `product`
- `providerError`
- `legs`

#### Submit manual reversal request

`POST /bo/backoffice/reversals/cases/{source}/{caseRef}/manual-request`

Request body:

```json
{
  "notes": "Auto-reversal failed twice after debit success and fulfilment failure. Requesting checker approval for manual retry."
}
```

Workflow notes:

- maker creates the request through `/backoffice/reversals/.../manual-request`
- checker reviews and approves through the normal `/backoffice/approvals/{approvalId}/approve` endpoint
- rejection sends the item into remediation, and the maker can resubmit through `/backoffice/approvals/{approvalId}/resubmit`
- approval triggers the owning source service retry endpoint; backoffice does not move money directly

Permissions involved:

- `reversal.exception.view`
- `reversal.manual.request`
- `reversal.manual.approve`
- `reversal.manual.remediate`

### 7. Role and Permission Management

#### List permission catalog

`GET /bo/admin/permissions`

Gateway note: this is also mapped as `/admin/permissions` inside the service because production may strip the `/bo` prefix before forwarding.

Response item shape:

```json
{
  "id": 1,
  "module": "INVESTMENT",
  "subModule": "LIQUIDATION",
  "action": "APPROVE",
  "code": "investment.liquidation.approve",
  "description": "Approve liquidation requests"
}
```

#### List roles

`GET /bo/admin/roles`

Gateway note: this is also mapped as `/admin/roles` inside the service because production may strip the `/bo` prefix before forwarding.

Response item shape:

```json
{
  "id": 1,
  "name": "OPERATIONS",
  "permissionCodes": [
    "approval.inbox.view",
    "investment.liquidation.approve"
  ]
}
```

#### Create role

`POST /bo/admin/roles`

Request body:

```json
{
  "name": "INVESTMENT_CHECKER",
  "permissionCodes": [
    "approval.inbox.view",
    "investment.liquidation.approve"
  ]
}
```

#### Replace role permissions

`PUT /bo/admin/roles/{roleId}/permissions`

Request body:

```json
{
  "permissionCodes": [
    "approval.inbox.view",
    "investment.liquidation.approve",
    "investment.order.view"
  ]
}
```

### 7. Admin User Management

Admin user routes are available under `/bo/admin-users`, `/admin-users`, and `/backoffice/admin-users`. Prefer `/bo/admin-users` for production frontend work because the public gateway already routes the `/bo` prefix. Inside the service, the gateway may strip `/bo`, so `/admin-users` is also mapped.

#### Create admin user

`POST /bo/admin-users`

Request body:

```json
{
  "email": "checker@example.com",
  "fullName": "Investment Checker",
  "password": "Password@123",
  "confirmPassword": "Password@123",
  "roles": [
    {
      "id": 4,
      "name": "INVESTMENT_CHECKER"
    }
  ]
}
```

#### Update admin user

`PATCH /bo/admin-users/{adminId}`

Request body:

```json
{
  "fullName": "Investment Checker",
  "roles": [
    {
      "id": 4,
      "name": "INVESTMENT_CHECKER"
    }
  ]
}
```

#### Activate, suspend, and reset password

- `POST /bo/admin-users/{adminId}/activate`
- `POST /bo/admin-users/{adminId}/suspend`
- `POST /bo/admin-users/{adminId}/password-reset`

Password reset response:

Notes:
- this endpoint is for `SUPER_ADMIN` only
- it unlocks the target account and clears failed-attempt lock state
- it revokes existing refresh tokens for that admin user
- FE should display the returned temporary password once and instruct ops to share it over a secure channel


```json
{
  "adminId": 7,
  "email": "checker@example.com",
  "temporaryPassword": "T3mp!Passw0rd#",
  "message": "Temporary password generated successfully. Share it with the admin user over a secure channel and require them to change it immediately after login."
}
```

#### List and view admin users

- No `boAdminUserId` query param is required. The backend derives the acting admin from the bearer token.
- `GET /bo/admin-users/admins?page=0&size=20&q=checker`
- `GET /bo/admin-users/admins/{adminId}`

## Suggested Screen-to-Endpoint Mapping

### Login Page

- `POST /bo/auth/login`
- if response status is `MFA_REQUIRED`, show MFA step and call `POST /bo/auth/mfa/verify`
- signed-in password change: `POST /bo/auth/password/change`
- forgot-password start: `POST /bo/auth/password/recovery/start`
- forgot-password complete: `POST /bo/auth/password/recovery/complete`

### Dashboard Counters

- liquidation pending count: `GET /bo/backoffice/investments/liquidations`
- approval inbox count: `GET /bo/backoffice/approvals`

### Liquidation Queue Screen

- table: `GET /bo/backoffice/investments/liquidations`
- history tab: `GET /bo/backoffice/investments/liquidations/history`
- if using maker-checker flow, actions should go through `/bo/backoffice/approvals/*`

### Transactions / Orders Screen

- table: `GET /bo/backoffice/investments/orders`

### Performance Screen

- dashboard load: `GET /bo/backoffice/investments/performance`
- product filter: pass `productCode`
- date filter: pass `fromDate` and `toDate`

### Oversight Screen

### Group Savings Contribution & Payout Monitoring

`GET /bo/backoffice/group-savings/contribution-payout-monitoring`

Optional query params:
- `period`: `DAILY`, `WEEKLY`, or `MONTHLY`
- `fromDate`: `YYYY-MM-DD`
- `toDate`: `YYYY-MM-DD`
- `groupId`: numeric group savings id

Purpose:
- powers the Group Savings `Contribution & Payout Monitoring` screen
- returns contribution totals, payout totals, chart series, alerts, and group filter options

Example:
```http
GET /bo/backoffice/group-savings/contribution-payout-monitoring?period=DAILY
Authorization: Bearer <token>
```

Response shape:
```json
{
  "statusCode": 200,
  "description": "Contribution and payout monitoring fetched successfully.",
  "data": {
    "filters": {
      "period": "DAILY",
      "fromDate": "2026-04-19",
      "toDate": "2026-04-25",
      "groupId": null
    },
    "summary": {
      "totalContributions": 5242651,
      "totalPayouts": 3301763,
      "netFlow": 1940888,
      "activeGroups": 8,
      "lastUpdatedAt": "2026-04-25T17:45:29Z"
    },
    "trend": [
      {
        "key": "2026-04-19",
        "label": "Sat",
        "periodStart": "2026-04-19",
        "periodEnd": "2026-04-19",
        "contributionAmount": 510000,
        "payoutAmount": 320000,
        "contributionCount": 4,
        "payoutCount": 2
      }
    ],
    "alerts": [
      {
        "level": "HIGH",
        "category": "PAYOUT",
        "status": "FAILED",
        "reference": "TX-9003",
        "message": "Payout TX-9003 failed - retrigger required.",
        "eventAt": "2026-04-25T08:55:00Z",
        "groupId": 412,
        "groupName": "Friday Savers"
      }
    ],
    "groupOptions": [],
    "periodOptions": ["DAILY", "WEEKLY", "MONTHLY"]
  }
}
```

### Group Savings Slot Assignment & Tracking

`GET /bo/backoffice/group-savings/slot-assignment-tracking`

Optional query params:
- `groupId`: numeric group savings id
- `status`: `UPCOMING`, `IN_PROGRESS`, `MISSED`, or `COMPLETED`

Purpose:
- powers the Group Savings `Slot Assignment & Tracking` screen
- returns slot schedule rows, payout history, alerts, and group filter options

Example:
```http
GET /bo/backoffice/group-savings/slot-assignment-tracking?status=UPCOMING
Authorization: Bearer <token>
```

Response shape:
```json
{
  "statusCode": 200,
  "description": "Slot assignment and tracking fetched successfully.",
  "data": {
    "filters": {
      "groupId": null,
      "status": "UPCOMING"
    },
    "slotSchedule": [
      {
        "groupId": 412,
        "groupName": "Friday Savers",
        "slotNumber": 1,
        "memberName": "Ada Lovelace",
        "memberWalletId": "1590746834",
        "payoutDate": "2026-04-28",
        "status": "UPCOMING",
        "cycleStatus": "PENDING",
        "reference": "GS-412-SLOT-1"
      }
    ],
    "payoutHistory": [
      {
        "groupId": 412,
        "groupName": "Friday Savers",
        "memberName": "Ada Lovelace",
        "slotNumber": 1,
        "amount": 250000,
        "date": "2026-04-10T09:15:00Z",
        "status": "PAID",
        "reference": "412:1:payout"
      }
    ],
    "alerts": [
      {
        "level": "HIGH",
        "category": "SLOT",
        "status": "MISSED",
        "reference": "GS-412-SLOT-4",
        "message": "Slot 4 missed contribution. Notify member and reschedule.",
        "eventAt": "2026-04-21T00:00:00Z",
        "groupId": 412,
        "groupName": "Friday Savers",
        "memberName": "Mary Jackson"
      }
    ],
    "groupOptions": [],
    "statusOptions": ["UPCOMING", "IN_PROGRESS", "MISSED", "COMPLETED"]
  }
}
```

### Group Savings Screens

- `Contribution & Payout Monitoring` consumes `GET /bo/backoffice/group-savings/contribution-payout-monitoring`
- `Slot Assignment & Tracking` consumes `GET /bo/backoffice/group-savings/slot-assignment-tracking`

- dashboard load: `GET /bo/backoffice/investments/oversight`
- filters: pass `productCode`, `fromDate`, `toDate`, `actionType`, `status`
- table rows: `data.actions`
- headline cards: `data.summary`

### Customer Detail Screen

- shell load: `GET /bo/backoffice/profiling/{id}/investment-summary`
- optional tab-specific pagination:
  - `/orders`
  - `/liquidations`
  - `/positions`

### Approval Inbox Screen

- list: `GET /bo/backoffice/approvals`
- detail: `GET /bo/backoffice/approvals/{approvalId}`
- approve: `POST /bo/backoffice/approvals/{approvalId}/approve`
- reject: `POST /bo/backoffice/approvals/{approvalId}/reject`
- resubmit: `POST /bo/backoffice/approvals/{approvalId}/resubmit`

### Reversal Exceptions Screen

- summary cards: `GET /bo/backoffice/reversals/summary`
- queue table: `GET /bo/backoffice/reversals/cases`
- source/status filters: pass `source`, `status`, `page`, `size`
- maker action: `POST /bo/backoffice/reversals/cases/{source}/{caseRef}/manual-request`
- checker decisioning still happens in the shared approval inbox

### Role Management Screen

- permission picklist: `GET /bo/admin/permissions`
- role table: `GET /bo/admin/roles`
- create role: `POST /bo/admin/roles`
- edit role permissions: `PUT /bo/admin/roles/{roleId}/permissions`

### Admin User Management Screen

- table: `GET /bo/admin-users/admins`
- detail: `GET /bo/admin-users/admins/{adminId}`
- create user: `POST /bo/admin-users`
- update user: `PATCH /bo/admin-users/{adminId}`
- activate user: `POST /bo/admin-users/{adminId}/activate`
- suspend user: `POST /bo/admin-users/{adminId}/suspend`
- password reset: `POST /bo/admin-users/{adminId}/password-reset`

## Frontend Caveats

- Response envelopes are not yet fully standardized across modules.
- Investment queue and customer investment endpoints are proxied from exchange and use `statusCode` and `description`.
- Profiling endpoints use `code`, `message`, and `data`.
- Approvals return direct objects without a wrapper.
- For now, build response adapters on the frontend rather than assuming one shared envelope.

## Recommended Frontend Utilities

- a single auth client for login, MFA verify, refresh, and logout
- a response normalizer per module:
  - `normalizeApiResponse`
  - `normalizePagedExchangeResponse`
  - `normalizeApprovalPage`
- a permission helper that:
  - decodes JWT `permissions` claim if available
  - still falls back to server-side `403` handling

## Current Priorities Covered by This Handoff

- investment products
- liquidation queue and history
- investment and topup order queue
- customer 360 read views
- approval inbox and remediation flow
- role and permission management

If more modules are added next, this README should be extended in the same screen-first format.
