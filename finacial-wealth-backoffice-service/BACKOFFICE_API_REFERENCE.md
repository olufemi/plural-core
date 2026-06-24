# Backoffice API Reference

Single FE-facing API reference for `finacial-wealth-backoffice-service`.

Source: generated from local backoffice controllers and Swagger annotations. The live `/bo/v3/api-docs` JSON was blocked by an Infoblox access-denied page from this machine, so this file is source-derived rather than exported directly from Swagger.

Generated at: `2026-06-23T08:13:53.867345+00:00`
Controllers scanned: `18`
Operations found: `91`

## How FE Should Call

- For production/staging gateway calls, prefer paths that begin with `/bo/...` where present.
- For endpoints listed only as `/backoffice/...`, the gateway form is normally `/bo/backoffice/...`.
- Swagger also shows service-local paths like `/backoffice/...`; aliases are listed exactly as declared in code.
- Authenticated endpoints use the normal backoffice bearer token.
- Use `/backoffice/reversals` as the single FE reversal module. Product-specific reversal paths such as `/backoffice/interbank/reversals` are compatibility wrappers.

```http
Authorization: Bearer <backoffice_access_token>
Content-Type: application/json
```

## Endpoint Summary

| Group | Method | Paths | Summary |
| --- | --- | --- | --- |
| Admin Users | `POST` | `/admin-users`<br>`/backoffice/admin-users`<br>`/bo/admin-users` | create |
| Admin Users | `GET` | `/admin-users/admins`<br>`/backoffice/admin-users/admins`<br>`/bo/admin-users/admins` | getAdmins |
| Admin Users | `GET` | `/admin-users/admins/{adminId}`<br>`/backoffice/admin-users/admins/{adminId}`<br>`/bo/admin-users/admins/{adminId}` | getAdmin |
| Admin Users | `PATCH` | `/admin-users/{adminId}`<br>`/backoffice/admin-users/{adminId}`<br>`/bo/admin-users/{adminId}` | update |
| Admin Users | `POST` | `/admin-users/{adminId}/activate`<br>`/backoffice/admin-users/{adminId}/activate`<br>`/bo/admin-users/{adminId}/activate` | activate |
| Admin Users | `POST` | `/admin-users/{adminId}/password-reset`<br>`/backoffice/admin-users/{adminId}/password-reset`<br>`/bo/admin-users/{adminId}/password-reset` | resetPassword |
| Admin Users | `POST` | `/admin-users/{adminId}/suspend`<br>`/backoffice/admin-users/{adminId}/suspend`<br>`/bo/admin-users/{adminId}/suspend` | suspend |
| Approvals | `GET` | `/backoffice/approvals` | list |
| Approvals | `GET` | `/backoffice/approvals/{approvalId}` | get |
| Approvals | `POST` | `/backoffice/approvals/{approvalId}/approve` | approve |
| Approvals | `POST` | `/backoffice/approvals/{approvalId}/reject` | reject |
| Approvals | `POST` | `/backoffice/approvals/{approvalId}/resubmit` | resubmit |
| Authentication | `POST` | `/auth/login`<br>`/bo/auth/login` | Login admin user |
| Authentication | `POST` | `/auth/logout`<br>`/bo/auth/logout` | Logout admin user |
| Authentication | `POST` | `/auth/mfa/verify`<br>`/bo/auth/mfa/verify` | Verify MFA code |
| Authentication | `POST` | `/auth/password/change`<br>`/bo/auth/password/change` | changePassword |
| Authentication | `POST` | `/auth/password/recovery/complete`<br>`/bo/auth/password/recovery/complete` | completePasswordRecovery |
| Authentication | `POST` | `/auth/password/recovery/start`<br>`/bo/auth/password/recovery/start` | startPasswordRecovery |
| Authentication | `POST` | `/auth/refresh`<br>`/bo/auth/refresh` | Refresh access token |
| Authentication | `POST` | `/auth/refresh-old`<br>`/bo/auth/refresh-old` | Refresh access token |
| Customers | `GET` | `/backoffice/profiling` | getAllCustomers |
| Customers | `GET` | `/backoffice/profiling/{id}` | getCustomerById |
| Customers | `PATCH` | `/backoffice/profiling/{id}/block` | blockCustomer |
| Customers | `GET` | `/backoffice/profiling/{id}/investment-summary` | getCustomerInvestmentSummary |
| Customers | `GET` | `/backoffice/profiling/{id}/liquidations` | getCustomerLiquidations |
| Customers | `GET` | `/backoffice/profiling/{id}/orders` | getCustomerInvestmentOrders |
| Customers | `GET` | `/backoffice/profiling/{id}/positions` | getCustomerInvestmentPositions |
| Customers | `PATCH` | `/backoffice/profiling/{id}/unblock` | unblockCustomer |
| Group Savings | `GET` | `/backoffice/group-savings/contribution-payout-monitoring` | getContributionPayoutMonitoring |
| Group Savings | `POST` | `/backoffice/group-savings/delete` | deleteGroupSaving |
| Group Savings | `GET` | `/backoffice/group-savings/groups` | listGroups |
| Group Savings | `GET` | `/backoffice/group-savings/groups/{groupId}` | getGroup |
| Group Savings | `POST` | `/backoffice/group-savings/groups/{groupId}/close` | closeGroup |
| Group Savings | `GET` | `/backoffice/group-savings/slot-assignment-tracking` | getSlotAssignmentTracking |
| Investments | `POST` | `/backoffice/investments/approve-liquidation-request`<br>`/bo/backoffice/investments/approve-liquidation-request` | approveLiquidation |
| Investments | `GET` | `/backoffice/investments/dashboard`<br>`/bo/backoffice/investments/dashboard` | getDashboard |
| Investments | `POST` | `/backoffice/investments/deny-customer-liquidation-request`<br>`/bo/backoffice/investments/deny-customer-liquidation-request` | cancelLiquidation |
| Investments | `GET` | `/backoffice/investments/featured-services`<br>`/bo/backoffice/investments/featured-services` | getFeaturedServices |
| Investments | `GET` | `/backoffice/investments/featured-services/config`<br>`/bo/backoffice/investments/featured-services/config` | getFeaturedServicesConfig |
| Investments | `POST` | `/backoffice/investments/featured-services/config`<br>`/bo/backoffice/investments/featured-services/config` | saveFeaturedServicesConfig |
| Investments | `GET` | `/backoffice/investments/liquidations`<br>`/bo/backoffice/investments/liquidations` | getLiquidations |
| Investments | `GET` | `/backoffice/investments/liquidations/history`<br>`/bo/backoffice/investments/liquidations/history` | getLiquidationHistory |
| Investments | `GET` | `/backoffice/investments/orders`<br>`/bo/backoffice/investments/orders` | getOrders |
| Investments | `GET` | `/backoffice/investments/oversight`<br>`/bo/backoffice/investments/oversight` | getOversightDashboard |
| Investments | `GET` | `/backoffice/investments/performance`<br>`/bo/backoffice/investments/performance` | getPerformanceDashboard |
| Investments | `GET` | `/backoffice/investments/products`<br>`/bo/backoffice/investments/products` | getProducts |
| Investments | `POST` | `/backoffice/investments/products`<br>`/bo/backoffice/investments/products` | createProduct |
| Investments | `GET` | `/backoffice/investments/products/export.csv`<br>`/bo/backoffice/investments/products/export.csv` | exportProducts |
| Investments | `GET` | `/backoffice/investments/products/{productCode}`<br>`/bo/backoffice/investments/products/{productCode}` | getProduct |
| Investments | `PUT` | `/backoffice/investments/products/{productCode}`<br>`/bo/backoffice/investments/products/{productCode}` | updateProduct |
| Other | `POST` | `/auth/mfa/confirm`<br>`/bo/auth/mfa/confirm` | confirm |
| Other | `POST` | `/auth/mfa/setup`<br>`/bo/auth/mfa/setup` | setup |
| Other | `GET` | `/backoffice/audit` | list |
| Other | `POST` | `/backoffice/campaigns/create` | create |
| Other | `GET` | `/backoffice/campaigns/get-all` | list |
| Other | `GET` | `/backoffice/campaigns/{id}` | get |
| Other | `PUT` | `/backoffice/campaigns/{id}` | update |
| Other | `POST` | `/backoffice/campaigns/{id}/approve` | approve |
| Other | `GET` | `/backoffice/campaigns/{id}/audit` | audit |
| Other | `POST` | `/backoffice/campaigns/{id}/cancel` | cancel |
| Other | `POST` | `/backoffice/campaigns/{id}/restart` | restart |
| Other | `POST` | `/backoffice/campaigns/{id}/stop` | stop |
| Other | `POST` | `/backoffice/fxpeer/offers/update` | updateOffer |
| Other | `GET` | `/backoffice/fxpeer/services/airtime-reversals` | getAirtimeReversalCases |
| Other | `GET` | `/backoffice/fxpeer/services/airtime-reversals/summary` | getAirtimeReversalSummary |
| Other | `POST` | `/backoffice/interbank/name-enquiry` | nameEnquiry |
| Other | `GET` | `/backoffice/interbank/reversals` | reversalCases |
| Other | `GET` | `/backoffice/interbank/reversals/summary` | reversalSummary |
| Other | `GET` | `/backoffice/referral-programs/active` | active |
| Other | `POST` | `/backoffice/referral-programs/create` | create |
| Other | `GET` | `/backoffice/referral-programs/get-all` | list |
| Other | `GET` | `/backoffice/referral-programs/{id}` | get |
| Other | `PUT` | `/backoffice/referral-programs/{id}` | update |
| Other | `POST` | `/backoffice/referral-programs/{id}/activate` | activate |
| Other | `GET` | `/backoffice/referral-programs/{id}/audit` | audit |
| Other | `POST` | `/backoffice/referral-programs/{id}/end` | end |
| Other | `POST` | `/backoffice/referral-programs/{id}/pause` | pause |
| Other | `DELETE` | `/backoffice/storage/slides` | deleteSlide |
| Other | `GET` | `/backoffice/storage/slides` | listSlides |
| Other | `DELETE` | `/backoffice/storage/slides/all` | deleteAllSlides |
| Other | `POST` | `/backoffice/storage/uploadPicture` | uploadPicture |
| Other | `POST` | `/backoffice/storage/uploadPictureFile` | uploadPictureFile |
| Other | `POST` | `/backoffice/storage/uploadSlide` | uploadSlide |
| Other | `GET` | `/bo/reports/sample-transactions.csv` | sample |
| Permissions | `GET` | `/admin/permissions`<br>`/api/admin/permissions`<br>`/backoffice/admin/permissions`<br>`/bo/admin/permissions` | list |
| Reversals | `GET` | `/backoffice/reversals/cases` | listCases |
| Reversals | `POST` | `/backoffice/reversals/cases/{source}/{caseRef}/manual-request` | requestManualReversal |
| Reversals | `GET` | `/backoffice/reversals/summary` | getSummary |
| Roles | `GET` | `/admin/roles`<br>`/api/admin/roles`<br>`/backoffice/admin/roles`<br>`/bo/admin/roles` | list |
| Roles | `POST` | `/admin/roles`<br>`/api/admin/roles`<br>`/backoffice/admin/roles`<br>`/bo/admin/roles` | create |
| Roles | `PUT` | `/admin/roles/{roleId}/permissions`<br>`/api/admin/roles/{roleId}/permissions`<br>`/backoffice/admin/roles/{roleId}/permissions`<br>`/bo/admin/roles/{roleId}/permissions` | updatePermissions |

## Admin Users

Backoffice admin user management endpoints.

### POST /admin-users

Aliases:
- `/admin-users`
- `/backoffice/admin-users`
- `/bo/admin-users`

Handler: `create`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/auth/controller/AdminUserController.java`
Returns: `ResponseEntity<AdminUserResponse>`
Permission/Security: `hasAuthority('ROLE_SUPER_ADMIN')`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `boAdminUserId` | `request-attribute` | `Long` | `` | @RequestAttribute("boAdminUserId") Long actorAdminId |
| `req` | `body` | `CreateAdminUserRequest` | `true` |  |

Request body fields for `CreateAdminUserRequest`:

| Field | Type |
| --- | --- |
| `email` | `String` |
| `fullName` | `String` |
| `password` | `String` |
| `confirmPassword` | `String` |
| `roles` | `Set<BoAdminRole>` |

---

### GET /admin-users/admins

Aliases:
- `/admin-users/admins`
- `/backoffice/admin-users/admins`
- `/bo/admin-users/admins`

Handler: `getAdmins`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/auth/controller/AdminUserController.java`
Returns: `Page<AdminUserResponse>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `boAdminUserId` | `request-attribute` | `Long` | `` | @RequestAttribute("boAdminUserId") Long actorAdminId |
| `page` | `query` | `int` | `true` |  |
| `size` | `query` | `int` | `true` |  |
| `q` | `query` | `String` | `false` |  |

---

### GET /admin-users/admins/{adminId}

Aliases:
- `/admin-users/admins/{adminId}`
- `/backoffice/admin-users/admins/{adminId}`
- `/bo/admin-users/admins/{adminId}`

Handler: `getAdmin`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/auth/controller/AdminUserController.java`
Returns: `AdminUserResponse`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `adminId` | `path` | `Long` | `true` |  |
| `boAdminUserId` | `request-attribute` | `Long` | `` | @RequestAttribute("boAdminUserId") Long actorAdminId |

---

### PATCH /admin-users/{adminId}

Aliases:
- `/admin-users/{adminId}`
- `/backoffice/admin-users/{adminId}`
- `/bo/admin-users/{adminId}`

Handler: `update`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/auth/controller/AdminUserController.java`
Returns: `ResponseEntity<AdminUserResponse>`
Permission/Security: `hasAuthority('ROLE_SUPER_ADMIN')`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `boAdminUserId` | `request-attribute` | `Long` | `` | @RequestAttribute("boAdminUserId") Long actorAdminId |
| `adminId` | `path` | `Long` | `true` |  |
| `req` | `body` | `UpdateAdminUserRequest` | `true` |  |

Request body fields for `UpdateAdminUserRequest`:

| Field | Type |
| --- | --- |
| `fullName` | `String` |
| `roles` | `Set<BoAdminRole>` |

---

### POST /admin-users/{adminId}/activate

Aliases:
- `/admin-users/{adminId}/activate`
- `/backoffice/admin-users/{adminId}/activate`
- `/bo/admin-users/{adminId}/activate`

Handler: `activate`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/auth/controller/AdminUserController.java`
Returns: `ResponseEntity<AdminUserResponse>`
Permission/Security: `hasAuthority('ROLE_SUPER_ADMIN')`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `boAdminUserId` | `request-attribute` | `Long` | `` | @RequestAttribute("boAdminUserId") Long actorAdminId |
| `adminId` | `path` | `Long` | `true` |  |

---

### POST /admin-users/{adminId}/password-reset

Aliases:
- `/admin-users/{adminId}/password-reset`
- `/backoffice/admin-users/{adminId}/password-reset`
- `/bo/admin-users/{adminId}/password-reset`

Handler: `resetPassword`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/auth/controller/AdminUserController.java`
Returns: `ResponseEntity<AdminPasswordResetResponse>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `boAdminUserId` | `request-attribute` | `Long` | `` | @RequestAttribute("boAdminUserId") Long actorAdminId |
| `adminId` | `path` | `Long` | `true` |  |

---

### POST /admin-users/{adminId}/suspend

Aliases:
- `/admin-users/{adminId}/suspend`
- `/backoffice/admin-users/{adminId}/suspend`
- `/bo/admin-users/{adminId}/suspend`

Handler: `suspend`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/auth/controller/AdminUserController.java`
Returns: `ResponseEntity<AdminUserResponse>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `boAdminUserId` | `request-attribute` | `Long` | `` | @RequestAttribute("boAdminUserId") Long actorAdminId |
| `adminId` | `path` | `Long` | `true` |  |

---

## Approvals

Maker-checker approval inbox and decision endpoints.

### GET /backoffice/approvals

Handler: `list`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/approval/controller/ApprovalController.java`
Returns: `Map<String, Object>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `status` | `query` | `String` | `false` |  |
| `page` | `query` | `Integer` | `true` |  |
| `size` | `query` | `Integer` | `true` |  |

---

### GET /backoffice/approvals/{approvalId}

Handler: `get`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/approval/controller/ApprovalController.java`
Returns: `Map<String, Object>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `approvalId` | `path` | `Long` | `true` |  |

---

### POST /backoffice/approvals/{approvalId}/approve

Handler: `approve`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/approval/controller/ApprovalController.java`
Returns: `Map<String, Object>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `approvalId` | `path` | `Long` | `true` |  |
| `boAdminUserId` | `request-attribute` | `Long` | `` | @RequestAttribute("boAdminUserId") Long actorAdminId |

---

### POST /backoffice/approvals/{approvalId}/reject

Handler: `reject`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/approval/controller/ApprovalController.java`
Returns: `Map<String, Object>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `approvalId` | `path` | `Long` | `true` |  |
| `boAdminUserId` | `request-attribute` | `Long` | `` | @RequestAttribute("boAdminUserId") Long actorAdminId |
| `decision` | `body` | `ApprovalDecisionRequest` | `false` |  |

Request body fields for `ApprovalDecisionRequest`:

| Field | Type |
| --- | --- |
| `reason` | `String` |

---

### POST /backoffice/approvals/{approvalId}/resubmit

Handler: `resubmit`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/approval/controller/ApprovalController.java`
Returns: `Map<String, Object>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `approvalId` | `path` | `Long` | `true` |  |
| `boAdminUserId` | `request-attribute` | `Long` | `` | @RequestAttribute("boAdminUserId") Long actorAdminId |
| `body` | `body` | `ApprovalResubmitRequest` | `false` |  |

Request body fields for `ApprovalResubmitRequest`:

| Field | Type |
| --- | --- |
| `notes` | `String` |

---

## Authentication

Backoffice login, MFA, token refresh, and password recovery endpoints.

### POST /auth/login

Aliases:
- `/auth/login`
- `/bo/auth/login`

Summary: Login admin user

Handler: `login`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/auth/controller/AuthController.java`
Returns: `ResponseEntity<?>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `req` | `body` | `LoginRequest` | `true` |  |

Request body fields for `LoginRequest`:

| Field | Type |
| --- | --- |
| `email` | `String` |
| `password` | `String` |

---

### POST /auth/logout

Aliases:
- `/auth/logout`
- `/bo/auth/logout`

Summary: Logout admin user

Handler: `logout`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/auth/controller/AuthController.java`
Returns: `ResponseEntity<Void>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `refreshToken` | `query` | `String` | `true` |  |

---

### POST /auth/mfa/verify

Aliases:
- `/auth/mfa/verify`
- `/bo/auth/mfa/verify`

Summary: Verify MFA code

Handler: `verifyMfa`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/auth/controller/AuthController.java`
Returns: `ResponseEntity<TokenResponse>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `req` | `body` | `LoginStep2Request` | `true` |  |

Request body fields for `LoginStep2Request`:

| Field | Type |
| --- | --- |
| `challengeId` | `String` |
| `code` | `String` |

---

### POST /auth/password/change

Aliases:
- `/auth/password/change`
- `/bo/auth/password/change`

Handler: `changePassword`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/auth/controller/AuthController.java`
Returns: `ResponseEntity<Void>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `boAdminUserId` | `request-attribute` | `Long` | `` | @RequestAttribute("boAdminUserId") Long adminUserId |
| `request` | `body` | `ChangePasswordRequest` | `true` |  |

Request body fields for `ChangePasswordRequest`:

| Field | Type |
| --- | --- |
| `currentPassword` | `String` |
| `newPassword` | `String` |
| `confirmPassword` | `String` |

---

### POST /auth/password/recovery/complete

Aliases:
- `/auth/password/recovery/complete`
- `/bo/auth/password/recovery/complete`

Handler: `completePasswordRecovery`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/auth/controller/AuthController.java`
Returns: `ResponseEntity<Void>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `request` | `body` | `PasswordRecoveryCompleteRequest` | `true` |  |

Request body fields for `PasswordRecoveryCompleteRequest`:

| Field | Type |
| --- | --- |
| `challengeId` | `String` |
| `code` | `String` |
| `newPassword` | `String` |
| `confirmPassword` | `String` |

---

### POST /auth/password/recovery/start

Aliases:
- `/auth/password/recovery/start`
- `/bo/auth/password/recovery/start`

Handler: `startPasswordRecovery`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/auth/controller/AuthController.java`
Returns: `ResponseEntity<PasswordRecoveryStartResponse>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `request` | `body` | `PasswordRecoveryStartRequest` | `true` |  |

Request body fields for `PasswordRecoveryStartRequest`:

| Field | Type |
| --- | --- |
| `email` | `String` |

---

### POST /auth/refresh

Aliases:
- `/auth/refresh`
- `/bo/auth/refresh`

Summary: Refresh access token

Handler: `refresh`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/auth/controller/AuthController.java`
Returns: `ResponseEntity<TokenResponse>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `refreshToken` | `query` | `String` | `true` |  |

---

### POST /auth/refresh-old

Aliases:
- `/auth/refresh-old`
- `/bo/auth/refresh-old`

Summary: Refresh access token

Handler: `refreshOld`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/auth/controller/AuthController.java`
Returns: `ResponseEntity<TokenResponse>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `refreshToken` | `query` | `String` | `true` |  |

---

## Customers

Customer support and customer 360 backoffice endpoints.

### GET /backoffice/profiling

Handler: `getAllCustomers`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/ProfilingManagementController.java`
Returns: `ApiResponse<Page<RegWalletInfoBackofficeResponse>>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `page` | `query` | `int` | `true` |  |
| `size` | `query` | `int` | `true` |  |
| `sort` | `query` | `String` | `true` |  |

---

### GET /backoffice/profiling/{id}

Handler: `getCustomerById`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/ProfilingManagementController.java`
Returns: `ApiResponse<RegWalletInfoBackofficeResponse>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `id` | `path` | `Long` | `true` |  |

---

### PATCH /backoffice/profiling/{id}/block

Handler: `blockCustomer`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/ProfilingManagementController.java`
Returns: `ApiResponse<RegWalletInfoBackofficeResponse>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `id` | `path` | `Long` | `true` |  |
| `request` | `body` | `BlockUserRequest` | `true` |  |

Request body fields for `BlockUserRequest`:

| Field | Type |
| --- | --- |
| `reason` | `String` |
| `performedBy` | `String` |

---

### GET /backoffice/profiling/{id}/investment-summary

Handler: `getCustomerInvestmentSummary`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/ProfilingManagementController.java`
Returns: `Map<String, Object>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `id` | `path` | `Long` | `true` |  |

---

### GET /backoffice/profiling/{id}/liquidations

Handler: `getCustomerLiquidations`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/ProfilingManagementController.java`
Returns: `Map<String, Object>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `id` | `path` | `Long` | `true` |  |
| `status` | `query` | `String` | `false` |  |
| `page` | `query` | `Integer` | `true` |  |
| `size` | `query` | `Integer` | `true` |  |

---

### GET /backoffice/profiling/{id}/orders

Handler: `getCustomerInvestmentOrders`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/ProfilingManagementController.java`
Returns: `Map<String, Object>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `id` | `path` | `Long` | `true` |  |
| `type` | `query` | `String` | `false` |  |
| `status` | `query` | `String` | `false` |  |
| `page` | `query` | `Integer` | `true` |  |
| `size` | `query` | `Integer` | `true` |  |

---

### GET /backoffice/profiling/{id}/positions

Handler: `getCustomerInvestmentPositions`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/ProfilingManagementController.java`
Returns: `Map<String, Object>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `id` | `path` | `Long` | `true` |  |
| `page` | `query` | `Integer` | `true` |  |
| `size` | `query` | `Integer` | `true` |  |

---

### PATCH /backoffice/profiling/{id}/unblock

Handler: `unblockCustomer`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/ProfilingManagementController.java`
Returns: `ApiResponse<RegWalletInfoBackofficeResponse>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `id` | `path` | `Long` | `true` |  |
| `request` | `body` | `BlockUserRequest` | `true` |  |

Request body fields for `BlockUserRequest`:

| Field | Type |
| --- | --- |
| `reason` | `String` |
| `performedBy` | `String` |

---

## Group Savings

Backoffice monitoring and operational endpoints for contribution, payout, and slot tracking.

### GET /backoffice/group-savings/contribution-payout-monitoring

Handler: `getContributionPayoutMonitoring`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/BoGroupSavingsController.java`
Returns: `Map<String, Object>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `period` | `query` | `String` | `false` |  |
| `fromDate` | `query` | `LocalDate` | `false` |  |
| `toDate` | `query` | `LocalDate` | `false` |  |
| `groupId` | `query` | `Long` | `false` |  |

---

### POST /backoffice/group-savings/delete

Handler: `deleteGroupSaving`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/BoGroupSavingsController.java`
Returns: `Map<String, Object>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `request` | `body` | `Map<String, Object>` | `true` |  |

---

### GET /backoffice/group-savings/groups

Handler: `listGroups`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/BoGroupSavingsController.java`
Returns: `Map<String, Object>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `status` | `query` | `String` | `false` |  |
| `search` | `query` | `String` | `false` |  |
| `page` | `query` | `Integer` | `true` |  |
| `size` | `query` | `Integer` | `true` |  |

---

### GET /backoffice/group-savings/groups/{groupId}

Handler: `getGroup`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/BoGroupSavingsController.java`
Returns: `Map<String, Object>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `groupId` | `path` | `Long` | `true` |  |

---

### POST /backoffice/group-savings/groups/{groupId}/close

Handler: `closeGroup`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/BoGroupSavingsController.java`
Returns: `Map<String, Object>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `groupId` | `path` | `Long` | `true` |  |

---

### GET /backoffice/group-savings/slot-assignment-tracking

Handler: `getSlotAssignmentTracking`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/BoGroupSavingsController.java`
Returns: `Map<String, Object>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `groupId` | `query` | `Long` | `false` |  |
| `status` | `query` | `String` | `false` |  |

---

## Investments

Backoffice investment product, transaction, and liquidation queue endpoints.

### POST /backoffice/investments/approve-liquidation-request

Aliases:
- `/backoffice/investments/approve-liquidation-request`
- `/bo/backoffice/investments/approve-liquidation-request`

Handler: `approveLiquidation`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/BoInvestmentController.java`
Returns: `Map<String, Object>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `req` | `body` | `LiquidationApprovalRequest` | `true` |  |

Request body fields for `LiquidationApprovalRequest`:

| Field | Type |
| --- | --- |
| `orderRef` | `String` |

---

### GET /backoffice/investments/dashboard

Aliases:
- `/backoffice/investments/dashboard`
- `/bo/backoffice/investments/dashboard`

Handler: `getDashboard`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/BoInvestmentController.java`
Returns: `Map<String, Object>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `range` | `query` | `String` | `true` |  |
| `productCode` | `query` | `String` | `false` |  |
| `fromDate` | `query` | `LocalDate` | `false` |  |
| `toDate` | `query` | `LocalDate` | `false` |  |

---

### POST /backoffice/investments/deny-customer-liquidation-request

Aliases:
- `/backoffice/investments/deny-customer-liquidation-request`
- `/bo/backoffice/investments/deny-customer-liquidation-request`

Handler: `cancelLiquidation`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/BoInvestmentController.java`
Returns: `Map<String, Object>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `req` | `body` | `LiquidationApprovalRequest` | `true` |  |

Request body fields for `LiquidationApprovalRequest`:

| Field | Type |
| --- | --- |
| `orderRef` | `String` |

---

### GET /backoffice/investments/featured-services

Aliases:
- `/backoffice/investments/featured-services`
- `/bo/backoffice/investments/featured-services`

Handler: `getFeaturedServices`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/BoInvestmentController.java`
Returns: `Map<String, Object>`

---

### GET /backoffice/investments/featured-services/config

Aliases:
- `/backoffice/investments/featured-services/config`
- `/bo/backoffice/investments/featured-services/config`

Handler: `getFeaturedServicesConfig`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/BoInvestmentController.java`
Returns: `Map<String, Object>`

---

### POST /backoffice/investments/featured-services/config

Aliases:
- `/backoffice/investments/featured-services/config`
- `/bo/backoffice/investments/featured-services/config`

Handler: `saveFeaturedServicesConfig`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/BoInvestmentController.java`
Returns: `Map<String, Object>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `request` | `body` | `FeaturedServicesConfigRequest` | `true` |  |

---

### GET /backoffice/investments/liquidations

Aliases:
- `/backoffice/investments/liquidations`
- `/bo/backoffice/investments/liquidations`

Handler: `getLiquidations`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/BoInvestmentController.java`
Returns: `Map<String, Object>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `status` | `query` | `String` | `false` |  |
| `productCode` | `query` | `String` | `false` |  |
| `fromDate` | `query` | `LocalDate` | `false` |  |
| `toDate` | `query` | `LocalDate` | `false` |  |
| `page` | `query` | `Integer` | `true` |  |
| `size` | `query` | `Integer` | `true` |  |

---

### GET /backoffice/investments/liquidations/history

Aliases:
- `/backoffice/investments/liquidations/history`
- `/bo/backoffice/investments/liquidations/history`

Handler: `getLiquidationHistory`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/BoInvestmentController.java`
Returns: `Map<String, Object>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `status` | `query` | `String` | `false` |  |
| `productCode` | `query` | `String` | `false` |  |
| `fromDate` | `query` | `LocalDate` | `false` |  |
| `toDate` | `query` | `LocalDate` | `false` |  |
| `page` | `query` | `Integer` | `true` |  |
| `size` | `query` | `Integer` | `true` |  |

---

### GET /backoffice/investments/orders

Aliases:
- `/backoffice/investments/orders`
- `/bo/backoffice/investments/orders`

Handler: `getOrders`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/BoInvestmentController.java`
Returns: `Map<String, Object>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `type` | `query` | `String` | `false` |  |
| `status` | `query` | `String` | `false` |  |
| `productCode` | `query` | `String` | `false` |  |
| `cutoffBucket` | `query` | `String` | `false` |  |
| `fromDate` | `query` | `LocalDate` | `false` |  |
| `toDate` | `query` | `LocalDate` | `false` |  |
| `page` | `query` | `Integer` | `true` |  |
| `size` | `query` | `Integer` | `true` |  |

---

### GET /backoffice/investments/oversight

Aliases:
- `/backoffice/investments/oversight`
- `/bo/backoffice/investments/oversight`

Handler: `getOversightDashboard`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/BoInvestmentController.java`
Returns: `Map<String, Object>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `productCode` | `query` | `String` | `false` |  |
| `fromDate` | `query` | `LocalDate` | `false` |  |
| `toDate` | `query` | `LocalDate` | `false` |  |
| `actionType` | `query` | `String` | `false` |  |
| `status` | `query` | `String` | `false` |  |
| `size` | `query` | `Integer` | `false` |  |

---

### GET /backoffice/investments/performance

Aliases:
- `/backoffice/investments/performance`
- `/bo/backoffice/investments/performance`

Handler: `getPerformanceDashboard`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/BoInvestmentController.java`
Returns: `Map<String, Object>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `productCode` | `query` | `String` | `false` |  |
| `fromDate` | `query` | `LocalDate` | `false` |  |
| `toDate` | `query` | `LocalDate` | `false` |  |

---

### GET /backoffice/investments/products

Aliases:
- `/backoffice/investments/products`
- `/bo/backoffice/investments/products`

Handler: `getProducts`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/BoInvestmentController.java`
Returns: `Map<String, Object>`

---

### POST /backoffice/investments/products

Aliases:
- `/backoffice/investments/products`
- `/bo/backoffice/investments/products`

Handler: `createProduct`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/BoInvestmentController.java`
Returns: `Map<String, Object>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `req` | `body` | `InvestmentProductUpsertRequest` | `true` |  |

Request body fields for `InvestmentProductUpsertRequest`:

| Field | Type |
| --- | --- |
| `unitPrice` | `BigDecimal` |
| `yieldPa` | `BigDecimal` |
| `yieldYtd` | `BigDecimal` |
| `tenorDays` | `Integer` |
| `active` | `Boolean` |
| `liquidationFeeAppliedTo` | `LiquidationFeeAppliedTo` |
| `liquidationFeeType` | `LiquidationFeeType` |
| `liquidationFeeRate` | `BigDecimal` |
| `minLiquidationFee` | `BigDecimal` |
| `liquidationFeeCap` | `BigDecimal` |
| `lockEnabled` | `Boolean` |
| `lockDays` | `Integer` |
| `earlyLiquidationFeeAppliedTo` | `LiquidationFeeAppliedTo` |
| `earlyLiquidationFeeType` | `LiquidationFeeType` |
| `earlyLiquidationFeeRate` | `BigDecimal` |
| `earlyLiquidationFeeCap` | `BigDecimal` |
| `partnerProductCode` | `String` |
| `prospectusUrl` | `String` |
| `metaJson` | `String` |
| `enableProduct` | `String` |
| `percentageCurrValue` | `BigDecimal` |
| `scheduleMode` | `ScheduleMode` |
| `interestAccrueType` | `InterestAccrueType` |
| `interestCapitalization` | `InterestCapitalization` |
| `settlementDelayMinutes` | `Long` |
| `tenorMinutes` | `Long` |
| `maturityAtEndOfDay` | `Boolean` |
| `settlementAt` | `Instant` |
| `maturityAt` | `Instant` |

---

### GET /backoffice/investments/products/export.csv

Aliases:
- `/backoffice/investments/products/export.csv`
- `/bo/backoffice/investments/products/export.csv`

Handler: `exportProducts`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/BoInvestmentController.java`
Returns: `void`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `response` | `param` | `HttpServletResponse` | `` |  |

---

### GET /backoffice/investments/products/{productCode}

Aliases:
- `/backoffice/investments/products/{productCode}`
- `/bo/backoffice/investments/products/{productCode}`

Handler: `getProduct`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/BoInvestmentController.java`
Returns: `ResponseEntity<Map<String, Object>>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `productCode` | `path` | `String` | `true` |  |

---

### PUT /backoffice/investments/products/{productCode}

Aliases:
- `/backoffice/investments/products/{productCode}`
- `/bo/backoffice/investments/products/{productCode}`

Handler: `updateProduct`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/BoInvestmentController.java`
Returns: `Map<String, Object>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `productCode` | `path` | `String` | `true` |  |
| `req` | `body` | `InvestmentProductUpsertRequest` | `true` |  |

Request body fields for `InvestmentProductUpsertRequest`:

| Field | Type |
| --- | --- |
| `unitPrice` | `BigDecimal` |
| `yieldPa` | `BigDecimal` |
| `yieldYtd` | `BigDecimal` |
| `tenorDays` | `Integer` |
| `active` | `Boolean` |
| `liquidationFeeAppliedTo` | `LiquidationFeeAppliedTo` |
| `liquidationFeeType` | `LiquidationFeeType` |
| `liquidationFeeRate` | `BigDecimal` |
| `minLiquidationFee` | `BigDecimal` |
| `liquidationFeeCap` | `BigDecimal` |
| `lockEnabled` | `Boolean` |
| `lockDays` | `Integer` |
| `earlyLiquidationFeeAppliedTo` | `LiquidationFeeAppliedTo` |
| `earlyLiquidationFeeType` | `LiquidationFeeType` |
| `earlyLiquidationFeeRate` | `BigDecimal` |
| `earlyLiquidationFeeCap` | `BigDecimal` |
| `partnerProductCode` | `String` |
| `prospectusUrl` | `String` |
| `metaJson` | `String` |
| `enableProduct` | `String` |
| `percentageCurrValue` | `BigDecimal` |
| `scheduleMode` | `ScheduleMode` |
| `interestAccrueType` | `InterestAccrueType` |
| `interestCapitalization` | `InterestCapitalization` |
| `settlementDelayMinutes` | `Long` |
| `tenorMinutes` | `Long` |
| `maturityAtEndOfDay` | `Boolean` |
| `settlementAt` | `Instant` |
| `maturityAt` | `Instant` |

---

## Other

### POST /auth/mfa/confirm

Aliases:
- `/auth/mfa/confirm`
- `/bo/auth/mfa/confirm`

Handler: `confirm`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/auth/controller/MfaController.java`
Returns: `ResponseEntity<Void>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `boAdminUserId` | `request-attribute` | `Long` | `` | @RequestAttribute("boAdminUserId") Long adminUserId |
| `req` | `body` | `MfaConfirmRequest` | `true` |  |

Request body fields for `MfaConfirmRequest`:

| Field | Type |
| --- | --- |
| `code` | `String` |

---

### POST /auth/mfa/setup

Aliases:
- `/auth/mfa/setup`
- `/bo/auth/mfa/setup`

Handler: `setup`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/auth/controller/MfaController.java`
Returns: `ResponseEntity<MfaSetupResponse>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `boAdminUserId` | `request-attribute` | `Long` | `false` | @RequestAttribute(value = "boAdminUserId", required = false) Long adminUserId |

---

### GET /backoffice/audit

Handler: `list`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/auth/controller/AuditController.java`
Returns: `Page<AdminAuditLog>`
Permission/Security: `hasAuthority('ROLE_SUPER_ADMIN')`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `page` | `query` | `int` | `true` |  |
| `size` | `query` | `int` | `true` |  |

---

### POST /backoffice/campaigns/create

Handler: `create`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/CampaignManagementController.java`
Returns: `ApiResponseModel`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `req` | `body` | `CreateCampaignRequest` | `true` |  |

Request body fields for `CreateCampaignRequest`:

| Field | Type |
| --- | --- |
| `title` | `String` |
| `description` | `String` |
| `mediaObjectName` | `String` |
| `mediaContentType` | `String` |
| `mediaSignedUrl` | `String` |
| `embeddedLink` | `String` |
| `startAt` | `Date` |
| `endAt` | `Date` |
| `items` | `List<CampaignMediaItemRequest>` |

---

### GET /backoffice/campaigns/get-all

Handler: `list`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/CampaignManagementController.java`
Returns: `ApiResponseModel`

---

### GET /backoffice/campaigns/{id}

Handler: `get`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/CampaignManagementController.java`
Returns: `ApiResponseModel`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `id` | `path` | `Long` | `true` |  |

---

### PUT /backoffice/campaigns/{id}

Handler: `update`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/CampaignManagementController.java`
Returns: `ApiResponseModel`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `id` | `path` | `Long` | `true` |  |
| `req` | `body` | `UpdateCampaignRequest` | `true` |  |

Request body fields for `UpdateCampaignRequest`:

| Field | Type |
| --- | --- |
| `title` | `String` |
| `description` | `String` |
| `mediaObjectName` | `String` |
| `mediaContentType` | `String` |
| `mediaSignedUrl` | `String` |
| `embeddedLink` | `String` |
| `startAt` | `Date` |
| `endAt` | `Date` |
| `rotationSeconds` | `Integer` |
| `displayMode` | `String` |
| `items` | `List<CampaignMediaItemRequest>` |

---

### POST /backoffice/campaigns/{id}/approve

Handler: `approve`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/CampaignManagementController.java`
Returns: `ApiResponseModel`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `id` | `path` | `Long` | `true` |  |
| `req` | `body` | `ApproveCampaignRequest` | `true` |  |

Request body fields for `ApproveCampaignRequest`:

| Field | Type |
| --- | --- |
| `note` | `String` |

---

### GET /backoffice/campaigns/{id}/audit

Handler: `audit`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/CampaignManagementController.java`
Returns: `ApiResponseModel`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `id` | `path` | `Long` | `true` |  |

---

### POST /backoffice/campaigns/{id}/cancel

Handler: `cancel`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/CampaignManagementController.java`
Returns: `ApiResponseModel`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `id` | `path` | `Long` | `true` |  |

---

### POST /backoffice/campaigns/{id}/restart

Handler: `restart`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/CampaignManagementController.java`
Returns: `ApiResponseModel`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `id` | `path` | `Long` | `true` |  |

---

### POST /backoffice/campaigns/{id}/stop

Handler: `stop`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/CampaignManagementController.java`
Returns: `ApiResponseModel`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `id` | `path` | `Long` | `true` |  |

---

### POST /backoffice/fxpeer/offers/update

Handler: `updateOffer`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/BoFxPeerOfferController.java`
Returns: `Map<String, Object>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `request` | `body` | `Map<String, Object>` | `true` |  |

---

### GET /backoffice/fxpeer/services/airtime-reversals

Handler: `getAirtimeReversalCases`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/BoFxPeerServicesController.java`
Returns: `Map<String, Object>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `status` | `query` | `String` | `false` |  |

---

### GET /backoffice/fxpeer/services/airtime-reversals/summary

Handler: `getAirtimeReversalSummary`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/BoFxPeerServicesController.java`
Returns: `Map<String, Object>`

---

### POST /backoffice/interbank/name-enquiry

Handler: `nameEnquiry`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/BoInterbankController.java`
Returns: `Map<String, Object>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `request` | `body` | `Map<String, Object>` | `true` |  |

---

### GET /backoffice/interbank/reversals

Handler: `reversalCases`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/BoInterbankController.java`
Returns: `Map<String, Object>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `status` | `query` | `String` | `false` |  |

---

### GET /backoffice/interbank/reversals/summary

Handler: `reversalSummary`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/BoInterbankController.java`
Returns: `Map<String, Object>`

---

### GET /backoffice/referral-programs/active

Handler: `active`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/ReferralProgramManagementController.java`
Returns: `ApiResponseModel`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `productType` | `query` | `String` | `true` |  |

---

### POST /backoffice/referral-programs/create

Handler: `create`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/ReferralProgramManagementController.java`
Returns: `ApiResponseModel`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `req` | `body` | `CreateReferralProgramRequest` | `true` |  |
| `X-User-Id` | `header` | `String` | `false` |  |

Request body fields for `CreateReferralProgramRequest`:

| Field | Type |
| --- | --- |
| `programCode` | `String` |
| `title` | `String` |
| `description` | `String` |
| `productType` | `String` |
| `rewardTarget` | `String` |
| `rewardMode` | `String` |
| `rewardValue` | `BigDecimal` |
| `rewardCurrencyMode` | `String` |
| `fixedCurrencyCode` | `String` |
| `minQualifyingAmount` | `BigDecimal` |
| `minRewardAmount` | `BigDecimal` |
| `maxRewardAmount` | `BigDecimal` |
| `qualifyingTransactionCount` | `Integer` |
| `startAt` | `Date` |
| `endAt` | `Date` |

---

### GET /backoffice/referral-programs/get-all

Handler: `list`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/ReferralProgramManagementController.java`
Returns: `ApiResponseModel`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `productType` | `query` | `String` | `false` |  |

---

### GET /backoffice/referral-programs/{id}

Handler: `get`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/ReferralProgramManagementController.java`
Returns: `ApiResponseModel`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `id` | `path` | `Long` | `true` |  |

---

### PUT /backoffice/referral-programs/{id}

Handler: `update`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/ReferralProgramManagementController.java`
Returns: `ApiResponseModel`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `id` | `path` | `Long` | `true` |  |
| `req` | `body` | `UpdateReferralProgramRequest` | `true` |  |
| `X-User-Id` | `header` | `String` | `false` |  |

Request body fields for `UpdateReferralProgramRequest`:

| Field | Type |
| --- | --- |
| `title` | `String` |
| `description` | `String` |
| `rewardTarget` | `String` |
| `rewardMode` | `String` |
| `rewardValue` | `BigDecimal` |
| `rewardCurrencyMode` | `String` |
| `fixedCurrencyCode` | `String` |
| `minQualifyingAmount` | `BigDecimal` |
| `minRewardAmount` | `BigDecimal` |
| `maxRewardAmount` | `BigDecimal` |
| `qualifyingTransactionCount` | `Integer` |
| `startAt` | `Date` |
| `endAt` | `Date` |

---

### POST /backoffice/referral-programs/{id}/activate

Handler: `activate`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/ReferralProgramManagementController.java`
Returns: `ApiResponseModel`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `id` | `path` | `Long` | `true` |  |
| `X-User-Id` | `header` | `String` | `false` |  |

---

### GET /backoffice/referral-programs/{id}/audit

Handler: `audit`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/ReferralProgramManagementController.java`
Returns: `ApiResponseModel`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `id` | `path` | `Long` | `true` |  |

---

### POST /backoffice/referral-programs/{id}/end

Handler: `end`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/ReferralProgramManagementController.java`
Returns: `ApiResponseModel`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `id` | `path` | `Long` | `true` |  |
| `X-User-Id` | `header` | `String` | `false` |  |

---

### POST /backoffice/referral-programs/{id}/pause

Handler: `pause`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/ReferralProgramManagementController.java`
Returns: `ApiResponseModel`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `id` | `path` | `Long` | `true` |  |
| `X-User-Id` | `header` | `String` | `false` |  |

---

### DELETE /backoffice/storage/slides

Handler: `deleteSlide`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/BackofficeStorageController.java`
Returns: `ResponseEntity<BaseResponseFireBase>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `slide` | `body` | `SlideObject` | `true` |  |

Request body fields for `SlideObject`:

| Field | Type |
| --- | --- |
| `fileName` | `String` |

---

### GET /backoffice/storage/slides

Handler: `listSlides`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/BackofficeStorageController.java`
Returns: `ResponseEntity<BaseResponseFireBase>`

---

### DELETE /backoffice/storage/slides/all

Handler: `deleteAllSlides`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/BackofficeStorageController.java`
Returns: `ResponseEntity<BaseResponseFireBase>`

---

### POST /backoffice/storage/uploadPicture

Handler: `uploadPicture`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/BackofficeStorageController.java`
Returns: `ResponseEntity<BaseResponseFireBase>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `rq` | `body` | `UploadBase64Request` | `true` |  |

Request body fields for `UploadBase64Request`:

| Field | Type |
| --- | --- |
| `base64Image` | `String` |

---

### POST /backoffice/storage/uploadPictureFile

Handler: `uploadPictureFile`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/BackofficeStorageController.java`
Returns: `ResponseEntity<BaseResponseFireBase>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `file` | `param` | `MultipartFile` | `` |  |

---

### POST /backoffice/storage/uploadSlide

Handler: `uploadSlide`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/BackofficeStorageController.java`
Returns: `ResponseEntity<BaseResponseFireBase>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `file` | `param` | `MultipartFile` | `` |  |

---

### GET /bo/reports/sample-transactions.csv

Handler: `sample`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/reports/ReportController.java`
Returns: `void`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `response` | `param` | `HttpServletResponse` | `` |  |
| `walletNo` | `query` | `String` | `false` |  |

---

## Permissions

Permission catalog endpoints used for role and approval setup.

### GET /admin/permissions

Aliases:
- `/admin/permissions`
- `/api/admin/permissions`
- `/backoffice/admin/permissions`
- `/bo/admin/permissions`

Handler: `list`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/auth/controller/AdminPermissionController.java`
Returns: `ResponseEntity<List<AdminPermissionDto>>`
Permission/Security: `hasAnyAuthority('role.manage','ROLE_SUPER_ADMIN')`

---

## Reversals

Reversal exception monitoring and manual reversal request endpoints.

### GET /backoffice/reversals/cases

Handler: `listCases`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/BoReversalController.java`
Returns: `Map<String, Object>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `source` | `query` | `String` | `false` |  |
| `status` | `query` | `String` | `false` |  |
| `page` | `query` | `Integer` | `true` |  |
| `size` | `query` | `Integer` | `true` |  |

---

### POST /backoffice/reversals/cases/{source}/{caseRef}/manual-request

Handler: `requestManualReversal`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/BoReversalController.java`
Returns: `Map<String, Object>`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `source` | `path` | `String` | `true` |  |
| `caseRef` | `path` | `String` | `true` |  |
| `boAdminUserId` | `request-attribute` | `Long` | `` | @RequestAttribute("boAdminUserId") Long actorAdminId |
| `body` | `body` | `ManualReversalRequest` | `false` |  |

Request body fields for `ManualReversalRequest`:

| Field | Type |
| --- | --- |
| `notes` | `String` |

---

### GET /backoffice/reversals/summary

Handler: `getSummary`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/controller/BoReversalController.java`
Returns: `Map<String, Object>`

---

## Roles

Role management and permission assignment endpoints.

### GET /admin/roles

Aliases:
- `/admin/roles`
- `/api/admin/roles`
- `/backoffice/admin/roles`
- `/bo/admin/roles`

Handler: `list`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/auth/controller/AdminRoleController.java`
Returns: `ResponseEntity<List<AdminRoleDto>>`
Permission/Security: `hasAnyAuthority('role.manage','ROLE_SUPER_ADMIN')`

---

### POST /admin/roles

Aliases:
- `/admin/roles`
- `/api/admin/roles`
- `/backoffice/admin/roles`
- `/bo/admin/roles`

Handler: `create`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/auth/controller/AdminRoleController.java`
Returns: `ResponseEntity<AdminRoleDto>`
Permission/Security: `hasAnyAuthority('role.manage','ROLE_SUPER_ADMIN')`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `request` | `body` | `CreateRoleRequest` | `true` |  |

Request body fields for `CreateRoleRequest`:

| Field | Type |
| --- | --- |
| `name` | `String` |
| `permissionCodes` | `Set<String>` |

---

### PUT /admin/roles/{roleId}/permissions

Aliases:
- `/admin/roles/{roleId}/permissions`
- `/api/admin/roles/{roleId}/permissions`
- `/backoffice/admin/roles/{roleId}/permissions`
- `/bo/admin/roles/{roleId}/permissions`

Handler: `updatePermissions`
Source: `finacial-wealth-backoffice-service/src/main/java/com/finacial/wealth/backoffice/auth/controller/AdminRoleController.java`
Returns: `ResponseEntity<AdminRoleDto>`
Permission/Security: `hasAnyAuthority('role.manage','ROLE_SUPER_ADMIN')`

| Param | Location | Type | Required | Notes |
| --- | --- | --- | --- | --- |
| `roleId` | `path` | `Long` | `true` |  |
| `request` | `body` | `UpdateRolePermissionsRequest` | `true` |  |

Request body fields for `UpdateRolePermissionsRequest`:

| Field | Type |
| --- | --- |
| `permissionCodes` | `Set<String>` |

---

## DTO / Model Field Reference

### ActiveCampaignResponse

| Field | Type |
| --- | --- |
| `id` | `Long` |
| `title` | `String` |
| `embeddedLink` | `String` |
| `mediaKind` | `MediaKind` |
| `mediaContentType` | `String` |
| `mediaObjectName` | `String` |
| `mediaUrl` | `String` |
| `startAt` | `Date` |
| `endAt` | `Date` |
| `campaignId` | `Long` |
| `rotationSeconds` | `Integer` |
| `displayMode` | `String` |
| `items` | `List<CampaignMediaItemResponse>` |
| `status` | `CampaignStatus` |

### AdminPasswordResetResponse

| Field | Type |
| --- | --- |
| `adminId` | `Long` |
| `email` | `String` |
| `temporaryPassword` | `String` |
| `message` | `String` |

### AdminPermissionDto

| Field | Type |
| --- | --- |
| `id` | `Long` |
| `module` | `String` |
| `subModule` | `String` |
| `action` | `String` |
| `code` | `String` |
| `description` | `String` |

### AdminRoleDto

| Field | Type |
| --- | --- |
| `id` | `Long` |
| `name` | `String` |
| `permissionCodes` | `Set<String>` |

### AdminUserResponse

| Field | Type |
| --- | --- |
| `id` | `Long` |
| `email` | `String` |
| `fullName` | `String` |
| `status` | `BoAdminUser.Status` |
| `mfaEnabled` | `boolean` |
| `roles` | `Set<BoAdminRole>` |

### ApiResponse

| Field | Type |
| --- | --- |
| `code` | `String` |
| `message` | `String` |
| `data` | `T` |

### ApiResponseModel

| Field | Type |
| --- | --- |
| `statusCode` | `int` |
| `description` | `String` |
| `data` | `Object` |
| `other` | `String` |
| `externalRefrence` | `String` |
| `benefNarration` | `String` |

### ApprovalDecisionRequest

| Field | Type |
| --- | --- |
| `reason` | `String` |

### ApprovalResubmitRequest

| Field | Type |
| --- | --- |
| `notes` | `String` |

### ApproveCampaignRequest

| Field | Type |
| --- | --- |
| `note` | `String` |

### BaseResponse

| Field | Type |
| --- | --- |
| `serialVersionUID` | `long` |
| `statusCode` | `int` |
| `description` | `String` |

### BaseResponseFireBase

| Field | Type |
| --- | --- |
| `statusCode` | `int` |
| `message` | `String` |
| `data` | `Object` |

### BlockUserRequest

| Field | Type |
| --- | --- |
| `reason` | `String` |
| `performedBy` | `String` |

### CampaignAudit

| Field | Type |
| --- | --- |
| `id` | `Long` |
| `campaignId` | `Long` |
| `action` | `String` |
| `actor` | `String` |
| `eventAt` | `Date` |

### CampaignDto

| Field | Type |
| --- | --- |
| `id` | `Long` |
| `title` | `String` |
| `description` | `String` |
| `mediaObjectName` | `String` |
| `mediaContentType` | `String` |
| `mediaSignedUrl` | `String` |
| `embeddedLink` | `String` |
| `startAt` | `Date` |
| `endAt` | `Date` |
| `status` | `String` |
| `mediaKind` | `String` |
| `rotationSeconds` | `Integer` |
| `displayMode` | `String` |
| `createdBy` | `String` |
| `createdAt` | `Date` |
| `updatedBy` | `String` |
| `updatedAt` | `Date` |
| `approvedBy` | `String` |
| `approvedAt` | `Date` |

### CampaignMediaItemRequest

| Field | Type |
| --- | --- |
| `orderNo` | `Integer` |
| `objectName` | `String` |
| `embeddedLink` | `String` |

### CampaignMediaItemResponse

| Field | Type |
| --- | --- |
| `orderNo` | `Integer` |
| `mediaKind` | `String` |
| `contentType` | `String` |
| `objectName` | `String` |
| `mediaUrl` | `String` |

### ChangePasswordRequest

| Field | Type |
| --- | --- |
| `currentPassword` | `String` |
| `newPassword` | `String` |
| `confirmPassword` | `String` |

### CreateAdminUserRequest

| Field | Type |
| --- | --- |
| `email` | `String` |
| `fullName` | `String` |
| `password` | `String` |
| `confirmPassword` | `String` |
| `roles` | `Set<BoAdminRole>` |

### CreateCampaignRequest

| Field | Type |
| --- | --- |
| `title` | `String` |
| `description` | `String` |
| `mediaObjectName` | `String` |
| `mediaContentType` | `String` |
| `mediaSignedUrl` | `String` |
| `embeddedLink` | `String` |
| `startAt` | `Date` |
| `endAt` | `Date` |
| `items` | `List<CampaignMediaItemRequest>` |

### CreateReferralProgramRequest

| Field | Type |
| --- | --- |
| `programCode` | `String` |
| `title` | `String` |
| `description` | `String` |
| `productType` | `String` |
| `rewardTarget` | `String` |
| `rewardMode` | `String` |
| `rewardValue` | `BigDecimal` |
| `rewardCurrencyMode` | `String` |
| `fixedCurrencyCode` | `String` |
| `minQualifyingAmount` | `BigDecimal` |
| `minRewardAmount` | `BigDecimal` |
| `maxRewardAmount` | `BigDecimal` |
| `qualifyingTransactionCount` | `Integer` |
| `startAt` | `Date` |
| `endAt` | `Date` |

### CreateRoleRequest

| Field | Type |
| --- | --- |
| `name` | `String` |
| `permissionCodes` | `Set<String>` |

### FeaturedServiceConfigItem

| Field | Type |
| --- | --- |
| `featureKey` | `String` |
| `featureGroup` | `FeatureGroup` |
| `strategy` | `FeatureStrategy` |
| `fallbackStrategy` | `FeatureStrategy` |
| `enabled` | `Boolean` |
| `priority` | `Integer` |
| `manualTargetId` | `String` |
| `titleOverride` | `String` |
| `subtitleOverride` | `String` |
| `badge` | `String` |
| `ctaLabel` | `String` |
| `targetScreen` | `String` |
| `filters` | `Map<String, Object>` |

### FileItem

| Field | Type |
| --- | --- |
| `fileName` | `String` |
| `size` | `Long` |
| `contentType` | `String` |
| `updatedAtMs` | `Long` |
| `signedUrl` | `String` |

### InvestmentProductUpsertRequest

| Field | Type |
| --- | --- |
| `unitPrice` | `BigDecimal` |
| `yieldPa` | `BigDecimal` |
| `yieldYtd` | `BigDecimal` |
| `tenorDays` | `Integer` |
| `active` | `Boolean` |
| `liquidationFeeAppliedTo` | `LiquidationFeeAppliedTo` |
| `liquidationFeeType` | `LiquidationFeeType` |
| `liquidationFeeRate` | `BigDecimal` |
| `minLiquidationFee` | `BigDecimal` |
| `liquidationFeeCap` | `BigDecimal` |
| `lockEnabled` | `Boolean` |
| `lockDays` | `Integer` |
| `earlyLiquidationFeeAppliedTo` | `LiquidationFeeAppliedTo` |
| `earlyLiquidationFeeType` | `LiquidationFeeType` |
| `earlyLiquidationFeeRate` | `BigDecimal` |
| `earlyLiquidationFeeCap` | `BigDecimal` |
| `partnerProductCode` | `String` |
| `prospectusUrl` | `String` |
| `metaJson` | `String` |
| `enableProduct` | `String` |
| `percentageCurrValue` | `BigDecimal` |
| `scheduleMode` | `ScheduleMode` |
| `interestAccrueType` | `InterestAccrueType` |
| `interestCapitalization` | `InterestCapitalization` |
| `settlementDelayMinutes` | `Long` |
| `tenorMinutes` | `Long` |
| `maturityAtEndOfDay` | `Boolean` |
| `settlementAt` | `Instant` |
| `maturityAt` | `Instant` |

### LiquidationApprovalRequest

| Field | Type |
| --- | --- |
| `orderRef` | `String` |

### LoginRequest

| Field | Type |
| --- | --- |
| `email` | `String` |
| `password` | `String` |

### LoginStep1Response

| Field | Type |
| --- | --- |
| `status` | `String` |
| `mfaToken` | `String` |

### LoginStep2Request

| Field | Type |
| --- | --- |
| `challengeId` | `String` |
| `code` | `String` |

### ManualReversalRequest

| Field | Type |
| --- | --- |
| `notes` | `String` |

### MfaConfirmRequest

| Field | Type |
| --- | --- |
| `code` | `String` |

### MfaSetupResponse

| Field | Type |
| --- | --- |
| `qrDataUri` | `String` |
| `email` | `String` |
| `issuer` | `String` |

### MfaVerifyRequest

| Field | Type |
| --- | --- |
| `mfaToken` | `String` |
| `totpCode` | `String` |

### PagedResponse

| Field | Type |
| --- | --- |
| `content` | `List<T>` |
| `page` | `int` |
| `size` | `int` |
| `totalElements` | `long` |
| `totalPages` | `int` |
| `first` | `boolean` |
| `last` | `boolean` |

### PasswordRecoveryCompleteRequest

| Field | Type |
| --- | --- |
| `challengeId` | `String` |
| `code` | `String` |
| `newPassword` | `String` |
| `confirmPassword` | `String` |

### PasswordRecoveryStartRequest

| Field | Type |
| --- | --- |
| `email` | `String` |

### PasswordRecoveryStartResponse

| Field | Type |
| --- | --- |
| `status` | `String` |
| `challengeId` | `String` |
| `emailAddress` | `String` |
| `message` | `String` |

### ReferralProgramAuditDto

| Field | Type |
| --- | --- |
| `id` | `Long` |
| `programId` | `Long` |
| `eventType` | `String` |
| `actor` | `String` |
| `note` | `String` |
| `eventAt` | `Date` |

### ReferralProgramDto

| Field | Type |
| --- | --- |
| `id` | `Long` |
| `programCode` | `String` |
| `title` | `String` |
| `description` | `String` |
| `productType` | `String` |
| `rewardTarget` | `String` |
| `rewardMode` | `String` |
| `rewardValue` | `BigDecimal` |
| `rewardCurrencyMode` | `String` |
| `fixedCurrencyCode` | `String` |
| `minQualifyingAmount` | `BigDecimal` |
| `minRewardAmount` | `BigDecimal` |
| `maxRewardAmount` | `BigDecimal` |
| `qualifyingTransactionCount` | `Integer` |
| `status` | `String` |
| `startAt` | `Date` |
| `endAt` | `Date` |
| `createdBy` | `String` |
| `createdAt` | `Date` |
| `updatedBy` | `String` |
| `updatedAt` | `Date` |

### RegWalletInfoBackofficeResponse

| Field | Type |
| --- | --- |
| `id` | `Long` |
| `personId` | `String` |
| `firstName` | `String` |
| `lastName` | `String` |
| `middleName` | `String` |
| `fullName` | `String` |
| `email` | `String` |
| `phoneNumber` | `String` |
| `isOnboarded` | `String` |
| `activation` | `boolean` |
| `accountBankCode` | `String` |
| `bankName` | `String` |
| `bvnNumber` | `String` |
| `dateOfBirth` | `String` |
| `client` | `String` |
| `customerId` | `String` |
| `uuid` | `String` |
| `userName` | `String` |
| `emailVerification` | `boolean` |
| `emailCreation` | `String` |
| `livePhotoUpload` | `String` |
| `phoneVerification` | `String` |
| `walletTier` | `String` |
| `joinTransactionId` | `String` |
| `uuidAllowUser` | `String` |
| `accountName` | `String` |
| `walletId` | `String` |
| `accountNumber` | `String` |
| `created` | `LocalDateTime` |
| `modified` | `LocalDateTime` |
| `completed` | `boolean` |
| `referralCode` | `String` |
| `referralCodeLink` | `String` |
| `isUserBlocked` | `String` |
| `createdDate` | `Instant` |
| `lastModifiedDate` | `Instant` |

### SlideObject

| Field | Type |
| --- | --- |
| `fileName` | `String` |

### TokenResponse

| Field | Type |
| --- | --- |
| `accessToken` | `String` |
| `refreshToken` | `String` |
| `emailAddress` | `String` |
| `fullName` | `String` |
| `userRole` | `String` |
| `adminId` | `Long` |

### UpdateAdminUserRequest

| Field | Type |
| --- | --- |
| `fullName` | `String` |
| `roles` | `Set<BoAdminRole>` |

### UpdateCampaignRequest

| Field | Type |
| --- | --- |
| `title` | `String` |
| `description` | `String` |
| `mediaObjectName` | `String` |
| `mediaContentType` | `String` |
| `mediaSignedUrl` | `String` |
| `embeddedLink` | `String` |
| `startAt` | `Date` |
| `endAt` | `Date` |
| `rotationSeconds` | `Integer` |
| `displayMode` | `String` |
| `items` | `List<CampaignMediaItemRequest>` |

### UpdateReferralProgramRequest

| Field | Type |
| --- | --- |
| `title` | `String` |
| `description` | `String` |
| `rewardTarget` | `String` |
| `rewardMode` | `String` |
| `rewardValue` | `BigDecimal` |
| `rewardCurrencyMode` | `String` |
| `fixedCurrencyCode` | `String` |
| `minQualifyingAmount` | `BigDecimal` |
| `minRewardAmount` | `BigDecimal` |
| `maxRewardAmount` | `BigDecimal` |
| `qualifyingTransactionCount` | `Integer` |
| `startAt` | `Date` |
| `endAt` | `Date` |

### UpdateRolePermissionsRequest

| Field | Type |
| --- | --- |
| `permissionCodes` | `Set<String>` |

### UploadBase64Request

| Field | Type |
| --- | --- |
| `base64Image` | `String` |
