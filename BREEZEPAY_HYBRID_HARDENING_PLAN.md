# BreezePay Hybrid Hardening Plan For Plural

## Goal

Strengthen Plural's webhook-driven funding model without changing the core architecture.

Plural should continue to:

- use BreezePay for virtual account creation and inbound funding;
- receive webhook notifications from BreezePay;
- credit the customer internally in Smart Core Ledger;
- use Smart Core Ledger as the authoritative wallet balance for FXPeer, investment, airtime, bills, and other outflows.

## What Plural Can Improve Now Without New BreezePay APIs

These changes can be implemented immediately with the current Plural design:

1. Persist webhook receipt audit earlier, before the Smart Core credit attempt finishes.
2. Improve unresolved-deposit visibility for operations.
3. Add a controlled replay path for pending or failed deposits using existing `CreateQuoteResLog` data.
4. Keep terminal `4xx` posting failures out of endless retry loops.
5. Keep transient failures retryable.

## Immediate Internal Improvements

### 1. Early webhook receipt audit

Current risk:

- a webhook may arrive and fail later during posting, but the audit trail is not as strong as it should be at the point of receipt.

Improvement:

- record raw webhook payload and receipt timestamp as soon as the quote is resolved;
- append later posting result notes instead of only writing on final success/failure.

### 2. Recovery candidate review

Current risk:

- operations has no clean lightweight view of unresolved deposit quotes that are still `PENDING` or already `FAILED`.

Improvement:

- add an internal endpoint to list unresolved deposit candidates from `CREATE_QUOTE_RESPONSE_LOG`;
- include quote id, amount, currency, email, status, acceptance flag, and whether webhook payload/audit exists.

### 3. Manual replay of deposit credit

Current risk:

- if a webhook was valid but posting failed transiently, replay today is operationally clumsy.

Improvement:

- add an internal replay endpoint by `quoteId`;
- reconstruct the deposit payload from `CreateQuoteResLog`;
- pass it back through the existing `processPayment(...)` flow so idempotency and validations remain consistent.

## BreezePay APIs Plural Still Needs Next

To complete the hybrid model properly, Plural should ask BreezePay for:

1. Transaction status by provider reference
2. Transaction list by virtual account and date range
3. Stable unique provider transaction/reference id shared between webhook and inquiry responses
4. Clear final status values such as `SUCCESSFUL`, `FAILED`, and `REVERSED`
5. Reversal visibility where applicable

## Why These Next APIs Matter

With those APIs, Plural can add:

- missed-webhook detection
- provider-vs-ledger reconciliation
- safe auto-recovery for provider-confirmed but internally uncredited inflows
- stronger audit and operational support

## What Plural Should Not Change

Plural should not:

- make BreezePay the runtime source of truth for customer wallet balances;
- query BreezePay balance before every FXPeer, investment, airtime, or bills debit;
- tie wallet spendability to one provider's balance model.

## Recommended Hybrid End-State

1. Webhook remains the primary fast path.
2. Smart Core Ledger remains the source of truth for customer balances.
3. BreezePay inquiry becomes the reconciliation and fallback path.
4. Provider-side data is used to detect, confirm, and repair exceptions, not to replace Plural's wallet ledger.
