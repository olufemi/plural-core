# Go-Live Readiness Checklist

## Purpose

This checklist turns the current solution state into a practical pilot and production readiness pack for the July 1 rollout target.

It is meant to answer:

- what must be in the release baseline
- which services must be deployed together
- which DB tables, columns, and seed rows must exist
- which config values must be prepared per environment
- which smoke tests must pass in pilot before production

## Release Baseline

- Stable rollout branch: `feature/after-first-user-experience-test-on-dev`
- Long-running architecture branch: `codex/multi-market-onboarding-architecture-hardening`
- Rule: all go-live packaging, QA, pilot fixes, and production deployment preparation should be driven from the stable branch
- Rule: after every stable-branch fix, merge stable into the long-running architecture branch on the same day

## Solution Areas In Scope

The current release shape spans these areas:

- multi-market onboarding and market orchestration
- FXPeer market-aware adoption for NG and CAD paths
- referral program runtime and admin support
- investment valuation-mode guard and legacy safety
- dashboard/login referral-code exposure
- backoffice product-detail compatibility and referral-program admin APIs

## Services To Treat As A Coordinated Release Group

### Must verify and package together

- `finacial-wealth-api-profiling`
- `finacial-wealth-api-fxpeer-exchange`
- `finacial-wealth-api-sessionmanager`
- `finacial-wealth-api-utility`
- `finacial-wealth-backoffice-service`

### Critical dependency to verify even if not newly changed

- `finacial-wealth-api-transactions`

Reason:

- referral reward payout depends on the existing transactions batch endpoint
- FXPeer uses `/peer-to-peer/batch-post-with-type`
- a healthy transactions deployment is required even if that service is not reworked heavily in this release

## Pre-Freeze Hygiene

- [ ] Remove untracked build artifacts from source trees before packaging
- [ ] Specifically clean `finacial-wealth-api-profiling/BOOT-INF/`
- [ ] Confirm there are no local-only temporary DB workarounds left undocumented
- [ ] Confirm packaged jars/images were built from the stable branch head
- [ ] Record exact git commit/hash for each service artifact sent to pilot or prod

## Java And Build Compatibility

- `finacial-wealth-api-profiling` runs on Java 8
- `finacial-wealth-api-fxpeer-exchange` runs on Java 21
- `finacial-wealth-backoffice-service` runs on Java 21
- `sessionmanager`, `utility`, and `transactions` must be verified against the current environment JDK/runtime actually used in pilot/prod

- [ ] Confirm build agents and deployment hosts use the correct Java version per service
- [ ] Confirm no Java 11+ APIs leaked into profiling
- [ ] Confirm final artifacts were built without stale Docker/cache layers

## Schema And Data Preparation

## 1. Profiling Market Tables

### `MARKET_DEFINITION`

Purpose:

- canonical market metadata for multi-market orchestration

Required seeded rows:

- `CA_RETAIL`
- `NG_RETAIL`

Key expectations:

- bootstrap upserts both rows at startup
- table audit columns are strict and must remain healthy:
  - `created_by`
  - `created_date`

- [ ] Verify `MARKET_DEFINITION` exists
- [ ] Verify `CA_RETAIL` row exists and is enabled
- [ ] Verify `NG_RETAIL` row exists and is enabled
- [ ] Verify bootstrap logs show both rows being ensured on startup
- [ ] Verify `created_by` and `created_date` are no longer being inserted as null

### `CUSTOMER_MARKET_PROFILE`

Purpose:

- per-customer multi-market lifecycle tracking

Expected fields to validate in pilot data:

- `customerId`
- `marketCode`
- `status`
- `kycStatus`
- `accountProvisionStatus`
- `walletProvisionStatus`
- `smartCoreCustomerId`
- `smartCoreAccountId`
- `walletId`
- `localAccountNumber`
- `virtualAccountNumber`

- [ ] Verify table exists
- [ ] Verify new CAD/NG customer flows create or update profiles
- [ ] Verify legacy customers entering NG add-account flow get usable market profiles

## 2. Profiling Referral Tables

### `referral_programs`

Purpose:

- product-scoped referral program configuration

Required readiness:

- at least one active `P2P` program for pilot if P2P referral is expected to work

Key enum-backed fields:

- `product_type`: `P2P`, `AIRTIME`, `INVESTMENT`
- `reward_target`: `REFERRER_ONLY`, `REFEREE_ONLY`, `BOTH`
- `reward_mode`: `FLAT_AMOUNT`, `PERCENTAGE_OF_TRANSACTION`
- `reward_currency_mode`: `TRADE_CURRENCY`, `FIXED_CURRENCY`
- `status`: `DRAFT`, `ACTIVE`, `PAUSED`, `ENDED`

- [ ] Verify table exists
- [ ] Verify active program row exists for `P2P` if referral is being piloted
- [ ] Verify `program_code` is unique
- [ ] Verify `qualifying_transaction_count`, reward values, and date windows are correct

### `referral_attributions`

Purpose:

- applied referral state, qualification, payout status, and idempotency

Key enum-backed fields:

- `status`: `APPLIED`, `QUALIFIED_PENDING_PAYOUT`, `REWARDED`, `CANCELLED`

Key operational fields:

- `qualified_transaction_id`
- `qualified_amount`
- `qualified_currency_code`
- `reward_currency_code`
- `referrer_reward_amount`
- `referee_reward_amount`
- `referrer_reward_paid`
- `referee_reward_paid`
- `referrer_payout_reference`
- `referee_payout_reference`

- [ ] Verify table exists
- [ ] Verify uniqueness by `(referee_wallet_id, product_type)`
- [ ] Verify payout references are written after reward posting
- [ ] Verify failed payout leaves attribution pending instead of corrupting trade flow

### `referral_program_audit`

- [ ] Verify table exists
- [ ] Verify create/update/activate/pause/end actions are being audited

## 3. FXPeer Investment Table Change

### `fx_investment_position`

Required column:

- `valuation_method`

Purpose:

- pin valuation mode to the position instead of the mutable product

- [ ] Verify column exists in every environment
- [ ] Verify new positions stamp `valuation_method`
- [ ] Verify legacy positions with null valuation method are still valued safely

## 4. Existing Customer Referral Fields

The release relies on customer referral fields already present on wallet/customer records.

- [ ] Verify `referralCode` and `referralCodeLink` exist on stored customer data
- [ ] Verify legacy users without these values are backfilled lazily on login
- [ ] Verify malformed historic links are corrected on login

## Environment Config Inventory

## 1. App Config Rows

### Referral link base

- `onboardRefLink`

Expected behavior:

- login/onboarding should produce a normalized share link such as `https://.../<code>`

- [ ] Dev value present
- [ ] Pilot value present
- [ ] Prod value present
- [ ] Value format verified so link concatenation is correct

### Referral funding GL config

These are required when referral reward payout is enabled:

- `NGN_REFERRAL_GGL_ACCOUNT`
- `NGN_REFERRAL_GGL_CODE`
- `CAD_REFERRAL_GGL_ACCOUNT`
- `CAD_REFERRAL_GGL_CODE`

Rules:

- `*_ACCOUNT` values are encrypted in DB
- `*_CODE` values are plain
- FXPeer decrypts only the account values before posting

- [ ] Dev rows present
- [ ] Pilot rows present
- [ ] Prod rows present
- [ ] Encryption confirmed for `*_ACCOUNT`
- [ ] `NGN` and `CAD` codes correct
- [ ] GL funding accounts are approved by finance/operations

## 2. Service Property / Secret Review

### Profiling

- BreezePay merchant id/code/auth/subscription values
- request authorizer values
- profile/environment switches
- any SDK onboarding provider values

### FXPeer

- transaction-service connectivity
- profiling-service connectivity
- encryption/decryption compatibility for app config

### Sessionmanager

- JWT signing/verification
- login response mapping

### Utility

- OTP encryption key
- referral base-link lookup working against app config

### Backoffice

- profiling integration host/auth
- approval/admin settings

- [ ] Build a per-environment config sheet for all secrets and endpoints
- [ ] Mark each value as `plain`, `encrypted`, or `secret manager only`
- [ ] Confirm pilot and prod have matching key material where required

## Feature/Business Data Readiness

- [ ] Decide whether referral pilot is enabled at launch or deferred
- [ ] If enabled, create and activate the `P2P` referral program before QA signoff
- [ ] Confirm reward mode for pilot:
  - `FLAT_AMOUNT` or `PERCENTAGE_OF_TRANSACTION`
- [ ] Confirm reward target for pilot:
  - `REFERRER_ONLY`, `REFEREE_ONLY`, or `BOTH`
- [ ] Confirm qualifying transaction count
- [ ] Confirm min qualifying amount, min reward, and max reward
- [ ] Confirm reward currency mode:
  - `TRADE_CURRENCY` or `FIXED_CURRENCY`
- [ ] Confirm the business actually wants NG product `UNIT_PRICE` changes deferred or disabled if not fully QA’d

## Pilot Smoke-Test Matrix

## 1. Profiling / Onboarding

- [ ] New CAD onboarding succeeds
- [ ] New NG onboarding/add-account path succeeds
- [ ] Legacy CAD user still logs in normally
- [ ] Legacy NG user can add account without breaking existing data
- [ ] `CA_RETAIL` and `NG_RETAIL` market definitions are present after startup
- [ ] `CUSTOMER_MARKET_PROFILE` rows are created/updated correctly

## 2. Login / Session / Dashboard

- [ ] Login response returns `referralCode`
- [ ] Login response returns normalized `referralCodeLink`
- [ ] JWT claims include `referralCode`
- [ ] JWT claims include `referralCodeLink`
- [ ] Legacy customer with no referral values gets backfilled on login

## 3. P2P Trade And Referral

- [ ] Trade works with no referral code
- [ ] New user can apply valid referral code before first trade
- [ ] Invalid referral code returns correct user-facing error
- [ ] Already-applied referral returns correct user-facing error
- [ ] Referrer must have prior completed trade
- [ ] Referee with prior completed trade is rejected
- [ ] First qualified trade moves attribution to qualified state
- [ ] Reward payout posts debit from referral GL and credit to beneficiary
- [ ] Successful payout marks attribution `REWARDED`
- [ ] Failed payout does not roll back the successful trade

## 4. Investment

- [ ] New `RATE` investments accrue correctly
- [ ] Existing NG money-market positions no longer flatline at `5000.00`
- [ ] Top-up logic respects position valuation mode
- [ ] Liquidation logic respects position valuation mode
- [ ] Future history rows continue from corrected live value

## 5. Backoffice

- [ ] `GET /bo/backoffice/investments/products/{productCode}` works
- [ ] Product edit/activate flow works in UI/API
- [ ] Referral-program admin APIs work:
  - create
  - update
  - activate
  - pause
  - end
  - get active by product

## 6. Transactions Dependency

- [ ] `/peer-to-peer/batch-post-with-type` is healthy
- [ ] Batch reward posting handles debit/credit pairs correctly
- [ ] Reward settlement references are traceable in downstream records

## Production Cutover Checklist

## Before deployment

- [ ] Freeze stable branch for release candidate
- [ ] Tag the release candidate commits
- [ ] Build artifacts fresh with no stale caches
- [ ] Confirm DB DDL required by release is already applied
- [ ] Confirm app config rows exist in prod before service rollout
- [ ] Confirm finance-approved referral GL accounts are funded and active

## Deployment order

Recommended order:

1. `finacial-wealth-api-profiling`
2. `finacial-wealth-api-utility`
3. `finacial-wealth-api-sessionmanager`
4. `finacial-wealth-api-fxpeer-exchange`
5. `finacial-wealth-backoffice-service`
6. verify `finacial-wealth-api-transactions` dependency health

Reason:

- profiling owns market bootstrap and referral runtime
- utility/sessionmanager depend on referral data exposure
- FXPeer depends on profiling runtime and transactions health
- backoffice depends on profiling referral admin endpoints

## After deployment

- [ ] Check profiling startup logs for market bootstrap success
- [ ] Check no audit/bootstrap startup exceptions occur
- [ ] Check login returns referral data correctly
- [ ] Check referral program active lookup works
- [ ] Execute at least one controlled P2P referral test
- [ ] Execute at least one controlled investment valuation test
- [ ] Execute one backoffice product edit test

## Rollback Notes

- Do not rollback only one service in the referral stack without checking compatibility across:
  - profiling
  - utility
  - sessionmanager
  - FXPeer
  - backoffice

- If referral runtime must be disabled quickly:
  - pause or end the active referral program in profiling/backoffice
  - do not delete attribution records
  - keep referral GL config intact for reconciliation

- If profiling bootstrap fails in a future environment:
  - inspect `MARKET_DEFINITION` audit columns first
  - confirm the deployed artifact contains the bootstrap audit fix
  - do not leave relaxed null constraints undocumented

## Known Open Items To Resolve Before Final Go-Live Signoff

- [ ] Decide whether referral is pilot-enabled or production-enabled on day one
- [ ] Produce a full env-by-env secret/config matrix outside source control
- [ ] Confirm whether transactions service needs its own explicit release note for referral payout dependency
- [ ] Confirm if historical investment rows need one-time backfill beyond live-position recovery
- [ ] Confirm if any additional products beyond `P2P` should be turned on for referral later, but keep them disabled for initial rollout unless separately QA’d

## Recommended Working Approach From Now To July 1

- treat every remaining change as a checklist item, not an ad hoc fix
- keep pilot findings and prod readiness notes in this file
- update this document after each deployment, DB change, or config decision
- require a pass across all smoke sections before moving from pilot to production
