# Investment Valuation Mode Guard

## Purpose

This note explains the fix introduced to stop existing investment positions from changing valuation behavior after an admin updates the product's `valuationMethod`.

It also explains why some Nigeria money market positions could flatline at `5000.00` even when their capital had already grown far beyond that amount.

## Problem Summary

The broken pattern looked like this:

- an investment started as a `RATE` product
- the position accumulated value through `investedAmount + accruedInterest`
- later, the product configuration was edited and the product was marked `UNIT_PRICE`
- the daily valuation scheduler began valuing the old position as `units * unitPrice`

For the affected Nigeria Prime Money Market example:

- `investedAmount` had grown to about `605002.47`
- `units` remained `5000`
- `unitPrice` resolved to `1`
- the scheduler therefore produced `5000 * 1 = 5000.00`

That caused:

- flat market value in history
- false negative gain/loss
- misleading chart output in mobile

## Root Cause

The original design used the product's live valuation method every time the system revalued a position.

Before this fix:

- `InvestmentPosition` stored `product`, `units`, `investedAmount`, `currentValue`, and accrued-interest fields
- it did not store the valuation method used at subscription time
- `InvestmentValuationScheduler` called `resolveValuationMethod(pos.getProduct())`
- top-up and liquidation logic also branched on the product's current valuation mode

That meant an admin change to a product could silently reinterpret already-existing positions.

## Design Fix

The fix makes valuation mode sticky at the position level.

### 1. Persist valuation mode on the position

`InvestmentPosition` now has:

- `valuationMethod`

This field represents the valuation basis that belongs to the position itself, not the mutable product.

File:

- `src/main/java/com/finacial/wealth/api/fxpeer/exchange/investment/domain/InvestmentPosition.java`

## 2. Stamp valuation mode when a position is created

When a subscription is activated and a position is populated, the service now copies the product's valuation mode into the position:

- `position.setValuationMethod(resolveValuationMethod(product))`

This means:

- new `RATE` positions remain `RATE`
- new `UNIT_PRICE` positions remain `UNIT_PRICE`
- later product edits do not change existing positions

File:

- `src/main/java/com/finacial/wealth/api/fxpeer/exchange/investment/service/InvestmentOrderService.java`

## 3. Use position valuation mode for later transaction math

Top-up and liquidation flows now branch on `resolveValuationMethod(position)` instead of `resolveValuationMethod(product)`.

Why this matters:

- top-up logic decides whether to add units or just add capital
- liquidation logic decides whether to redeem units or reduce rate-based capital

Without this change, a position could still drift into the wrong branch even if daily snapshots were fixed.

Files:

- `InvestmentOrderService.topupInvestment(...)`
- `InvestmentOrderService.completeLiquidation(...)`

## 4. Add legacy inference for rows created before this field existed

Older rows will not have `position.valuationMethod` populated immediately.

To keep those rows safe, both the order service and the scheduler now infer the legacy valuation mode when the field is null.

Inference rules:

- if `totalAccruedInterest > 0`, treat as `RATE`
- if `investedAmount` and `currentValue` both closely match `units * unitPrice`, treat as `UNIT_PRICE`
- if `investedAmount` or `currentValue` materially differ from `units * unitPrice`, treat as `RATE`
- otherwise, fall back to the current product setting

This protects legacy positions from being misclassified after rollout.

## 5. Recover flattened legacy RATE positions in the scheduler

Some positions were already damaged before this patch.

For those positions, the scheduler now:

- detects when a position looks like a flattened rate product
- checks whether `currentValue` matches `units * unitPrice`
- checks whether `investedAmount` differs materially from that unit-based value
- walks backward through the position's history
- finds the last non-flattened history row
- restores the recovered accrued amount before continuing valuation

This does not rewrite old history rows in bulk.
It does restore the live position and allows future snapshots to continue from the correct rate-based value.

File:

- `src/main/java/com/finacial/wealth/api/fxpeer/exchange/investment/service/InvestmentValuationScheduler.java`

## Why the fix is safe

The change is intentionally narrow:

- product configuration still controls new subscriptions
- existing positions become stable after creation
- legacy positions get a defensive inference path
- scheduler recovery only activates when the live numbers clearly look flattened

This avoids changing healthy `UNIT_PRICE` positions into `RATE` positions by mistake.

## Operational Note

This change adds a new column to `fx_investment_position`:

- `valuation_method`

If schema auto-update is disabled in the target environment, add the column manually before deployment.

Suggested SQL:

```sql
alter table fx_investment_position
add column valuation_method varchar(32) null;
```

## What this fix does not do

This patch does not bulk-correct previously written bad history rows in the database.

It does:

- stop future misvaluation
- restore affected live positions when the scheduler runs
- let future history snapshots resume with the correct valuation basis

If historical rows need to be backfilled for reporting consistency, that should be handled with a separate one-time repair script.

## Verification Performed

The FXPeer module was compiled after the change with:

```bash
./mvnw -q -DskipTests compile
```

Compile succeeded.

Automated tests were not added or run as part of this change.
