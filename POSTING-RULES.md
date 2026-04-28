# Posting Rules

## Purpose

This note documents the accounting and implementation rules we follow for customer wallet postings, currency control GLs, and explicit multi-leg settlements across `transactions`, `fxpeer`, and supporting services.

The control objective is:

- each currency has a control GL
- customer wallet movements in that currency must reconcile against the control GL over time
- explicit journal groups must balance to zero at posting time

## Core Rule

### 1. Explicit `batch-post` groups must net to zero

When a service builds a posting group and calls SmartCore `/v2/batch-post`, the service owns the journal composition. That group must balance to zero inside the batch.

For a simple customer wallet movement against a currency control GL:

- customer `CREDIT` should normally be matched by control GL `DEBIT`
- customer `DEBIT` should normally be matched by control GL `CREDIT`

The only time same-direction customer and GL legs are acceptable in a `batch-post` request is when other legs in that same posting group offset them and the full group still nets to zero.

### 2. Single wallet debit/credit APIs are a different posting mode

Some flows do **not** build an explicit journal group in the service layer. Instead, they call:

- `/walletmgt/account/debit-Wallet-phone`
- `/walletmgt/account/credit-Wallet-phone`

through `transactions` and `utilities-service`.

In those flows, the mirror control-side entry is **not visible in these repos**. The service relies on the downstream wallet/core system behind `WALLET_DEBIT_WALLET` and `WALLET_CREDIT_WALLET` to perform the underlying ledger effects.

Implication:

- do **not** assume a single service-layer `debitCustomerWithType(...)` or `creditCustomerWithType(...)` call is a raw journal leg
- do **not** judge those flows by the same rule as explicit `batch-post` code unless the downstream wallet/core posting contract is being inspected

## Current Service Map

### `transactions`

#### Explicit balanced `batch-post` flows

- Local transfer
  - customer sender `DEBIT`
  - sender-side CAD GL `DEBIT`
  - customer receiver `CREDIT`
  - receiver-side CAD GL `CREDIT`
  - acceptable because the **full group** offsets

- Transfaar deposit fulfillment
  - customer `CREDIT`
  - CAD GL `DEBIT`

- Transfaar withdrawal fulfillment
  - customer `DEBIT`
  - CAD GL `CREDIT`

- Group savings credit
  - customer `CREDIT`
  - CAD GL `DEBIT`

- Group savings debit
  - customer `DEBIT`
  - CAD GL `CREDIT`

#### Single wallet debit/credit proxy flows

These go through `utilities-service` and then into the external wallet/core system:

- `debitCustomerWithType(...)`
- `creditCustomerWithType(...)`

These flows are not explicit journals in the `transactions` codebase.

### `fxpeer`

#### Explicit balanced `batch-post` flow

- Airtime settlement (`ProcSochitelServices`)
  - buyer `DEBIT`
  - buyer GL `DEBIT`
  - seller `CREDIT`
  - seller GL `CREDIT`
  - acceptable because the **full group** offsets

#### Sequential single-wallet posting flows

These rely on `transactions -> utilities-service -> wallet/core` single debit/credit APIs:

- FX marketplace order settlement
- FX offer settlement
- Investment subscription and liquidation

These are only fully auditable from an accounting-proof perspective when the downstream wallet/core system contract is included, because the mirror control-side posting is not expressed in the service code itself.

## Rules for New Work

### Use `batch-post` when:

- the service is explicitly composing a multi-leg settlement journal
- atomic balancing across several legs is required
- the posting logic is owned by the service

### Use single wallet debit/credit APIs when:

- the downstream wallet/core system is the posting engine of record
- the flow intentionally relies on core to manage the mirror ledger effects
- the team has confidence in the downstream posting contract

## Things To Avoid

Do not create an explicit `batch-post` group with only:

- customer `DEBIT` + control GL `DEBIT`
- customer `CREDIT` + control GL `CREDIT`

unless other offsetting legs in the same batch make the overall posting group balance to zero.

## Operational Rule

No settlement flow should be marked `PROCESSED`, `SUCCESS`, `RECEIVED`, or equivalent until the underlying posting call succeeds.

If the core posting fails:

- leave the item retryable where appropriate
- return a failure description such as `Transaction processing failed`
- record enough data for manual investigation or reversal

## Audit Caveat

For flows built on top of:

- `WALLET_DEBIT_WALLET`
- `WALLET_CREDIT_WALLET`

this repository set proves the caller chain, but not the final mirror ledger posting inside the external wallet/core system.

Those flows should be documented to auditors as:

- service-layer instruction to the core wallet engine
- mirror control-entry owned by the downstream wallet/core platform

## Open Verification Items

To close the audit story fully for single debit/credit flows, verify the implementation or vendor contract behind:

- `WALLET_DEBIT_WALLET`
- `WALLET_CREDIT_WALLET`

That verification should confirm whether customer wallet debit/credit automatically generates the corresponding control-GL posting internally.
