# Backoffice FE API: Reversals and Referral Programs

This document is the frontend handoff for the Swagger sections shown as `Reversals` and `referral-program-management-controller`.

## Base Path

Use the backoffice service wrapper endpoints.

- Service-local path shown in Swagger: `/backoffice/...`
- Gateway path used in existing backoffice docs: `/bo/backoffice/...`

If the frontend is calling through the gateway, use the `/bo` prefix. If it is calling the backoffice service directly in local/dev Swagger, use the path without `/bo`.

All endpoints require the normal backoffice bearer token:

```http
Authorization: Bearer <backoffice_access_token>
Content-Type: application/json
```

## Response Envelope

Referral-program endpoints return the standard backoffice response envelope:

```json
{
  "statusCode": 200,
  "description": "OK",
  "data": {}
}
```

Reversal endpoints return a map/object response directly from the reversal module.

## Reversals

Reversal APIs monitor reversal exceptions across products through one queue. FE should use `/backoffice/reversals` for airtime, interbank, local wallet transfer, and future product reversal sources. Product-specific endpoints such as `/backoffice/interbank/reversals` are legacy compatibility wrappers. Manual reversal requests are maker-checker gated; approval triggers the owning source service retry endpoint. Backoffice does not move funds directly.

Required permissions:

- View summary/cases: `reversal.exception.view` or `ROLE_SUPER_ADMIN`
- Submit manual request: `reversal.manual.request` or `ROLE_SUPER_ADMIN`

### Get Reversal Summary

```http
GET /backoffice/reversals/summary
```

Gateway:

```http
GET /bo/backoffice/reversals/summary
```

Response shape:

```json
{
  "summary": {
    "totalCount": 3,
    "pendingCount": 1,
    "failedCount": 2,
    "successfulCount": 0
  },
  "sources": {
    "FXPEER_AIRTIME": {
      "source": "FXPEER_AIRTIME",
      "statusCode": 200,
      "description": "OK",
      "totalCount": 1,
      "pendingCount": 0,
      "failedCount": 1,
      "successfulCount": 0
    },
    "TRANSACTIONS_INTERBANK": {
      "source": "TRANSACTIONS_INTERBANK",
      "sourceGroup": "TRANSACTIONS",
      "statusCode": 200,
      "description": "OK",
      "totalCount": 2,
      "pendingCount": 1,
      "failedCount": 1,
      "successfulCount": 0
    }
  }
}
```

### List Reversal Cases

```http
GET /backoffice/reversals/cases
```

Gateway:

```http
GET /bo/backoffice/reversals/cases
```

Query params:

| Name | Required | Example | Notes |
| --- | --- | --- | --- |
| `source` | No | `FXPEER_AIRTIME` | Allowed values: `FXPEER_AIRTIME`, `TRANSACTIONS_INTERBANK`, `TRANSACTIONS_LOCAL_TRANSFER`, or legacy umbrella `TRANSACTIONS`. Empty returns all. |
| `status` | No | `FAILED` | Typical values: `PENDING`, `FAILED`, `SUCCESS`. |
| `page` | No | `0` | Default `0`. |
| `size` | No | `20` | Default `20`. |

Example:

```http
GET /bo/backoffice/reversals/cases?source=TRANSACTIONS_INTERBANK&status=FAILED&page=0&size=20
```

Response shape:

```json
{
  "content": [
    {
      "source": "TRANSACTIONS_INTERBANK",
      "sourceGroup": "TRANSACTIONS",
      "caseRef": "1770381188463700526",
      "status": "FAILED",
      "requestedAt": "2026-06-23T07:45:00Z",
      "completedAt": null,
      "retryCount": 2,
      "lastError": "Provider reversal failed",
      "serviceType": "INTERBANK",
      "operator": null,
      "product": null,
      "providerError": "Timeout from provider",
      "legs": [],
      "raw": {},
      "openManualRequests": [
        {
          "approvalRequestId": 91,
          "status": "PENDING",
          "makerAdminId": 12,
          "createdAt": "2026-06-23T07:46:00Z",
          "submittedAt": "2026-06-23T07:46:00Z"
        }
      ]
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

### Submit Manual Reversal Request

```http
POST /backoffice/reversals/cases/{source}/{caseRef}/manual-request
```

Gateway:

```http
POST /bo/backoffice/reversals/cases/{source}/{caseRef}/manual-request
```

Path params:

| Name | Example | Notes |
| --- | --- | --- |
| `source` | `TRANSACTIONS_INTERBANK` | Allowed values: `FXPEER_AIRTIME`, `TRANSACTIONS_INTERBANK`, `TRANSACTIONS_LOCAL_TRANSFER`, or legacy umbrella `TRANSACTIONS`. |
| `caseRef` | `1770381188463700526` | Uses `processId` for `FXPEER_AIRTIME`, `transactionId` for transaction-service sources. |

Request body:

```json
{
  "notes": "Auto-reversal failed after debit success. Requesting checker approval for manual retry."
}
```

Response shape:

```json
{
  "approvalRequestId": 91,
  "status": "PENDING",
  "source": "TRANSACTIONS_INTERBANK",
  "caseRef": "1770381188463700526",
  "notes": "Auto-reversal failed after debit success. Requesting checker approval for manual retry."
}
```

Manual request rules:

- Only `FAILED` or `PENDING` reversal cases can be submitted.
- Maker submits here.
- Checker approves through the existing approval endpoints.
- Rejected approvals go to remediation/resubmission through the existing approval flow.

## Referral Program Management

These endpoints let backoffice create, update, activate, pause, end, list, inspect, and audit referral programs. The backoffice service proxies these calls to the profiling service; frontend should still call the backoffice paths below.

For write/state-change requests, FE may pass `X-User-Id` if it has the current admin user id. If omitted, the backend still accepts the request, but audit attribution may be less specific.

```http
X-User-Id: <adminUserId>
```

### List Referral Programs

```http
GET /backoffice/referral-programs/get-all
```

Gateway:

```http
GET /bo/backoffice/referral-programs/get-all
```

Query params:

| Name | Required | Example | Notes |
| --- | --- | --- | --- |
| `productType` | No | `P2P` | Filters programs by product type. Empty returns all. |

Example:

```http
GET /bo/backoffice/referral-programs/get-all?productType=P2P
```

### Get Active Referral Program

```http
GET /backoffice/referral-programs/active?productType=P2P
```

Gateway:

```http
GET /bo/backoffice/referral-programs/active?productType=P2P
```

Query params:

| Name | Required | Example |
| --- | --- | --- |
| `productType` | Yes | `P2P` |

### Get Referral Program Detail

```http
GET /backoffice/referral-programs/{id}
```

Gateway:

```http
GET /bo/backoffice/referral-programs/{id}
```

### Create Referral Program

```http
POST /backoffice/referral-programs/create
```

Gateway:

```http
POST /bo/backoffice/referral-programs/create
```

Request body:

```json
{
  "programCode": "P2P_REF_2026_Q3",
  "title": "P2P Referral Q3",
  "description": "Reward customers for completed P2P referrals.",
  "productType": "P2P",
  "rewardTarget": "REFERRER",
  "rewardMode": "FIXED",
  "rewardValue": 1000,
  "rewardCurrencyMode": "FIXED",
  "fixedCurrencyCode": "NGN",
  "minQualifyingAmount": 5000,
  "minRewardAmount": 0,
  "maxRewardAmount": 1000,
  "qualifyingTransactionCount": 1,
  "startAt": "2026-07-01T00:00:00Z",
  "endAt": "2026-09-30T23:59:59Z"
}
```

### Update Referral Program

```http
PUT /backoffice/referral-programs/{id}
```

Gateway:

```http
PUT /bo/backoffice/referral-programs/{id}
```

Request body:

```json
{
  "title": "P2P Referral Q3 Updated",
  "description": "Updated campaign copy.",
  "rewardTarget": "REFERRER",
  "rewardMode": "FIXED",
  "rewardValue": 1500,
  "rewardCurrencyMode": "FIXED",
  "fixedCurrencyCode": "NGN",
  "minQualifyingAmount": 5000,
  "minRewardAmount": 0,
  "maxRewardAmount": 1500,
  "qualifyingTransactionCount": 1,
  "startAt": "2026-07-01T00:00:00Z",
  "endAt": "2026-09-30T23:59:59Z"
}
```

Note: `programCode` and `productType` are create-only in the backoffice request model.

### Activate Referral Program

```http
POST /backoffice/referral-programs/{id}/activate
```

Gateway:

```http
POST /bo/backoffice/referral-programs/{id}/activate
```

No request body is required.

### Pause Referral Program

```http
POST /backoffice/referral-programs/{id}/pause
```

Gateway:

```http
POST /bo/backoffice/referral-programs/{id}/pause
```

No request body is required.

### End Referral Program

```http
POST /backoffice/referral-programs/{id}/end
```

Gateway:

```http
POST /bo/backoffice/referral-programs/{id}/end
```

No request body is required.

### Get Referral Program Audit

```http
GET /backoffice/referral-programs/{id}/audit
```

Gateway:

```http
GET /bo/backoffice/referral-programs/{id}/audit
```

## Referral Program Data Shapes

### ReferralProgram

Returned as `data` for create, update, activate, pause, end, active, and detail endpoints. List endpoint returns `data` as an array of this object.

```json
{
  "id": 10,
  "programCode": "P2P_REF_2026_Q3",
  "title": "P2P Referral Q3",
  "description": "Reward customers for completed P2P referrals.",
  "productType": "P2P",
  "rewardTarget": "REFERRER",
  "rewardMode": "FIXED",
  "rewardValue": 1000,
  "rewardCurrencyMode": "FIXED",
  "fixedCurrencyCode": "NGN",
  "minQualifyingAmount": 5000,
  "minRewardAmount": 0,
  "maxRewardAmount": 1000,
  "qualifyingTransactionCount": 1,
  "status": "ACTIVE",
  "startAt": "2026-07-01T00:00:00Z",
  "endAt": "2026-09-30T23:59:59Z",
  "createdBy": "12",
  "createdAt": "2026-06-23T08:00:00Z",
  "updatedBy": "12",
  "updatedAt": "2026-06-23T08:05:00Z"
}
```

### ReferralProgramAudit

Returned as `data` array from the audit endpoint.

```json
[
  {
    "id": 100,
    "programId": 10,
    "eventType": "ACTIVATED",
    "actor": "12",
    "note": "Program activated",
    "eventAt": "2026-06-23T08:05:00Z"
  }
]
```

## Exact Paths To Share With FE

Gateway paths:

```text
GET  /bo/backoffice/reversals/summary
GET  /bo/backoffice/reversals/cases?source=TRANSACTIONS_INTERBANK&status=&page=0&size=20
POST /bo/backoffice/reversals/cases/{source}/{caseRef}/manual-request

GET  /bo/backoffice/referral-programs/get-all?productType=P2P
GET  /bo/backoffice/referral-programs/active?productType=P2P
GET  /bo/backoffice/referral-programs/{id}
POST /bo/backoffice/referral-programs/create
PUT  /bo/backoffice/referral-programs/{id}
POST /bo/backoffice/referral-programs/{id}/activate
POST /bo/backoffice/referral-programs/{id}/pause
POST /bo/backoffice/referral-programs/{id}/end
GET  /bo/backoffice/referral-programs/{id}/audit
```

Service-local Swagger paths:

```text
GET  /backoffice/reversals/summary
GET  /backoffice/reversals/cases?source=TRANSACTIONS_INTERBANK&status=&page=0&size=20
POST /backoffice/reversals/cases/{source}/{caseRef}/manual-request

GET  /backoffice/referral-programs/get-all?productType=P2P
GET  /backoffice/referral-programs/active?productType=P2P
GET  /backoffice/referral-programs/{id}
POST /backoffice/referral-programs/create
PUT  /backoffice/referral-programs/{id}
POST /backoffice/referral-programs/{id}/activate
POST /backoffice/referral-programs/{id}/pause
POST /backoffice/referral-programs/{id}/end
GET  /backoffice/referral-programs/{id}/audit
```
