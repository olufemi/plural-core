# Multi-Market Compatibility Guardrails

## Purpose

This document defines the non-negotiable compatibility rules for the multi-market optimization.

It exists to ensure the optimization:

- does not break frontend or mobile applications
- does not require immediate API contract changes
- does not disturb existing third-party/provider integrations
- does not scatter incompatible behavior across services

## Optimization Boundary

This optimization must be analyzed as a **platform-wide change**, not only as a profiling or FX Peer change.

That means the design must consider:

- `finacial-wealth-api-profiling`
- `finacial-wealth-api-fxpeer-exchange`
- `finacial-wealth-api-transactions`
- `finacial-wealth-api-session-manager`
- `finacial-wealth-api-utility`
- `finacial-wealth-backoffice-service`

and the current third-party/client integrations used by those services, including but not limited to:

- Smart Core / wallet system
- BreezePay
- quote and pricing providers
- airtime/data providers
- transfer/payment providers
- KYC and identity providers

## Non-Negotiable Rules

## 1. Mobile And Frontend Contracts Must Stay Backward Compatible

The optimization must not require mobile or frontend teams to redesign or re-integrate existing flows as a prerequisite.

This means:

- existing endpoints must continue to behave as they do today
- request payloads must remain accepted as-is
- current response shapes must remain valid
- new orchestration must sit behind compatibility wrappers where needed

### Examples

- `create-user` must still work for existing app onboarding
- `validate/bvn` must still work as-is
- `add-other-currency-account` must continue to work from current mobile integrations
- current FX Peer product flows must continue to function without mobile contract redesign

## 2. Third-Party Payload Shapes Must Not Be Changed

We can reorganize internal orchestration, but we must preserve the current request and response expectations to:

- Smart Core
- wallet system
- BreezePay
- quote services
- payment and transfer providers
- airtime/data providers

The optimization should wrap or route existing calls, not redefine provider contracts.

## 3. Existing Customer Data And Accounts Must Continue To Work

The optimization must not assume a clean-slate customer base.

It must support:

- customers with only CAD
- customers with CAD + NGN
- customers created through the old onboarding path
- customers with historical `RegWalletInfo` and `AddAccountDetails`

Any new model like `CustomerMarketProfile` must coexist with current data during migration.

## 4. Existing Service Contracts Must Be Preserved First, Then Improved Internally

The first implementation phase must add internal abstractions without forcing contract changes across services.

That means:

- profiling can gain new market-aware internals
- old controller endpoints remain operational
- FX Peer can keep current request patterns while internally moving toward market orchestration
- transactions should remain downstream of resolved account/wallet data

## Service-by-Service Compatibility View

## 1. Profiling

Compatibility rule:

- current onboarding APIs remain externally unchanged
- new market model is introduced behind the controller layer

Expected strategy:

- keep `create-user`
- keep `validate/bvn`
- keep `add-other-currency-account`
- route old behaviors into new handler-based orchestration internally

## 2. FX Peer

Compatibility rule:

- mobile and app flows must remain unchanged
- no sudden replacement of product or offer APIs

Expected strategy:

- keep current flows for offer creation, buy order, airtime, and investments
- replace only the internal profiling call logic gradually
- preserve user-visible flow behavior

## 3. Transactions

Compatibility rule:

- money movement APIs and GL execution must remain stable
- onboarding optimization must not leak unstable market logic into transactions first

Expected strategy:

- transactions continues to receive resolved account and wallet details
- market model integration happens indirectly through upstream services

## 4. Session Manager

Compatibility rule:

- authentication, token issuance, and current customer session assumptions must continue to work
- no forced mobile re-authentication model change

Expected strategy:

- session payloads may later include market-aware metadata only if added in a non-breaking way
- do not make session-manager the first refactor target

Potential future role:

- expose current active market or eligible markets in a backward-compatible claim or payload field
- this should be additive, not breaking

## 5. Utility

Compatibility rule:

- event consumers, transaction history, SMS/email, and async workflows must continue to function

Expected strategy:

- if new market lifecycle events are introduced, they should be additive
- existing event formats should remain consumable
- utility should not become the orchestration owner of onboarding

Potential future role:

- audit or notification support for market-account provisioning events
- reporting and monitoring support

## 6. Backoffice

Compatibility rule:

- current administrative usage must continue while new market controls are introduced

Expected strategy:

- introduce market configuration and enablement screens gradually
- avoid breaking current backoffice integrations that expect existing customer/account models

Potential future role:

- manage enabled markets
- manage market rollout states
- manage provider-level configuration by market
- monitor provisioning outcomes and onboarding status

## Backward-Compatible Design Principles

## Principle 1: Compatibility Wrappers First

Old APIs should remain the external contract while new market-aware services sit behind them.

Example:

- old `add-other-currency-account` endpoint remains
- internally it resolves market and delegates to a market handler

## Principle 2: Additive Data Model, Not Destructive Replacement

New models like:

- `MarketDefinition`
- `CustomerMarketProfile`

should be additive first.

They should not immediately replace:

- `RegWalletInfo`
- `AddAccountDetails`
- existing country tables

Instead:

- old data remains authoritative where needed
- new data is introduced alongside it
- migration and reconciliation happen gradually

## Principle 3: Third-Party Integrations Stay Behind Adapters

We must not reshape provider flows just to fit the new internal model.

Instead:

- handler layer calls provider adapters
- provider adapters preserve existing payloads and sequencing

## Principle 4: Frontend Sees The Same Contract

If a market is not ready, existing APIs should still return a response pattern that current clients can handle.

If we later want richer statuses like:

- `PENDING_KYC`
- `PENDING_PROVIDER_ACCOUNT`
- `NEXT_ACTION`

those should be introduced carefully and preferably as additive fields or behind endpoints not yet consumed by legacy clients.

## Principle 5: Refactor Inward-Out

The first change should be:

- internal orchestration

not:

- public API redesign

The platform should change inside first and keep the same shell externally.

## Phased Compatibility Strategy

## Phase 0: Analysis Across All Services

Before coding:

- inventory profiling, FX Peer, transactions, session-manager, utility, and backoffice dependencies
- identify current customer/account/session assumptions
- map provider call boundaries

## Phase 1: Profiling Internal Market Layer

- add market abstractions internally
- preserve old endpoints
- wrap Canada and Nigeria flows

## Phase 2: FX Peer Internal Adoption

- keep same product APIs
- replace direct assumptions with profiling orchestration calls

## Phase 3: Additive Session And Backoffice Support

- add market metadata to session or admin views only where safe
- keep current consumers unaffected

## Phase 4: Utility And Event Support

- add optional market-aware events, notifications, or audit flows if needed
- preserve current listeners and transaction history behavior

## Current Recommendation

The optimization should absolutely consider all services and integration boundaries, but not all services should change at the same time.

The correct sequence is:

1. analyze all affected services and provider boundaries
2. implement the first safe seam in profiling
3. adopt the seam in FX Peer
4. keep transactions, session-manager, utility, and backoffice stable until their role in the new model is additive and low risk

## What This Means Practically

For now:

- yes, all services are in scope for analysis
- no, not all services should be the first implementation target
- all frontend and mobile integrations must remain backward compatible
- all third-party calls must retain their current payload and sequencing behavior

## Next Design Artifact

The next useful artifact should be a **cross-service impact matrix** that lists:

- service
- responsibility today
- current CAD/NGN assumptions
- future market role
- whether it changes in phase 1, 2, or later
- compatibility risk level

