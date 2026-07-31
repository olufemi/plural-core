# Plural Mobile App API Reference

This document is a single FE/mobile-facing API reference for the Plural mobile app. It is compiled from the current service controllers in this repository.

Last updated: 2026-06-24

## Base URLs

Use the API gateway public domain in mobile builds.

```text
Production base: https://finacialwealth.com
```

Service path prefixes may be routed by the gateway. In this repo, the controller paths below are the application paths. If a gateway route adds a service prefix, keep the same controller path after that prefix.

Common gateway examples:

```text
https://finacialwealth.com/session/authenticate/customer-mobile
https://finacialwealth.com/walletmgt/get-customer-details
https://finacialwealth.com/localtransfer/transfer
https://finacialwealth.com/offers/get-all-live-offers
```

## Common Headers

Authenticated APIs use the customer JWT:

```http
authorization: Bearer <accessToken>
Content-Type: application/json
```

Some endpoints require:

```http
channel: MOBILE
```

Some sensitive endpoints may require device consent/signature headers when backend cryptography enforcement is enabled. Use the exact request body bytes when signing because several endpoints verify the raw request body.

Planned/standard sensitive-request headers:

```http
X-Device-Id: <deviceId>
X-Request-Id: <unique request id/process id>
X-Request-Timestamp: <ISO-8601 timestamp or epoch millis, based on client implementation>
X-Request-Signature: <base64 signature>
```

## Common Response Shape

Most mobile APIs return one of these shapes:

```json
{
  "statusCode": 200,
  "description": "Success message",
  "data": {}
}
```

or:

```json
{
  "statusCode": 400,
  "description": "Validation or business error",
  "data": null
}
```

## Authentication And Session

Source service: `finacial-wealth-api-sessionmanager`

### POST `/session/authenticate/customer-mobile`

Customer mobile login.

Headers:

```http
channel: MOBILE
Content-Type: application/json
```

Request:

```json
{
  "emailAddress": "customer@example.com",
  "password": "Pass$$123",
  "uuid": "314EB75C-3A66-48BA-B5B1-4D81CC94C181",
  "appType": null,
  "pushNotificationToken": "firebase-token",
  "appVersion": "1.0",
  "deviceId": "314EB75C-3A66-48BA-B5B1-4D81CC94C181",
  "devicePublicSpkiB64": "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE..."
}
```

Notes:

- `deviceId` is the stable app/device installation identifier.
- `uuid` is the legacy device/user identifier. Existing mobile clients may send the same value for `uuid` and `deviceId`.
- `devicePublicSpkiB64` is the device public key for later signed request enforcement.
- Response contains the JWT access token and customer/session details.

### GET `/session/logout`

Logout and revoke the current JWT.

Headers:

```http
authorization: Bearer <accessToken>
```

## Profiling, Onboarding, Account And PIN

Source service: `finacial-wealth-api-profiling`

### GET `/walletmgt/get-countries`

Fetch supported country details.

Headers:

```http
authorization: Bearer <accessToken>
```

### POST `/walletmgt/create-user`

Onboard customer through the SDK flow.

Request DTO: `OnBoardUserForSDK`.

Common fields include:

```json
{
  "uuid": "device-or-user-uuid"
}
```

Use the current app onboarding payload for the remaining identity/provider fields.

### POST `/walletmgt/validate/bvn`

Validate BVN.

Headers:

```http
authorization: Bearer <accessToken>
```

Request:

```json
{
  "bvn": "12345678901"
}
```

### GET `/validate/to/{bvn}`

Direct BVN validation route.

Path param:

```text
bvn=<11 digit BVN>
```

Notes:

- This exists in the profiling service under `BvnController`.
- Prefer `/walletmgt/validate/bvn` if the mobile app already uses the wallet-management flow.

### POST `/walletmgt/add-other-currency-account`

Create or attach another currency account for the customer.

Headers:

```http
authorization: Bearer <accessToken>
```

Request DTO: `AddAccountObj`.

### POST `/walletmgt/validate-pin`

Validate customer transaction PIN.

Headers:

```http
authorization: Bearer <accessToken>
```

Request DTO: `WalletNo`.

### GET `/walletmgt/initiate-create-pin`

Start create-PIN OTP process.

Headers:

```http
authorization: Bearer <accessToken>
```

### POST `/walletmgt/create-pin`

Complete create-PIN.

Headers:

```http
authorization: Bearer <accessToken>
```

Request DTO: `CreatePinOtp`.

### GET `/walletmgt/initiate-reset-pin`

Start reset-PIN OTP process.

Headers:

```http
authorization: Bearer <accessToken>
```

### POST `/walletmgt/reset-pin`

Complete reset-PIN.

Headers:

```http
authorization: Bearer <accessToken>
```

Request DTO: `CreatePinOtp`.

### POST `/walletmgt/initiate-forget-password`

Start forgot-password flow.

Request DTO: `InitiateForgetPwdDataWallet`.

### POST `/walletmgt/change-password`

Complete forgot-password or password-change flow outside an active app session.

Request DTO: `ChangePasswordRequest`.

### POST `/walletmgt/change-password-in-app`

Change password inside an authenticated app session.

Headers:

```http
authorization: Bearer <accessToken>
```

Request DTO: `ChangePasswordInApp`.

Notes:

- Sensitive endpoint. Backend may require raw-body consent/signature headers.

### POST `/walletmgt/change-pin-in-app`

Change PIN inside an authenticated app session.

Headers:

```http
authorization: Bearer <accessToken>
```

Request DTO: `ChangePinInApp`.

### POST `/walletmgt/resend-otp-new`

Resend OTP.

Headers:

```http
channel: MOBILE
```

Request DTO: `OtpResendRequest`.

### GET `/walletmgt/get-customer-details`

Fetch logged-in customer profile and wallet details.

Headers:

```http
authorization: Bearer <accessToken>
```

### POST `/walletmgt/get-account-bal`

Fetch account balance from wallet/core.

Request:

```json
{
  "auth": "account-or-wallet-auth-value"
}
```

### POST `/walletmgt/initiate-request-device-change`

Start device change flow.

Request DTO: `UserDeviceReqChange`.

### POST `/walletmgt/change-device`

Complete device change flow.

Request DTO: `ChangeDevice`.

## Countries And Limits

Source service: `finacial-wealth-api-profiling`

### GET `/countries/all/existing`

List configured countries.

### GET `/countries/without-currency`

List countries without currency details.

### POST `/countries/validate/country-code`

Validate country code.

### POST `/countries/validate`

Validate country details.

### GET `/v1/profiling/limits`

Get profiling limits.

### POST `/v1/profiling/limits/account`

Get account-specific profiling limits.

## Local Transfer, Interbank And Transaction History

Source service: `finacial-wealth-api-transactions`

### GET `/interbank/get-banks`

Fetch supported banks for interbank transfer.

Headers:

```http
authorization: Bearer <accessToken>
```

### POST `/interbank/name-enquiry`

Resolve external bank account name.

Headers:

```http
authorization: Bearer <accessToken>
```

Request DTO: `NameLookUpInterBank`.

### POST `/interbank/make-payment`

Make interbank transfer.

Headers:

```http
authorization: Bearer <accessToken>
```

Request DTO: `OtherBankTransferRequest`.

Notes:

- Sensitive endpoint. Backend verifies raw request body consent/signature when enabled.

### POST `/interbank/validate-pin`

Validate PIN for the Transfaar/interbank quote flow.

Headers:

```http
authorization: Bearer <accessToken>
```

Request DTO: `WalletNo`.

### GET `/interbank/pending-accepted`

List pending accepted Transfaar quotes for logged-in user.

Headers:

```http
authorization: Bearer <accessToken>
```

Query params:

```text
page=0
size=20
```

### POST `/interbank/pending-accepted-quoteid`

Get one pending accepted quote by quote ID.

Headers:

```http
authorization: Bearer <accessToken>
```

Request:

```json
{
  "quoteId": "quote-id"
}
```

### POST `/interbank/create-quote`

Create Transfaar/CAD deposit quote.

Headers:

```http
authorization: Bearer <accessToken>
```

Request DTO: `CreateQuoteFE`.

### POST `/interbank/accept-quote`

Accept Transfaar/CAD deposit quote.

Headers:

```http
authorization: Bearer <accessToken>
```

Request DTO: `AcceptQuoteFE`.

Notes:

- Sensitive endpoint. Backend may require raw-body consent/signature headers.

### POST `/interbank/create-quote-withdrawal`

Create Transfaar/CAD withdrawal quote.

Headers:

```http
authorization: Bearer <accessToken>
```

Request DTO: `CreateQuoteWithdrawalFE`.

### POST `/interbank/accept-quote-withdrawal`

Accept Transfaar/CAD withdrawal quote.

Headers:

```http
authorization: Bearer <accessToken>
```

Request DTO: `AcceptQuoteFE`.

Notes:

- Sensitive endpoint. Backend may require raw-body consent/signature headers.

### POST `/localtransfer/name-enquiry`

Resolve wallet-to-wallet/local transfer receiver.

Headers:

```http
authorization: Bearer <accessToken>
```

Request:

```json
{
  "receiver": "receiverWalletIdOrAccountNumber",
  "currencyCode": "NGN"
}
```

Notes:

- `currencyCode` is important for multi-currency support.
- Current supported values include `CAD` and `NGN`.

### POST `/localtransfer/transfer`

Wallet-to-wallet/local transfer.

Headers:

```http
authorization: Bearer <accessToken>
```

Request:

```json
{
  "currencyCode": "NGN",
  "receiver": "receiverNairaAccountNumber",
  "amount": "3000",
  "theNarration": "Transfer",
  "processId": "unique-client-process-id"
}
```

CAD example:

```json
{
  "currencyCode": "CAD",
  "receiver": "receiverWalletIdOrCadAccount",
  "amount": "3000",
  "theNarration": "Transfer",
  "processId": "unique-client-process-id"
}
```

Notes:

- Sensitive endpoint. Backend may require consent/signature headers.
- FE should always send `currencyCode` going forward.

### POST `/localtransfer/transfer/non-pin`

Local transfer variant without PIN in request.

Headers:

```http
authorization: Bearer <accessToken>
```

Request DTO: `LocalTransferRequest`.

Notes:

- Sensitive endpoint. Backend logs and verifies canonical payload/hash.

### POST `/localtransfer/save-beneficiary`

Save local transfer beneficiary.

Headers:

```http
authorization: Bearer <accessToken>
```

Request DTO: `SaveBeneficiary`.

### GET `/localtransfer/find-beneficiary`

List saved local transfer beneficiaries.

Headers:

```http
authorization: Bearer <accessToken>
```

### GET `/otherbanktransfer/find-saved-beneficiaries`

List saved interbank beneficiaries.

Headers:

```http
authorization: Bearer <accessToken>
channel: MOBILE
```

### POST `/otherbanktransfer/save-beneficiary`

Save interbank beneficiary.

Headers:

```http
authorization: Bearer <accessToken>
```

Request DTO: `SaveBeneficiary`.

### POST `/get-transactions-history`

Fetch customer transaction history.

Headers:

```http
authorization: Bearer <accessToken>
```

Request:

```json
{
  "memberId": "08162325876"
}
```

Important FE display rule:

```text
entryDirection == CREDIT  => show positive/green/no minus
entryDirection == DEBIT   => show negative/red/minus
entryDirection == UNKNOWN => neutral/fallback
```

Do not infer credit/debit from `transactionType`, `paymentType`, title, sender, receiver, or currency.

Sample response item:

```json
{
  "id": 631,
  "historyEntryId": "631",
  "entryDirection": "CREDIT",
  "counterparty": "Olufemi Oshin",
  "transactionType": "Deposit",
  "paymentType": "Deposit to Account",
  "ammount": 4444.00,
  "transactionDate": "Jun 24, 2026",
  "currencyCode": "NGN",
  "status": "SUCCESS"
}
```

## Group Savings

Source service: `finacial-wealth-api-transactions`

All endpoints below use:

```http
authorization: Bearer <accessToken>
```

Some mutating endpoints are consent/signature protected when enabled.

### GET `/group-savings/configued-members-number-data`

Fetch configured group member count options.

### POST `/group-savings/initiate-group-savings`

Start group savings creation.

Request DTO: `InitiateGroupSavingsV2`.

### POST `/group-savings/confirm-create-transaction`

Confirm group savings creation transaction.

Request DTO: `GroupSavingConf`.

### POST `/group-savings/activate-group`

Activate group.

Request DTO: `GroupSavingActivation`.

### POST `/group-savings/add-members`

Add members to group.

Request DTO: `AddedMembersFE`.

### POST `/group-savings/join-group`

Join a group.

Request DTO: `JoinGroupRequest`.

### POST `/group-savings/leave-group`

Leave a group.

Request DTO: `LeaveGroupRequest`.

### POST `/group-savings/delete-group-saving`

Delete group savings.

Request DTO: `GroupSavingConf`.

### POST `/group-savings/get-user-request-details`

Get group request details for a user.

Request DTO: `ReByEmailAddress`.

### POST `/group-savings/get-user-initiated-group-savings`

Get group savings initiated by the logged-in user.

Request DTO: `ReByEmailAddress`.

### POST `/group-savings/get-all-transactions`

Get group savings transactions.

Request DTO: `ReByEmailAddress`.

### GET `/group-savings/validate-has-pin`

Check whether customer has a PIN.

### GET `/group-savings/get-member-swap-slot-notification-request`

Get swap-slot notification requests for logged-in member.

### POST `/group-savings/get-group-savings-transaction-slots`

Get group savings slots.

Request DTO: `ReByInvitationCode`.

### POST `/group-savings/send-swap-slot-request`

Send slot swap request.

Request DTO: `SwapSlotReq`.

### POST `/group-savings/accept-or-decline-swap-slot-request`

Accept or decline slot swap request.

Request DTO: `AcceptDeclineSwapSlotReq`.

### POST `/group-savings/get-customer-info`

Get customer info for group savings.

Request DTO: `GetMemBerDe`.

## Push Notifications

Source service: `finacial-wealth-api-transactions`

### GET `/push`

List notification inbox.

Query params:

```text
userId=<walletId>
page=0
size=20
unreadOnly=true
```

### POST `/push/{userNotificationId}/read`

Mark notification as read.

Query params:

```text
userId=<walletId>
```

### POST `/push/register`

Register/update device FCM token.

Request:

```json
{
  "userId": "1799204705",
  "platform": "ANDROID",
  "deviceToken": "firebase-token"
}
```

### POST `/push/sendToUser`

Send notification to supplied token. This is mostly an internal/helper endpoint.

### POST `/push/send`

Send notification to one supplied FCM token. This is mostly an internal/helper endpoint.

Request:

```json
{
  "deviceToken": "firebase-token",
  "title": "Title",
  "body": "Message",
  "data": {
    "type": "ALERT"
  }
}
```

### POST `/push/sendToUser-default`

Fan out a notification to all stored devices for a user. This is mostly an internal/helper endpoint.

Request:

```json
{
  "userId": "1799204705",
  "title": "Title",
  "body": "Message",
  "data": {
    "type": "ALERT"
  }
}
```

## Airtime, Bills And Featured Services

Source service: `finacial-wealth-api-fxpeer-exchange`

All app-facing routes below use:

```http
authorization: Bearer <accessToken>
```

### GET `/fxothers/services/featured`

Fetch featured services configuration for the mobile Services screen.

### POST `/fxothers/get-all-products`

Fetch products.

Request DTO: `GetProducts`.

### GET `/fxothers/int-utilities-get-categories`

Fetch international utilities categories.

### POST `/fxothers/int-utilities-get-products`

Fetch international utility products by category.

Request DTO: `GetProductsByCatId`.

### POST `/fxothers/int-utilities-get-products-by-country`

Fetch international utility products by country.

Request DTO: `GetProductsByCountry`.

### POST `/fxothers/airtime-validate-phonenumber`

Validate airtime phone number.

Request DTO: `ValidatePhoneNumber`.

### POST `/fxothers/int-utilities-validate-accountid`

Validate utility account ID.

Request DTO: `ValidateAccount`.

### POST `/fxothers/int-utilities-fulfilment`

Fulfil airtime/bills/international utility transaction.

Request DTO: `ProcessTrnsactionReq`.

Notes:

- Sensitive endpoint. Backend may require raw-body consent/signature headers.
- For provider failures and reversals, backend reversal monitoring handles rollback cases.

## FX Peer Offers And Orders

Source service: `finacial-wealth-api-fxpeer-exchange`

All main mobile endpoints use:

```http
authorization: Bearer <accessToken>
```

### GET `/offers/get-all-live-offers`

List all live offers.

Query/page params supported by Spring pageable:

```text
page=0&size=20&sort=id,DESC
```

### GET `/offers/get-all-other-offers`

List live offers excluding logged-in user.

### GET `/offers/get-all-my-offers`

List logged-in user offers.

### POST `/offers/create-offer`

Create FX peer offer.

Request DTO: `CreateOfferCaller`.

Notes:

- Sensitive endpoint. Consent/signature may be required when cryptography for PIN is enabled.

### POST `/offers/update-offer`

Update FX peer offer.

Request DTO: `UpdateOfferCallerReq`.

### POST `/offers/cancel-offer`

Cancel FX peer offer.

Request DTO: `CancelOfferCallerReq`.

### GET `/offers/{id}`

Get one offer. Uses `X-User-Id` in the lower-level endpoint; mobile should prefer the JWT-backed endpoints above unless the app already uses this route.

### PATCH `/offers/{id}/rate`

Update offer rate. Lower-level endpoint using `X-User-Id`.

### POST `/offers/{id}/cancel`

Cancel offer. Lower-level endpoint using `X-User-Id`.

### GET `/orders/get-all-customer-transactions`

Get logged-in customer FX peer transactions.

### POST `/orders/buy-offer-now`

Buy an FX peer offer.

Request DTO: `BuyOfferNow`.

Notes:

- Sensitive endpoint. Consent/signature may be required.

### POST `/orders/offers/{offerId}/buy-now`

Lower-level buy-now endpoint using `X-User-Id`.

Request:

```json
{
  "amount": 1000,
  "lockTtlSeconds": 600
}
```

### POST `/orders/orders/{orderId}/escrow/init`

Initialize escrow for an order. Usually backend/internal orchestration.

## Escrow

Source service: `finacial-wealth-api-fxpeer-exchange`

These are lower-level escrow endpoints. Mobile should normally use the offer/order flow unless the escrow screen explicitly needs these.

### GET `/api/escrows/{id}`

Get escrow by ID.

### POST `/api/escrows/{id}/fund/buyer`

Mark/fund buyer leg.

Headers:

```http
Idempotency-Key: <unique-key>
```

### POST `/api/escrows/{id}/fund/seller`

Mark/fund seller leg.

Headers:

```http
Idempotency-Key: <unique-key>
```

### POST `/api/escrows/{id}/release/buyer`

Confirm buyer release.

### POST `/api/escrows/{id}/release/seller`

Confirm seller release.

## FX Peer Marketplace, Negotiation, Ratings And Receipts

Source service: `finacial-wealth-api-fxpeer-exchange`

### GET `/api/market/offers`

Marketplace offers.

### POST `/api/negs`

Create negotiation.

### POST `/api/negs/{id}/accept`

Accept negotiation.

### POST `/api/negs/{id}/decline`

Decline negotiation.

### POST `/api/ratings`

Submit seller rating.

### GET `/api/sellers/{sellerId}/ratings`

List seller ratings.

### GET `/api/sellers/{sellerId}/stats`

Get seller rating stats.

### GET `/api/orders/{orderId}/receipt/buyer`

Buyer receipt HTML.

### GET `/api/orders/{orderId}/receipt/seller`

Seller receipt HTML.

### GET `/api/orders/{orderId}/receipt.pdf`

Receipt PDF.

### POST `/api/orders/{orderId}/receipt/email`

Email receipt.

## Investments

Source service: `finacial-wealth-api-fxpeer-exchange`

All mobile investment endpoints use:

```http
authorization: Bearer <accessToken>
```

### GET `/investments/get-products`

List mobile-facing investment products.

### POST `/investments/get-redemptions`

Fetch investment redemptions.

Request DTO: `RedemptionInvestmentRequest`.

### POST `/investments/create-subscription`

Create investment subscription/order.

Request DTO: `CreateSubscriptionReq`.

Notes:

- Sensitive endpoint. Consent/signature may be required.

### POST `/investments/request-liquidation`

Request liquidation/redemption.

Request DTO: `LiquidateInvestmentRequest`.

Behavior:

- When a redemption is requested, the requested amount is reserved immediately against the investment position.
- The customer-facing available value should use `availableInvestmentAmount`.
- Existing `marketValue` remains the gross investment value for backward compatibility.
- A pending redemption can appear before the wallet is credited; wallet credit happens only after automatic or backoffice approval settles the redemption.
- No mobile request/response shape change is required for notifications. FxPeer now emits redemption lifecycle events internally and Utility sends email/push where configured.

Customer notifications:

- Request received: sent after the redemption request is accepted and reserved.
- Redemption completed: sent after settlement completes and the wallet is credited.
- Redemption cancelled: sent after a pending/processing redemption is cancelled and the reserved amount is released.

Redemption policy:

- Backoffice governs settlement behavior through maker-checker protected `app_config` keys.
- `investment.redemption.approval-mode=AUTO` settles all pending redemptions from the scheduler.
- `investment.redemption.approval-mode=MANUAL` leaves pending redemptions for backoffice approval.
- `investment.redemption.approval-mode=THRESHOLD` auto-settles redemptions up to `investment.redemption.auto-approval-threshold` and leaves higher amounts pending.
- Environment variables remain fallback values only if these `app_config` keys are absent.

### POST `/investments/request-top-up`

Top up existing investment.

Request DTO: `InvestmentTopupRequestCaller`.

### GET `/investments/get-customer-history`

Get logged-in customer investment history.

### GET `/investments/get-customer-investment-position`

Get logged-in customer current investment positions.

Important response fields:

```json
{
  "marketValue": 155000,
  "grossInvestmentAmount": 155000,
  "reservedRedemptionAmount": 55000,
  "reservedLiquidationAmount": 55000,
  "availableInvestmentAmount": 100000,
  "settledRedemptionAmount": 0
}
```

Display guidance:

- Use `availableInvestmentAmount` for customer-facing “available investment” or “redeemable balance”.
- Use `grossInvestmentAmount` or existing `marketValue` only where the UI intentionally wants to show the total gross position before pending redemption holds.
- Show pending redemption history from the liquidation/redemption endpoints while status is `LIQUIDATION_PENDING_APPROVAL` or `LIQUIDATION_PROCESSING`.

### GET `/investments/orders/all-liquidation-settled`

Get settled liquidation orders for customer.

### GET `/investments/orders/all-liquidation-processing`

Get processing liquidation orders for customer.

## Campaigns And Featured Content

Source service: `finacial-wealth-api-profiling`

### GET `/walletmgt/campaigns/active`

Fetch active mobile campaigns.

### POST `/campaign-upload-mgt/storage/getSlide/{fileName}`

Get uploaded campaign slide by filename.

### GET `/campaign-upload-mgt/storage/getSlide/{fileName}`

Get uploaded campaign slide by filename.

### GET `/campaign-upload-mgt/storage/getSlides`

List uploaded slides.

### POST `/campaign-upload-mgt/storage/...`

Campaign storage upload routes exist for admin/content upload flows. They are not normally mobile-consumed except for reading active campaign media.

### DELETE `/campaign-upload-mgt/storage/delete-slide`

Delete one campaign slide. Admin/content operation.

### DELETE `/campaign-upload-mgt/storage/deleteAllSlides`

Delete all campaign slides. Admin/content operation.

## Referral Program Runtime

Source service: `finacial-wealth-api-profiling`

These endpoints are product/runtime referral APIs. Mobile should use them only where referral UX is enabled.

### POST `/referral-programs/runtime/apply`

Apply referral code/program attribution.

### POST `/referral-programs/runtime/qualify`

Check referral qualification.

### POST `/referral-programs/runtime/{attributionId}/complete`

Complete referral attribution.

## Device Crypto And Consent Helpers

Source service: `finacial-wealth-api-fxpeer-exchange`

These endpoints support device-bound signatures and receipt signing. Mobile may need these for secure transaction approval flows.

### POST `/canonical/crypto/login-key`

Register or update the login/device public key.

Request DTO: `DeviceLoginKeyRequest`.

### POST `/canonical/crypto/bind/confirm-otp`

Confirm device binding OTP.

### POST `/canonical/crypto/consent/verify`

Verify signed consent payload.

### GET `/canonical/crypto/keys`

Fetch public receipt/crypto keys.

### POST `/canonical/crypto/sign`

Sign canonical payload/receipt. Usually backend/internal unless mobile explicitly uses it for debugging or delegated signing.

### POST `/canonical/crypto/{txId}/challenge`

Create a device transaction approval challenge.

Headers:

```http
X-Device-Id: <deviceId>
```

Notes:

- Current controller implementation has a path-variable mismatch for `emailAddress`; verify before mobile depends on this route.

### POST `/canonical/crypto/{txId}/approve`

Approve a device transaction challenge.

Request DTO: `TxApproveRequest`.

Expected fields include:

```json
{
  "emailAddress": "customer@example.com",
  "deviceId": "device-id",
  "deviceKid": "device-key-id",
  "challengeId": "challenge-id",
  "alg": "ES256",
  "sigB64": "base64-signature"
}
```

## Provider Callbacks And Internal/Admin Routes

Do not call these from the mobile app unless specifically instructed. They are provider callbacks, recovery tools, admin tools, or backoffice/internal APIs.

### Transaction provider callbacks

```text
POST /interbank/webhook
POST /webhooks/transfaar/payment-deposit
```

### Transaction admin/recovery

```text
GET  /admin/reversals/summary
GET  /admin/reversals
POST /admin/reversals/{transactionId}/retry
GET  /admin/transfaar/payment-deposit/recovery-candidates
POST /admin/transfaar/payment-deposit/replay/{quoteId}
GET  /admin/group-savings/contribution-payout-monitoring
GET  /admin/group-savings/slot-assignment-tracking
GET  /admin/group-savings/groups
GET  /admin/group-savings/groups/{groupId}
POST /admin/group-savings/groups/{groupId}/close
```

### FXPeer utility/admin

```text
GET  /fxothers/admin/featured-services-config
POST /fxothers/admin/featured-services-config
GET  /fxothers/admin/airtime-reversals/summary
GET  /fxothers/admin/airtime-reversals
POST /fxothers/admin/airtime-reversals/{processId}/retry
GET  /fxothers/__ping
GET  /investments/__ping
```

### Internal wallet/core bridge

```text
POST /internal/get-account-bal
POST /internal/get-account-bal-phone
POST /peer-to-peer/create-offer-validate-account
POST /peer-to-peer/validate-balance
POST /peer-to-peer/debit-customer-with-type
POST /peer-to-peer/credit-customer-with-type
POST /peer-to-peer/batch-post-with-type
```

### Referral program admin/config

```text
POST /referral-programs
PUT  /referral-programs/{id}
POST /referral-programs/{id}/activate
POST /referral-programs/{id}/pause
POST /referral-programs/{id}/end
GET  /referral-programs
GET  /referral-programs/{id}
GET  /referral-programs/{id}/audit
GET  /referral-programs/active
```

### Campaign admin/config

```text
POST /campaigns
PUT  /campaigns/{id}
POST /campaigns/{id}/stop
POST /campaigns/{id}/cancel
POST /campaigns/{id}/restart
POST /campaigns/{id}/approve
GET  /campaigns
GET  /campaigns/{id}
GET  /campaigns/{id}/audit
```

### Backoffice/profile admin

```text
GET   /profiles
GET   /profiles/{id}
GET   /profiles/customer/{customerId}
GET   /profiles/uuid/{uuid}
GET   /profiles/filter
PATCH /profiles/{id}/block
PATCH /profiles/{id}/unblock
```

### Investment admin

```text
POST /investments/create-product
PUT  /investments/update-product/{productCode}
GET  /investments/admin/products
GET  /investments/admin/liquidations
GET  /investments/admin/liquidations/history
GET  /investments/admin/orders
GET  /investments/admin/performance
GET  /investments/admin/oversight
GET  /investments/admin/customers/orders
GET  /investments/admin/customers/liquidations
GET  /investments/admin/customers/positions
POST /investments/orders/liquidation/approve
POST /investments/orders/liquidation/cancel
```

## Current Mobile Integration Notes

- Use `authorization` lowercase header where existing controllers require it. Some services also accept standard `Authorization`, but current code often requests lowercase `authorization`.
- Keep reading transaction amount from `ammount` until backend adds a backward-compatible `amount` alias.
- Always use `entryDirection` for transaction-history sign and color.
- Always send `currencyCode` for local transfer. `NGN` and `CAD` are currently supported.
- Use a unique `processId` for mutating/sensitive operations where the DTO supports it.
- If a sensitive endpoint returns a consent/signature error, the app should retry only after generating the expected signed headers for the exact same request body.
