# Multi-Market Cross-Service Impact Matrix

## Purpose

This document gives a platform-wide view of the multi-market optimization.

It answers:

- which services are in scope
- what each service owns today
- where CAD and NGN assumptions currently exist
- what role each service should play in the future model
- when each service should change
- how much compatibility risk each change introduces

This is the decision table to use before implementation begins.

## Guiding Rule

The optimization must be:

- backward compatible for current mobile and frontend consumers
- backward compatible for current third-party provider payloads and flows
- introduced from the inside out

That means:

- internal orchestration can change first
- external contracts should remain stable first
- additive behavior is preferred over replacement behavior

## Service Impact Matrix

| Service | Owns Today | Current CAD/NGN Assumptions | Future Multi-Market Role | Phase To Change | Compatibility Risk |
|---|---|---|---|---|---|
| `finacial-wealth-api-profiling` | onboarding, BVN validation, account creation, wallet-system registration, country validation | CAD behaves like default primary onboarding path, NGN behaves like special second-account path | market orchestration owner | Phase 1 | High |
| `finacial-wealth-api-fxpeer-exchange` | offer, order, lazy account creation triggers, investments, airtime account checks | CAD and NGN branching in runtime account readiness and flow selection | consumer of profiling market orchestration | Phase 2 | High |
| `finacial-wealth-api-transactions` | debit/credit execution, GL steps, balance validation, transfer mechanics | currency-oriented assumptions exist but should not own onboarding | downstream money engine fed by resolved market/account data | Phase 4 or later | High |
| `finacial-wealth-api-session-manager` | session, auth tokens, customer context | likely assumes current customer/account posture but not market-first yet | additive source of market context later | Phase 3 or later | Medium |
| `finacial-wealth-api-utility` | async listeners, notifications, transaction history support, integration helpers | may consume or emit events with current customer/account assumptions | additive audit, notification, and event support | Phase 4 or later | Medium |
| `finacial-wealth-backoffice-service` | admin orchestration and support tooling | current models likely reflect existing customer/account structures | manage market enablement, rollout, support and reporting | Phase 3 or later | Medium |

## Detailed Service Notes

## 1. Profiling Service

### Why it changes first

Profiling is already the closest thing to the onboarding and account-provisioning orchestration layer. That makes it the safest first implementation target.

### What it owns now

- primary onboarding
- SDK-backed onboarding completion
- BVN validation
- secondary account creation
- wallet-system registration
- account metadata persistence
- country validation and some currency-country linkage

### What is fragile today

- Canada and Nigeria are modeled more like special flows than reusable market handlers.
- `add-other-currency-account` is generic by name but Nigeria-shaped in behavior.
- country, currency, and operational enablement are mixed too closely.

### Future role

Profiling should become the owner of:

- `MarketDefinition`
- `CustomerMarketProfile`
- `ensure market readiness`
- country-aware but market-driven onboarding orchestration

### Required compatibility posture

- old APIs remain available
- current provider call sequences remain unchanged
- current customer account data remains valid

### Change recommendation

Change first, but internally.

## 2. FX Peer Service

### Why it changes second

FX Peer already implements the correct product behavior:

- user selects a product or flow
- service checks whether required account exists
- missing account can be provisioned on demand

The problem is not behavior, but the shape of the dependency on profiling.

### What it owns now

- offers
- order purchase
- investment purchase/liquidation
- airtime-related account checks
- lazy account creation trigger

### What is fragile today

- CAD and NGN assumptions are scattered through runtime logic
- account readiness is inferred rather than explicitly resolved through a market contract
- future countries will multiply branching across business flows

### Future role

FX Peer should become a consumer of profiling’s market orchestration:

- resolve required market for flow or product
- call `ensure-ready`
- proceed if active
- return current-compatible behavior if not

### Required compatibility posture

- no mobile redesign
- no public API redesign in phase 1
- no change to provider payloads

### Change recommendation

Change second, after profiling seam exists.

## 3. Transactions Service

### Why it should not change first

Transactions is too close to money movement. It should receive resolved account and wallet details, not become the first place where market orchestration is introduced.

### What it owns now

- balance validation
- wallet debit and credit
- GL steps
- bank transfer and related settlement mechanics

### What is fragile today

- some currency-specific assumptions already exist
- if market behavior leaks further into this service, complexity will rise sharply

### Future role

Transactions should stay the execution engine:

- no onboarding ownership
- no market KYC ownership
- no country-specific onboarding branching

### Required compatibility posture

- stable money movement
- stable provider flows
- stable wallet/account mapping

### Change recommendation

Delay until upstream market orchestration is stable.

## 4. Session Manager

### Why it is in scope

Session data can become a useful source of current market context later, but it should not be the first target of this optimization.

### What it likely owns today

- auth
- token issuance
- customer session state
- user-level runtime context

### Current risk

- if session assumptions are tightly coupled to the current customer/account posture, future market richness may be hard to expose cleanly

### Future role

Potential additive role only:

- active market
- eligible markets
- selected market context

### Required compatibility posture

- no forced token redesign
- no forced mobile re-authentication change
- additive fields only

### Change recommendation

Observe in phase 1, change only if necessary later.

## 5. Utility Service

### Why it is in scope

Utility participates in async persistence, history, notifications, and helper workflows. If new market lifecycle events are introduced, it may need additive support.

### What it likely owns today

- transaction history consumers
- messaging/notification support
- shared async listener behavior

### Current risk

- existing event contracts may assume current account/customer shapes

### Future role

Additive support only:

- market provisioning audit events
- notification support for onboarding state
- reporting signals

### Required compatibility posture

- existing consumers continue to work
- existing event shapes remain consumable

### Change recommendation

No early refactor. Add support only when new market events are clearly defined.

## 6. Backoffice Service

### Why it is in scope

Backoffice will likely become the operational control plane for enabling and managing markets.

### What it likely owns today

- administrative APIs
- support workflows
- monitoring and proxying to internal services

### Current risk

- it may reflect the current country/account assumptions and not a generalized market model yet

### Future role

- enable or disable markets
- manage rollout status
- manage provider configuration by market
- inspect customer market readiness
- support manual intervention workflows if provisioning fails

### Required compatibility posture

- do not break current admin usage
- add market controls gradually

### Change recommendation

After profiling seam is stable, begin additive admin support.

## Integration Boundary Matrix

| Boundary | Current Role | Must Stay Stable? | Future Handling |
|---|---|---|---|
| Mobile / frontend -> Profiling | onboarding and account APIs | Yes | compatibility wrapper |
| Mobile / frontend -> FX Peer | product and trading flows | Yes | unchanged contract first |
| Profiling -> wallet system / Smart Core | identity and account provisioning | Yes | adapter-wrapped, same payloads |
| Profiling -> Nigeria provider flow | BVN + NGN provisioning path | Yes | wrapped in Nigeria handler |
| FX Peer -> Profiling | lazy account provisioning trigger | Yes from client perspective | internal contract evolves |
| FX Peer -> Transactions | balance and posting dependencies | Yes | no early contract change |
| Services -> Utility | events, notifications, history | Yes | additive event support later |
| Backoffice -> platform services | admin and support tooling | Yes | additive market controls later |

## Phase Matrix

| Phase | Objective | Services That Change | Services That Mostly Stay Stable |
|---|---|---|---|
| Phase 0 | analysis and containment | none or docs only | all |
| Phase 1 | internal market model in profiling | profiling | fxpeer, transactions, session-manager, utility, backoffice |
| Phase 2 | FX Peer adoption of profiling seam | profiling, fxpeer | transactions, session-manager, utility, backoffice |
| Phase 3 | admin and optional session support | profiling, fxpeer, backoffice, maybe session-manager | transactions, utility |
| Phase 4 | additive event and reporting support | utility, backoffice, maybe profiling | transactions mostly stable |
| Phase 5 | controlled hardening of money-engine assumptions | transactions only where justified | others stable |

## Where Compatibility Risk Is Highest

### Highest risk

- Profiling
- FX Peer
- Transactions

Reason:

- these three services sit directly on onboarding, account readiness, and money movement

### Medium risk

- Session Manager
- Utility
- Backoffice

Reason:

- they matter operationally, but they do not need to be the first refactor target

## Golden Rule For Implementation

The optimization should move in this order:

1. model the market internally
2. wrap existing Canada and Nigeria flows inside profiling
3. keep current APIs unchanged
4. make FX Peer consume the new internal seam
5. only later extend backoffice, session, utility, and transactions where genuinely needed

This preserves current customer behavior while preparing the platform for more markets.

## What This Means For Go-Live

Before go-live:

- Canada and Nigeria can stay as the only active market handlers
- the new architecture should be introduced as an internal hardening seam
- no frontend/mobile rework should be required
- no provider contract changes should be required

Shortly after go-live:

- India, UK, USD-market, and Euro-market expansion can be introduced as new market handlers instead of cross-service code branching

## Recommended Next Technical Artifact

The next artifact should be the profiling-first design spec:

- exact entities
- exact DTOs
- exact handler classes
- exact compatibility wrapper behavior
- exact sequence of code rollout
- exact regression test plan

