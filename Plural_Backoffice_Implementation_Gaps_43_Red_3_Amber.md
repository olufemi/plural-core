# Plural Backoffice Admin Portal — Implementation Gap List

This document itemizes the requirements identified as:

- 🔴 **Not Implemented** — 43 items
- 🟠 **Not Fully Implemented** — 3 items

# 🔴 Not Implemented — 43 Items

## Cross-Cutting Platform Requirements

1. **AUTH-01 — Multi-Factor Authentication**  
   Multi-factor authentication (MFA) shall be mandatory for all backoffice accounts.

## Module A — Group Savings Admin

2. **GS-01 — Group Search, Filter & View**  
   Search, filter, and view all groups by status, currency, contribution size, and creator.

3. **GS-02 — Group Configuration View**  
   View a group's complete configuration, including contribution amount, frequency, currency, payout model, member list, and schedule.

4. **GS-03 — Platform-Level Group Constraints**  
   Configure platform-level templates and limits such as maximum group size, maximum contribution amount by KYC tier, and allowed frequencies.

5. **GS-04 — Group Pause / Force-Close / Flag**  
   Pause, force-close, or flag a group for review with a mandatory reason and maker-checker approval.

6. **GS-05 — Group State Change Timeline**  
   Log every state change to a group with actor and reason, visible in the group's timeline.

7. **GS-9 — Contribution Schedule**  
   Provide a live view of each group's contribution schedule with per-member paid/unpaid status.

8. **GS-10 — Scheduled Payout & Override**  
   Support automatic payout to the scheduled recipient, with any admin override requiring a documented reason and maker-checker approval.

9. **GS-11 — Payout Hold**  
   Allow admins to place a payout on hold for compliance or dispute review without disrupting the rest of the group schedule.

10. **GS-14 — Member Risk Score**  
    Track missed/late contributions and roll them into a cross-group member risk score.

11. **GS-15 — Late-Payment Fee Rules**  
    Configure flat, percentage, or tiered late-payment penalty/fee rules.

12. **GS-16 — Repeated Default Restrictions**  
    Flag repeated defaulters and support platform-wide restrictions such as blocking them from joining new groups.

13. **GS-17 — Reserve / Insurance Fund Dashboard**  
    Provide a dashboard of reserve/insurance fund balance, utilization, and payout history.

14. **GS-18 — Dispute Intake Queue**  
    Provide a dispute intake queue for member-reported issues with case assignment to an Operations agent.

15. **GS-19 — Dispute Evidence & Resolution Actions**  
    Support evidence attachments and resolution actions such as refund, reverse payout, ban member, and close case.

16. **GS-20 — Dispute SLA Management**  
    Configure dispute SLA targets with breach escalation to the Operations Manager.

## Module B — Investment Admin

17. **INV-05 — Product Capacity Limit**  
    Configure a maximum total raise and automatically close the product to new subscriptions once capacity is reached.

18. **INV-08 — Liquidation Frequency Limit**  
    Configure the maximum number of liquidations permitted per customer per month, quarter, or year.

19. **INV-13 — Investment Account Search & Filters**  
    Search and filter customer investment accounts by name, phone, email, wallet ID, date range, and KYC status.

20. **INV-16 — Freeze / Unfreeze Investment Activity**  
    Freeze or unfreeze a customer's investment activity with a mandatory reason and maker-checker approval.

21. **INV-18 — Full Investment & Liquidation History**  
    Include full investment, top-up, and liquidation request history with statuses in the customer's investment profile.

22. **INV-20 — Rate / Unit Price History**  
    Retain and display the full rate/unit-price history per product, including after product closure.

23. **INV-21 — Investment Request Management**  
    View all subscriptions and top-ups per product and user with status and accurate timestamps.

24. **INV-24 — After-Cutoff Batch Rollover**  
    Move after-cutoff requests into the next business day's before-cutoff placement batch.

25. **INV-25 — Friday / Non-Business Day Rollover**  
    Roll requests received after cutoff on the last business day before a non-business day into the next business day.

26. **INV-26 — Before / After Cutoff Totals**  
    Display request count and value by currency before and after cutoff for each day and product.

27. **INV-27 — Individual & Bulk Status Updates**  
    Update investment request statuses individually or through bulk/multi-select actions.

28. **INV-29 — Auto Top-Up Visibility**  
    Provide visibility into scheduled auto top-ups, failed auto-debit attempts, and retry counts.

29. **INV-31 — Wallet Hold & Settlement Flow**  
    Place an immediate hold on wallet funds for investment orders and debit or release the funds based on settlement outcome.

30. **INV-33 — Immutable Investment Transactions**  
    Prevent edits to completed transactions and handle corrections through linked reversing entries.

31. **INV-34 — Failed / Reversed Order Notifications**  
    Notify customers when investment or top-up orders fail and are reversed.

32. **INV-37 — Total Investment Balance**  
    Display a customer's total investment balance aggregated across all products and currencies, with per-holding breakdown.

33. **INV-40 — Accrual Run Management**  
    Provide a view of computed interest accrual runs showing run date, product, holdings affected, and total amount accrued.

34. **INV-44 — Investment Statement Generation**  
    Generate and download a customer's investment statement for a specified date range.

35. **INV-45 — Maturity Calendar**  
    Show upcoming maturities by product, date, and amount for treasury planning.

36. **INV-46 — Auto-Rollover / Redeem-by-Default**  
    Configure investment products as auto-rollover by default or redeem-by-default.

37. **INV-47 — Dedicated Liquidation Workflow**  
    Route customer exits before maturity through the dedicated liquidation workflow.

38. **LIQ-11 — Retry Failed Liquidation**  
    Allow admins to retrigger failed liquidation transactions without requiring customer resubmission.

39. **LIQ-13 — Minimum Remaining Balance Validation**  
    Reject a partial liquidation if it would leave the holding below the configured minimum remaining balance.

40. **LIQ-14 — Liquidation Frequency Enforcement**  
    Enforce the configured maximum number of liquidations per customer per month, quarter, or year.

41. **Exposure Caps Requirement**  
    Support configurable exposure caps for maximum investment per user per product and platform-wide exposure per issuer/counterparty.

## Finance & Treasury Module

42. **FIN-02 — Fee Schedule Configuration**  
    Configure fee schedules per service, including FX spread, VAS margin, Group Savings fee, and Investment management fee, by country, currency, and tier.

43. **FIN-03 — Consolidated Revenue Dashboard**  
    Provide a consolidated revenue dashboard broken down by module, country, and currency.

# 🟠 Not Fully Implemented — 3 Items

1. **DASH-01 — Global Landing Dashboard**  
   Provide a role-sensitive dashboard summarizing key KPIs including active users, pending approvals, open disputes, and transaction volume.

2. **DASH-02 — Global Search**  
   Provide global search capable of resolving a user, wallet ID, group ID, trade ID, order ID, or transaction reference from any screen.

3. **INV-02 — Complete Product Identity**  
   Complete the investment product configuration with name, description, issuer/fund manager, currency, risk rating, minimum/maximum investment amount, term/tenor, and minimum holding period/days locked.

# Summary

| Status | Count |
|---|---:|
| 🔴 Not Implemented | 43 |
| 🟠 Not Fully Implemented | 3 |
| **Total Flagged Requirements** | **46** |
