# Plural vs HomeBuddy Virtual Pay / BreezePay Architecture Analysis

## Executive Summary

HomeBuddy and Plural use BreezePay / Virtual Pay for very different purposes.

- HomeBuddy is **provider-led**. Virtual Pay remains the source of truth for balances and transfer status. HomeBuddy queries provider APIs for balances, transaction status, and transaction lists, then updates local records from those provider responses.
- Plural is **internal-ledger-led**. BreezePay / Virtual Pay is mainly used to provision virtual accounts and receive inbound funding. Once an inbound payment webhook is processed, Plural credits the customer inside Smart Core Ledger, and that internal ledger balance becomes the source of truth used by FXPeer, investments, airtime, bills, and other wallet outflows.

The HomeBuddy model should **not** be copied wholesale into Plural. It would weaken Plural's independence and make wallet-based products depend on one provider's balance semantics. The best architecture for Plural is a **hybrid**:

- keep Smart Core Ledger as the authoritative customer wallet balance;
- keep webhook-driven crediting into the internal ledger;
- add provider-side balance inquiry and transaction inquiry as **reconciliation and fallback controls**, not as the primary customer balance authority.

## 1. Current HomeBuddy Virtual Pay Flow

### 1.1 Virtual account creation

HomeBuddy creates customer accounts through the `VirtualpayClient` abstraction, implemented by `BreezePayVirtualpayClient`.

- Interface: [VirtualpayClient.java](/Users/olufemioshin/Documents/Elara/New_Solutions/Home_Buddy/application/src/main/java/com/elara/homebuddy/savings/virtualpay/VirtualpayClient.java)
- BreezePay implementation: [BreezePayVirtualpayClient.java](/Users/olufemioshin/Documents/Elara/New_Solutions/Home_Buddy/application/src/main/java/com/elara/homebuddy/savings/virtualpay/breezepay/BreezePayVirtualpayClient.java:45)
- Onboarding usage: [CustomerOnboardingService.java](/Users/olufemioshin/Documents/Elara/New_Solutions/Home_Buddy/application/src/main/java/com/elara/homebuddy/savings/customer/CustomerOnboardingService.java:41)

HomeBuddy creates:

1. a spending virtual account
2. a savings virtual account

The architecture note explicitly says only the spending account should be shown as the funding account, while the savings account is for controlled internal product movement: [architecture.md](/Users/olufemioshin/Documents/Elara/New_Solutions/Home_Buddy/application/docs/architecture.md:66).

### 1.2 Balance retrieval

HomeBuddy does not derive balances from local movement tables. It fetches balances from Virtual Pay directly.

- Balance service: [CustomerAccountService.java](/Users/olufemioshin/Documents/Elara/New_Solutions/Home_Buddy/application/src/main/java/com/elara/homebuddy/savings/customer/CustomerAccountService.java:22)
- Provider balance call: [BreezePayVirtualpayClient.java](/Users/olufemioshin/Documents/Elara/New_Solutions/Home_Buddy/application/src/main/java/com/elara/homebuddy/savings/virtualpay/breezepay/BreezePayVirtualpayClient.java:81)
- Bulk provider balance call: [BreezePayVirtualpayClient.java](/Users/olufemioshin/Documents/Elara/New_Solutions/Home_Buddy/application/src/main/java/com/elara/homebuddy/savings/virtualpay/breezepay/BreezePayVirtualpayClient.java:89)

`CustomerAccountService.getBalances(...)` loads stored account references and calls `virtualpayClient.getBalances(...)`. The response is stamped with `source = "VIRTUALPAY"`: [CustomerAccountService.java](/Users/olufemioshin/Documents/Elara/New_Solutions/Home_Buddy/application/src/main/java/com/elara/homebuddy/savings/customer/CustomerAccountService.java:31).

### 1.3 Transfer status and reconciliation

HomeBuddy stores local transfer rows, but provider status remains authoritative.

- Reconciliation service: [ReconciliationService.java](/Users/olufemioshin/Documents/Elara/New_Solutions/Home_Buddy/application/src/main/java/com/elara/homebuddy/savings/reconciliation/ReconciliationService.java:21)
- Scheduled polling job: [VirtualpayReconciliationJob.java](/Users/olufemioshin/Documents/Elara/New_Solutions/Home_Buddy/application/src/main/java/com/elara/homebuddy/savings/reconciliation/VirtualpayReconciliationJob.java:25)
- Provider transfer status call: [BreezePayVirtualpayClient.java](/Users/olufemioshin/Documents/Elara/New_Solutions/Home_Buddy/application/src/main/java/com/elara/homebuddy/savings/virtualpay/breezepay/BreezePayVirtualpayClient.java:129)
- Provider transaction list call: [BreezePayVirtualpayClient.java](/Users/olufemioshin/Documents/Elara/New_Solutions/Home_Buddy/application/src/main/java/com/elara/homebuddy/savings/virtualpay/breezepay/BreezePayVirtualpayClient.java:150)

The reconciliation model is documented clearly:

- Virtual Pay is not expected to call HomeBuddy webhooks: [architecture.md](/Users/olufemioshin/Documents/Elara/New_Solutions/Home_Buddy/application/docs/architecture.md:59)
- HomeBuddy must call Virtual Pay for balances and treat Virtual Pay as the source of truth: [va-pull-reconciliation.md](/Users/olufemioshin/Documents/Elara/New_Solutions/Home_Buddy/application/docs/va-pull-reconciliation.md:23)
- Pending transfers are polled until final provider status is returned: [va-pull-reconciliation.md](/Users/olufemioshin/Documents/Elara/New_Solutions/Home_Buddy/application/docs/va-pull-reconciliation.md:35)

### 1.4 HomeBuddy architectural conclusion

HomeBuddy is built around this rule:

- provider balance is authoritative
- provider transfer status is authoritative
- local rows are product records and reconciliation snapshots

This is explicitly stated in the architecture note:

- Virtualpay is the only source of account balance: [architecture.md](/Users/olufemioshin/Documents/Elara/New_Solutions/Home_Buddy/application/docs/architecture.md:99)
- HomeBuddy must not calculate customer balance from local records: [va-pull-reconciliation.md](/Users/olufemioshin/Documents/Elara/New_Solutions/Home_Buddy/application/docs/va-pull-reconciliation.md:31)

## 2. Current Plural BreezePay / Virtual Account Flow

### 2.1 Virtual account creation in profiling

Plural uses BreezePay during onboarding/account creation to generate a customer virtual account.

- Account creation flow: [AddAccountService.java](/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-profiling/src/main/java/com/finacial/wealth/api/profiling/services/AddAccountService.java:123)
- BreezePay proxy: [BreezePayVirtAcctProxy.java](/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-profiling/src/main/java/com/finacial/wealth/api/profiling/proxies/BreezePayVirtAcctProxy.java:27)

The profiling service builds `GenerateVirtualAccountNumberReq`, calls BreezePay, then stores the returned virtual account metadata locally: [AddAccountService.java](/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-profiling/src/main/java/com/finacial/wealth/api/profiling/services/AddAccountService.java:132).

### 2.2 Webhook reception in transactions

Plural accepts deposit webhook notifications and validates request authenticity before processing the deposit.

- Webhook entrypoint: [PaymentWebhookController.java](/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-transactions/src/main/java/com/financial/wealth/api/transactions/controllers/PaymentWebhookController.java:41)
- The controller verifies headers/signature, then passes the raw payload to `WebhookKeyService.processPayment(...)`: [PaymentWebhookController.java](/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-transactions/src/main/java/com/financial/wealth/api/transactions/controllers/PaymentWebhookController.java:84)

Plural also has a BreezePay-specific payin handler path:

- [BreezePayWebhookKeyService.java](/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-transactions/src/main/java/com/financial/wealth/api/transactions/breezepay/payin/BreezePayWebhookKeyService.java:66)

That service directly credits the customer internally after mapping the virtual account to the registered Plural wallet/account.

### 2.3 Internal ledger credit after webhook

Plural does not keep BreezePay as the balance authority after money lands. Instead, transactions-service posts the wallet credit into the internal ledger.

There are two important patterns:

1. **Naira payin path**
   - [BreezePayWebhookKeyService.java](/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-transactions/src/main/java/com/financial/wealth/api/transactions/breezepay/payin/BreezePayWebhookKeyService.java:103)
   - `utilMeth.creditCustomerWithType(rqC, "CUSTOMER")`

2. **Webhook/reconciliation deposit path**
   - pending records are fetched by `findAcceptedPendingDepositWithResponsePending()`: [WebhookKeyService.java](/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-transactions/src/main/java/com/financial/wealth/api/transactions/tranfaar/services/WebhookKeyService.java:165)
   - transactions-service builds a balanced posting:
     - customer wallet `CREDIT`
     - CAD GL `DEBIT`
   - then posts both legs into Smart Core with `utilMeth.batchPost(...)`: [WebhookKeyService.java](/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-transactions/src/main/java/com/financial/wealth/api/transactions/tranfaar/services/WebhookKeyService.java:516)

The underlying Smart Core posting helper is:

- [UttilityMethods.java](/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-transactions/src/main/java/com/financial/wealth/api/transactions/utils/UttilityMethods.java:549)

That helper resolves the product code, constructs generalized ledger batch items, and posts to:

- `/generalledger/v2/batch-post`

### 2.4 Smart Core Ledger becomes the source of truth

Once the webhook-triggered credit succeeds, customer balance is no longer read from BreezePay for product execution. Product services spend from the internal Smart Core wallet balance.

Examples:

- FXPeer checks balance through transactions-service:
  - [OrderService.java](/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-fxpeer-exchange/src/main/java/com/finacial/wealth/api/fxpeer/exchange/order/OrderService.java:629)
- FXPeer seller settlement credits customer via transactions-service:
  - [OrderService.java](/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-fxpeer-exchange/src/main/java/com/finacial/wealth/api/fxpeer/exchange/order/OrderService.java:749)
- Investment subscription checks wallet balance through transactions-service:
  - [InvestmentOrderService.java](/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-fxpeer-exchange/src/main/java/com/finacial/wealth/api/fxpeer/exchange/investment/service/InvestmentOrderService.java:403)
- Transactions-service wallet validation sits behind:
  - [ManageWalletService.java](/Users/olufemioshin/Documents/DanFintech/FinacialWealth/repository/plural/plural-core/finacial-wealth-api-transactions/src/main/java/com/financial/wealth/api/transactions/services/fx/p2/p2/wallet/ManageWalletService.java:103)

### 2.5 Plural architectural conclusion

Plural uses BreezePay / Virtual Pay as:

- virtual account provider
- payin edge
- payment event initiator

But Plural uses Smart Core Ledger as:

- customer wallet source of truth
- settlement source of truth
- balance used by FXPeer, investment, airtime, bills, and other outflows

## 3. Provider-led Balance vs Internal-ledger Balance

### 3.1 HomeBuddy provider-led model

In HomeBuddy:

- the provider/bank owns the account balance
- the provider/bank confirms transfers
- local records are downstream mirrors and product records

Advantages:

- missed webhook risk is lower because balance and transfer status are always pulled from the provider
- simpler conceptual model when the same provider also powers savings, payouts, and other money movement

Costs:

- strong dependency on provider completeness and uptime
- harder to stay provider-agnostic
- harder to abstract multiple downstream outflow providers while keeping one balance model

### 3.2 Plural internal-ledger model

In Plural:

- provider collects money
- webhook/reconciliation path triggers internal posting
- Smart Core Ledger becomes the balance authority

Advantages:

- Plural owns customer wallet state
- Plural can route outflows through different providers while keeping one internal balance
- products like FXPeer escrow and investment do not need to ask BreezePay for each spend decision

Costs:

- webhook/reconciliation reliability becomes critical
- provider and ledger must be reconciled
- operational complexity is higher because Plural must detect and fix missed or duplicated credits

## 4. Can HomeBuddy's Model Safely Work for Plural?

Not as a full replacement.

### 4.1 Why it fits HomeBuddy

HomeBuddy is structurally narrower:

- the provider is central to the money movement model
- the product appears designed around provider-managed accounts
- local services reconcile against provider state rather than controlling a large multi-product internal wallet

### 4.2 Why it does not fit Plural as-is

Plural already uses internal wallet balances across multiple product lines:

- FXPeer escrow and settlement
- investment subscriptions and liquidations
- airtime and bills vending
- wallet transfers and other internal debits/credits

If Plural made BreezePay the source of truth for customer wallet balances:

- FXPeer would become indirectly dependent on provider balance and provider-side movement rules
- investment funding and liquidation would become tied to provider balance visibility and provider settlement semantics
- airtime/bills debits would become less flexible if a different outflow processor is preferred
- provider lock-in would increase materially

That would be a strategic regression for Plural.

## 5. Trade-off Assessment

### Reliability if webhook fails

- HomeBuddy pattern is stronger here because provider inquiry is the main balance/status mechanism.
- Plural's current webhook-led model is more exposed if webhook ingestion fails or is delayed.
- Best answer for Plural is not to abandon the internal ledger, but to add provider inquiry as reconciliation/fallback.

### Independence from one provider

- HomeBuddy sacrifices some independence for simpler provider-led balance control.
- Plural currently preserves better independence because balance is owned internally after credit.

### Multiple outflow providers

- HomeBuddy is less optimized for provider switching because balance authority stays with the provider.
- Plural is better positioned for multiple providers because products spend from one internal wallet ledger.

### FXPeer escrow handling

- FXPeer depends on deterministic internal wallet debits, credits, GL movement, and holds.
- A provider-led balance model would complicate escrow because Plural would need provider balance confirmation and potentially provider-side hold semantics.

### Investment balance usage

- Investment subscription and liquidation in Plural already assume an internal balance and internal reserve/release logic.
- Moving that authority back to BreezePay would create product redesign pressure.

### Airtime and bills vending flexibility

- Internal ledger authority lets Plural debit internally, then route vending through whichever downstream processor is suitable.
- Provider-led balance would make that less flexible and more provider-dependent.

### Reconciliation complexity

- HomeBuddy has simpler balance ownership, but still requires pull reconciliation jobs.
- Plural's internal-ledger model requires more serious reconciliation between provider inflow records and ledger credits.

### Customer balance ownership

- HomeBuddy: provider owns the spendable balance truth.
- Plural: Smart Core Ledger owns the spendable balance truth.

For Plural's product direction, internal ownership is strategically stronger.

### Operational risk

- HomeBuddy risk concentrates around provider API quality and polling completeness.
- Plural risk concentrates around webhook delivery, idempotency, and reconciliation.

## 6. Recommended Hybrid Model for Plural

Plural should keep the current internal-ledger design as the baseline, then add provider-led controls around it.

### 6.1 Keep

- BreezePay / Virtual Pay for virtual account provisioning
- webhook-driven inbound notification
- internal Smart Core posting after webhook
- Smart Core Ledger as the customer wallet source of truth

### 6.2 Add

1. provider balance inquiry for operational reconciliation
2. provider transaction-list inquiry for missed deposit detection
3. scheduled reconciliation for:
   - inbound payment seen at provider but not yet credited internally
   - internally credited transaction with missing provider confirmation
4. mismatch alerting and manual/automatic repair workflow
5. idempotent replay path for missed webhook credits

### 6.3 Do not do

- do not make BreezePay the primary live wallet balance source for Plural products
- do not force FXPeer, investment, airtime, or bills to depend directly on provider balance APIs for normal runtime spending decisions

## 7. Implementation Notes for Plural

### 7.1 Minimal architecture-safe improvement

The safest enhancement is:

- keep `PaymentWebhookController -> WebhookKeyService/BreezePayWebhookKeyService -> Smart Core posting`
- add provider inquiry jobs that compare:
  - provider transaction history
  - webhook logs / quote logs
  - Smart Core credit outcome

This would address the main weakness in the current design: missed or delayed webhook-driven credits.

### 7.2 Reconciliation target

Plural should reconcile at least these entities:

- provider payment reference / quote id
- internal credit transaction id
- credited wallet/account
- amount
- currency
- provider status
- internal ledger status

### 7.3 Operational stance

Treat provider inquiry as:

- fallback detection
- audit and reconciliation support
- repair trigger

Not as:

- the balance API for every product debit
- the runtime source of truth for wallet spendability

## 8. Final Recommendation

Plural should **not** copy HomeBuddy's provider-led balance model.

The better path is:

1. preserve Smart Core Ledger as the authoritative customer wallet balance
2. keep webhook-led internal crediting
3. add provider inquiry and pull reconciliation around BreezePay / Virtual Pay
4. use provider-side data to detect missed webhooks, delayed credits, and reconciliation mismatches

That gives Plural the best of both models:

- the product independence and multi-provider flexibility of an internal ledger
- the resilience and audit strength of provider-side balance/transaction inquiry

In short:

- **HomeBuddy model**: appropriate for a provider-centered account product
- **Plural model**: appropriate for a multi-product wallet platform
- **Best Plural improvement**: hybrid reconciliation, not provider-led balance ownership
