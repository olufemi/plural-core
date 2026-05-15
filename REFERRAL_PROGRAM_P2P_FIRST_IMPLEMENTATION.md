# Referral Program Runtime: P2P First

## Scope

This implementation introduces a configurable referral-program runtime that is product-agnostic by design, with `P2P` as the first enabled product flow.

It builds on the existing referral-program admin/configuration layer and wires a runtime path across:

- `finacial-wealth-api-profiling`
- `finacial-wealth-api-fxpeer-exchange`

Backoffice remains the administrative surface for program management and does not execute transaction-side reward logic.

## Service Ownership

### Profiling

Profiling owns:

- canonical customer referral codes through `RegWalletInfo.referralCode`
- active referral-program configuration
- referral attribution state
- reward calculation snapshot
- qualification state
- idempotent payout completion state

### FXPeer

FXPeer owns:

- P2P-first-trade eligibility checks based on actual trade history
- referral-code application before trade
- post-trade qualification trigger
- wallet bonus payout execution using transactions-service

## Runtime Flow

### 1. Apply code before trade

FXPeer applies the referral only when a code is supplied on the P2P buy request.

Before calling profiling, FXPeer checks:

- referee has not completed a prior P2P trade
- referrer exists by canonical customer referral code
- referrer is not the same customer
- referrer has completed at least one prior P2P trade

If the checks pass, FXPeer calls:

- `POST /referral-programs/runtime/apply`

Profiling then:

- confirms there is an active program for the product type
- resolves the current customer from the JWT
- prevents duplicate/competing attributions
- snapshots the reward configuration onto a new attribution record

### 2. Qualify after successful trade

After FXPeer successfully posts the trade and saves `WalletIndivTransactionsDetails`, it calls:

- `POST /referral-programs/runtime/qualify`

Profiling then:

- loads the stored attribution for the current customer and product
- enforces qualifying transaction count
- checks minimum qualifying amount
- calculates the reward from the stored program snapshot
- sets the attribution to `QUALIFIED_PENDING_PAYOUT` when money is due
- marks it `REWARDED` immediately when no payout is due

### 3. Payout reward bonus

If qualification returns a payable reward:

- FXPeer resolves the referral funding GL for the reward currency from app config
- supported config keys are:
  - `NGN_REFERRAL_GGL_ACCOUNT`
  - `NGN_REFERRAL_GGL_CODE`
  - `CAD_REFERRAL_GGL_ACCOUNT`
  - `CAD_REFERRAL_GGL_CODE`
- `*_REFERRAL_GGL_ACCOUNT` values are stored encrypted and decrypted in FXPeer before posting
- FXPeer resolves the destination wallet/account for each beneficiary in the reward currency
- for `CAD`, payout goes to the customer phone-backed wallet
- for non-`CAD`, FXPeer looks for the matching currency account and lazily provisions one through profiling if missing
- FXPeer posts the reward with `batchPostWithType`
- each beneficiary reward is now posted as an explicit debit/credit pair:
  - debit the currency-specific referral funding GL
  - credit the beneficiary wallet/account
- if the referral GL config is missing for the reward currency, payout is skipped and the attribution remains uncompleted until the funding config is fixed

### 4. Mark payout complete

After a successful payout batch, FXPeer calls:

- `POST /referral-programs/runtime/{attributionId}/complete`

Profiling marks the reward paid and moves the attribution to `REWARDED` once all due legs are complete.

## New Profiling Runtime Artifacts

### Entity

- `ReferralAttribution`

This stores:

- product/program snapshot
- referrer/referee identifiers
- reward rule snapshot
- qualified transaction facts
- payout references
- lifecycle status

### Enum

- `ReferralAttributionStatus`
  - `APPLIED`
  - `QUALIFIED_PENDING_PAYOUT`
  - `REWARDED`
  - `CANCELLED`

### Repository

- `ReferralAttributionRepository`

### Runtime Models

- `ApplyReferralAttributionRequest`
- `QualifyReferralAttributionRequest`
- `CompleteReferralAttributionRequest`

### Runtime Service + Controller

- `ReferralProgramRuntimeService`
- `ReferralProgramRuntimeController`

## New FXPeer Integration Points

### Profiling proxy additions

- `applyReferralAttribution(...)`
- `qualifyReferralAttribution(...)`
- `completeReferralAttribution(...)`

### New request models

- `ApplyReferralAttributionRequest`
- `QualifyReferralAttributionRequest`
- `CompleteReferralAttributionRequest`

### Order flow changes

`OrderService.createOrderCaller(...)` now:

- validates and applies referral code before a first P2P trade
- qualifies referral after successful trade completion
- executes payout as a post-trade side effect

## Safety Notes

- successful P2P trade execution is not rolled back because of a referral payout failure
- referral payout is best-effort but recoverable because the attribution remains pending until completion is recorded
- reward configuration is snapshotted onto the attribution record so later campaign edits do not retroactively alter already-applied referrals
- referral bonus funding is now explicit and separately reconcilable from normal product settlement GLs

## Java Compatibility

- profiling implementation was kept Java-8-safe
- earlier Java-11+ conveniences in the new referral-program package were normalized
- FXPeer and backoffice remain on Java 21

## Current Limitation

This slice is enabled only for `P2P` in runtime behavior.

The structure is intentionally generic so future products such as:

- `AIRTIME`
- `INVESTMENT`
- other transaction products

can reuse the same runtime model with product-specific qualification triggers.
