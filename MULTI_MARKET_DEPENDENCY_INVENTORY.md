# Multi-Market Dependency Inventory

## Purpose

This document is the implementation-facing inventory for the multi-market optimization.

It answers:

- where the current CAD and NGN assumptions live
- which services own which parts of onboarding and wallet readiness
- what must be preserved
- where the first safe implementation seam should be cut

This is designed to reduce risk before real refactoring begins.

## Scope

Reviewed solution area:

- `finacial-wealth-api-profiling`
- `finacial-wealth-api-fxpeer-exchange`
- `finacial-wealth-api-transactions`

Reference analysis documents:

- [MULTI_MARKET_ONBOARDING_ARCHITECTURE_ANALYSIS.md](/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/MULTI_MARKET_ONBOARDING_ARCHITECTURE_ANALYSIS.md)
- [MULTI_MARKET_IMPLEMENTATION_BLUEPRINT.md](/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/codex-plural/finacial-wealth-api-fxpeer-exchange/MULTI_MARKET_IMPLEMENTATION_BLUEPRINT.md)

## High-Level Ownership

### Profiling

Current owner of:

- primary customer onboarding
- SDK-backed customer onboarding completion
- BVN validation
- secondary account creation
- wallet-system identity creation
- account metadata and `RegWalletInfo`
- countries and country validation endpoints

### FX Peer

Current owner of:

- offer flows
- order purchase flows
- lazy account provisioning trigger
- investment purchase/liquidation flows
- airtime-related currency account checks

### Transactions

Current owner of:

- money movement execution
- wallet validation and balance checks
- GL-facing transaction execution
- transfer flows and some currency-specific assumptions

## Dependency Inventory By Service

## 1. Profiling Service

### Core files

- [/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-profiling/src/main/java/com/finacial/wealth/api/profiling/controllers/WalletMgtController.java](/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-profiling/src/main/java/com/finacial/wealth/api/profiling/controllers/WalletMgtController.java)
- [/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-profiling/src/main/java/com/finacial/wealth/api/profiling/services/WalletServices.java](/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-profiling/src/main/java/com/finacial/wealth/api/profiling/services/WalletServices.java)
- [/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-profiling/src/main/java/com/finacial/wealth/api/profiling/services/AddAccountService.java](/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-profiling/src/main/java/com/finacial/wealth/api/profiling/services/AddAccountService.java)

### Current functional shape

#### Primary onboarding

`WalletMgtController` and `WalletServices` currently handle the default onboarding path that leads to:

- user registration
- local wallet identity creation
- persistent `RegWalletInfo`
- CAD-oriented default wallet/account behavior

This is the right place to keep orchestration ownership.

#### Nigeria second-account onboarding

`AddAccountService` currently behaves like a Nigeria market adapter even though it is exposed as a generic add-account service.

Observed behavior:

- special-cases Nigeria
- forces `countryCode = NG`
- forces `country = Nigeria`
- depends on prior BVN validation
- provisions local or virtual account path for NGN
- registers user in wallet-system

### Current hard assumptions

1. Primary onboarding and secondary onboarding are not modeled as two market handlers.
2. `/add-other-currency-account` is generic by name but Nigeria-specific by behavior.
3. Customer account readiness is stored in a way that downstream services must infer rather than query explicitly.
4. `RegWalletInfo` is carrying operational meaning that will grow harder to reason about as markets expand.

### Country and ISO-related files

- [/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-profiling/src/main/java/com/finacial/wealth/api/profiling/controllers/CountriesController.java](/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-profiling/src/main/java/com/finacial/wealth/api/profiling/controllers/CountriesController.java)
- [/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-profiling/src/main/java/com/finacial/wealth/api/profiling/services/CountryService.java](/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-profiling/src/main/java/com/finacial/wealth/api/profiling/services/CountryService.java)
- [/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-profiling/src/main/java/com/finacial/wealth/api/profiling/services/CountryDataLoader.java](/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-profiling/src/main/java/com/finacial/wealth/api/profiling/services/CountryDataLoader.java)

### Country data concerns

Current country handling appears to mix:

- database-backed countries
- ISO/JDK-derived country and currency data
- enablement checks by currency
- operational use of country validation

This creates confusion between:

- country reference data
- currency reference data
- market enablement and onboarding behavior

### What must be preserved in profiling

1. Existing Canada onboarding should still work unchanged from the app’s point of view.
2. Existing Nigeria BVN flow and secondary account creation should still work from the app’s point of view.
3. Current third-party calls and payload structures must remain stable.
4. Existing `RegWalletInfo` and related wallet identity behavior must keep working during migration.

### First safe seam in profiling

Introduce new internal orchestration without breaking existing controllers:

- `MarketDefinition`
- `CustomerMarketProfile`
- `MarketOnboardingHandler`
- `MarketAccountProvisioner`
- `MarketKycHandler`
- `MarketWalletSystemAdapter`

Then make:

- Canada current flow -> `CanadaMarketOnboardingHandler`
- Nigeria current flow -> `NigeriaMarketOnboardingHandler`

The old endpoints remain as compatibility wrappers.

## 2. FX Peer Service

### Core files

- [/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-fxpeer-exchange/src/main/java/com/finacial/wealth/api/fxpeer/exchange/feign/ProfilingProxies.java](/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-fxpeer-exchange/src/main/java/com/finacial/wealth/api/fxpeer/exchange/feign/ProfilingProxies.java)
- [/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-fxpeer-exchange/src/main/java/com/finacial/wealth/api/fxpeer/exchange/order/OrderService.java](/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-fxpeer-exchange/src/main/java/com/finacial/wealth/api/fxpeer/exchange/order/OrderService.java)
- [/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-fxpeer-exchange/src/main/java/com/finacial/wealth/api/fxpeer/exchange/offer/OfferService.java](/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-fxpeer-exchange/src/main/java/com/finacial/wealth/api/fxpeer/exchange/offer/OfferService.java)
- [/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-fxpeer-exchange/src/main/java/com/finacial/wealth/api/fxpeer/exchange/investment/service/InvestmentOrderService.java](/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-fxpeer-exchange/src/main/java/com/finacial/wealth/api/fxpeer/exchange/investment/service/InvestmentOrderService.java)
- [/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-fxpeer-exchange/src/main/java/com/finacial/wealth/api/fxpeer/exchange/inter/airtime/security/ProcSochitelServices.java](/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-fxpeer-exchange/src/main/java/com/finacial/wealth/api/fxpeer/exchange/inter/airtime/security/ProcSochitelServices.java)
- [/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-fxpeer-exchange/src/main/java/com/finacial/wealth/api/fxpeer/exchange/common/CurrencyCode.java](/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-fxpeer-exchange/src/main/java/com/finacial/wealth/api/fxpeer/exchange/common/CurrencyCode.java)

### Current functional shape

FX Peer already implements an important pattern that should be kept:

- user selects a flow or product
- service determines required currency/account
- if account is missing, profiling is called to create or validate the missing account

This is the correct product behavior.

The problem is only that the contract is not market-shaped enough yet.

### Current hard assumptions

1. CAD often behaves like the primary identity/default account path.
2. NGN often behaves like the secondary provisioned market path.
3. Account readiness is inferred from `AddAccountDetails` and country-specific assumptions.
4. Profiling is called through a country/currency-specific endpoint instead of a market-aware readiness orchestration.
5. Currency enum and branching will become increasingly brittle as markets expand.

### Why this matters

If India, UK, USD-market onboarding, or Euro-market onboarding are added the same way:

- `OrderService`
- `OfferService`
- `InvestmentOrderService`
- `ProcSochitelServices`

will each gain more branching and duplicated readiness logic.

### What must be preserved in FX Peer

1. Lazy account creation must continue.
2. User should still be able to select a market-backed product and be provisioned on demand.
3. Existing product flows must not wait for a complete mobile redesign.
4. Investment and FX Peer flows should keep current API contracts unless a coordinated mobile change is planned.

### First safe seam in FX Peer

Do not rewrite business flows first.

Instead, only change the profiling call shape over time:

Current idea:

- `add-other-currency-account`

Target idea:

- `ensure-ready(marketCode, currencyCode, productType, triggerSource)`

This lets FX Peer preserve its product behavior while offloading market-specific decisions back to profiling.

## 3. Transactions Service

### Core files

- [/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-transactions/src/main/java/com/financial/wealth/api/transactions/services/LocalTransferService.java](/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-transactions/src/main/java/com/financial/wealth/api/transactions/services/LocalTransferService.java)
- [/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-transactions/src/main/java/com/financial/wealth/api/transactions/tranfaar/services/WebhookKeyService.java](/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-transactions/src/main/java/com/financial/wealth/api/transactions/tranfaar/services/WebhookKeyService.java)

### Current functional shape

Transactions should remain the money-movement engine, not the onboarding decision engine.

Its role is:

- validate balances
- debit and credit wallets
- manage GL steps
- complete money movement

### Current hard assumptions

There are already currency-oriented or market-adjacent assumptions in transaction handling and related services. That is manageable now for a few markets, but risky at larger scale.

### What must be preserved in transactions

1. Current account-number and wallet mapping must remain stable.
2. GL flows and provider interactions must not be disturbed by onboarding refactor.
3. Market expansion should not force transactions service to own onboarding logic.

### First safe seam in transactions

Do not start this refactor in transactions.

Instead, let transactions continue to receive:

- resolved account number
- resolved wallet identity
- resolved market-aware metadata from profiling or upstream services

Then later hardcoded currency assumptions can be reduced carefully, one path at a time.

## Dependency Matrix

| Concern | Profiling | FX Peer | Transactions |
|---|---|---|---|
| Primary onboarding | High | None | None |
| Secondary account creation | High | Trigger only | None |
| BVN validation | High | None | None |
| SDK onboarding completion | High | None | None |
| Country reference data | High | Low | Low |
| Currency assumptions | Medium | High | Medium |
| Lazy account provisioning | Owner | High consumer | None |
| Money movement | None | Medium consumer | High owner |
| Smart Core / wallet-system | High | Indirect | Low |
| Product-to-market eligibility | Medium | High | None |

## Where CAD And NGN Are Functionally Different Today

### CAD

Current operational meaning:

- effectively the primary/default onboarding market
- embedded in default onboarding expectation
- often assumed to be the already-available wallet identity

### NGN

Current operational meaning:

- effectively a special secondary market
- frequently requires runtime provisioning
- depends on BVN and Nigeria-specific provider/account rules

### Why this cannot scale

If each future market is added by mimicking NGN:

- every market becomes another special-case branch
- every flow that touches accounts must learn another market-specific rule
- operational behavior becomes scattered

## Proposed Refactor Order

### Stage 1: No behavior change

- create market model
- create market handler interfaces
- create compatibility wrappers in profiling

### Stage 2: Wrap existing logic

- wrap Canada onboarding in handler
- wrap Nigeria add-account in handler
- preserve old endpoint contracts

### Stage 3: Add orchestration API

- add internal `ensure-ready`
- return explicit status and next action

### Stage 4: Convert FX Peer call sites

- make `OrderService`, `OfferService`, `InvestmentOrderService`, and airtime flows call the new profiling orchestration
- leave downstream money movement unchanged

### Stage 5: Harden country and reference data

- separate `CountryReference`
- separate `CurrencyReference`
- separate `MarketDefinition`

### Stage 6: Expand markets

- add new handlers and config rather than cross-service branches

## First Implementation Checklist

### Profiling-only first cut

1. Add `MarketDefinition` entity/model.
2. Add `CustomerMarketProfile` entity/model.
3. Add `MarketOnboardingHandler` and related interfaces.
4. Implement `CanadaMarketOnboardingHandler`.
5. Implement `NigeriaMarketOnboardingHandler`.
6. Add internal `ensure-ready` service method.
7. Keep `create-user`, `validate/bvn`, and `add-other-currency-account` intact.

### FX Peer second cut

1. Introduce market resolution helper from product/currency.
2. Replace direct add-account assumptions with profiling orchestration call.
3. Keep user-visible product flow unchanged.

### Transactions third cut

1. Do not change until profiling seam and FX Peer adoption are stable.
2. Later, reduce hardcoded currency assumptions with controlled tests.

## No-Regression Checklist

Before any merge:

1. Canada onboarding still produces the expected default CAD account.
2. Nigeria account creation still works after BVN validation.
3. FX Peer buy flow still provisions a missing supported account.
4. Investment subscription still provisions a missing supported account when allowed.
5. Airtime and other dependent flows still resolve wallet identity correctly.
6. Smart Core and wallet-system payloads remain unchanged.
7. Existing customer accounts continue to work without migration breakage.

## Recommended Next Artifact

After this inventory, the next useful document should be:

- a profiling-first class design and endpoint specification

That document should contain:

- exact DTOs
- exact entities
- status enums
- sequence diagrams for `ensure-ready`
- migration mapping from old endpoints to new handlers

