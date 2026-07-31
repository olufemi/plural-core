# Plural Pilot/Production Deployment Runbook

## Purpose

This runbook is the operational guide for promoting the Plural solution from dev into pilot and production.

It complements `GO_LIVE_READINESS_CHECKLIST.md`:

- `GO_LIVE_READINESS_CHECKLIST.md` is the master tracker and go/no-go checklist.
- This file is the deployment-day sequence, including service order, config decisions, smoke tests, rollback points, and post-deploy monitoring.

## Deployment Principle

Plural is a coordinated platform, not a single service. Treat every pilot/prod release as a controlled release group across:

- gateway
- session/auth
- profiling
- fxpeer
- transactions
- utility/notifications
- backoffice
- identity-face-service
- SmartCore services
- Redis/RabbitMQ/MySQL/S3/email/push providers

Do not promote a service in isolation when its change depends on another service, schema migration, provider config, queue, or FE contract.

## Environment Policy

| Environment | Policy |
| --- | --- |
| Dev | Can use fallback defaults while features are still being tested. Redemption may fall back to FxPeer AUTO behavior if backoffice `app_config` rows are not yet configured. |
| Pilot | Must have release-critical `app_config` rows present, reviewed, and explicitly accepted by operations/business owner. |
| Production | Must have release-critical `app_config` rows present, maker-checker governed, backed up, and included in go/no-go signoff. |

## Pre-Deployment Snapshot

Before deploying pilot/prod, capture:

- git branch and commit SHA for every deployed service
- artifact name and build timestamp for every service
- database backup/snapshot timestamp
- `app_config` export
- `bo_app_config_registry` export
- `bo_approval_policy` export
- service env var export with secrets masked
- RabbitMQ queue/exchange list
- current running service versions
- rollback artifact locations

Suggested SQL inventory:

```sql
SELECT config_name, config_description, config_value
FROM app_config
ORDER BY config_name;

SELECT config_name, owner_service, value_type, editable, is_sensitive, validation_regex
FROM bo_app_config_registry
ORDER BY config_name;

SELECT action_code, module, sub_module, approval_required, checker_permission, active
FROM bo_approval_policy
ORDER BY action_code;
```

## Deployment Order

1. Confirm infrastructure health:
   - DNS/Route 53
   - ALB target groups
   - TLS/ACM certificate
   - WAF rules
   - RDS/MySQL
   - Redis/ElastiCache
   - RabbitMQ/Amazon MQ
   - S3
   - CloudWatch logging

2. Deploy shared/runtime dependencies first:
   - discovery/service registry if used
   - gateway config only after upstream routes are ready
   - session/auth
   - utility/notifications
   - transactions

3. Deploy domain services:
   - SmartCore services
   - identity-face-service
   - profiling
   - fxpeer
   - backoffice

4. Run migrations and confirm Flyway:

```sql
SELECT installed_rank, version, description, script, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

All rows must have `success = 1`. Failed migrations are a no-go until repaired intentionally.

5. Deploy gateway last or reload gateway routes after upstream health is confirmed.

## Release-Critical Redemption Controls

Backoffice now governs redemption mode and threshold through `app_config`.

Required keys:

| Key | Dev | Pilot recommendation | Production recommendation |
| --- | --- | --- | --- |
| `investment.redemption.approval-mode` | may fall back to `AUTO` | `THRESHOLD` after business agrees limit | `THRESHOLD` or `MANUAL`, signed off by finance/ops |
| `investment.redemption.auto-approval-threshold` | `0` or dev test value | agreed pilot amount, for example `50000` | finance-approved amount |
| `investment.redemption.scheduler-enabled` | `true` | `true` unless pilot is manually controlled | `true` only after liquidity/reversal controls are signed off |
| `investment.redemption.scheduler-cron` | `0 */5 * * * *` | agreed operations interval | agreed operations interval |

Fallback behavior:

- If the backoffice `app_config` rows are absent in dev, FxPeer falls back to service properties/env and then to legacy defaults.
- The default fallback behavior remains automatic settlement.
- Pilot/prod must not rely on implicit fallback. The rows must exist and be reviewed before go/no-go.

Backoffice maker-checker flow:

1. Maker updates the key through `/bo/backoffice/app-config/{configName}`.
2. Checker approves from `/bo/backoffice/approvals/{approvalId}/approve`.
3. FxPeer reads the approved value at runtime for settlement decisions.

Pilot smoke test:

1. Set mode to `THRESHOLD`.
2. Set threshold to an agreed pilot amount.
3. Create one redemption below threshold and confirm it settles automatically.
4. Create one redemption above threshold and confirm it remains pending for backoffice approval.
5. Approve the pending redemption from backoffice and confirm wallet credit.
6. Cancel a pending redemption and confirm reserved investment amount is released.
7. Confirm customer email/push notifications for request, completion, and cancellation.

## Notification Controls

Utility handles email and push delivery for redemption lifecycle events.

Required utility config:

| Config | Purpose |
| --- | --- |
| `APP_EMAIL_PROVIDER` | `SMTP` or active email provider |
| `SMTP_*` or `SENDGRID_*` | email delivery |
| `FCM_PUSH_ENABLED` | enable/disable FCM push |
| `FCM_PROJECT_ID` | Firebase project id |
| `FCM_SERVICE_ACCOUNT_FILE` | service account JSON path on server |

Pilot/prod checks:

- email provider credentials are in Secrets Manager/SSM or encrypted runtime config
- push is enabled only when Firebase credentials are present and approved
- push token updates are verified on login/device change
- RabbitMQ notification queue exists and utility consumes it
- notification failures do not block redemption settlement

## Minimum Smoke Test Pack

### Auth/session

- backoffice login
- `/bo/auth/me`
- refresh token rotation
- session revoke
- mobile login
- device change OTP

### Profiling/onboarding

- CAD onboarding normal path
- NGN add-account OTP path
- NGN add-account face/liveness path
- BVN image unavailable fallback to OTP
- internal CAD bypass blocked outside dev/local

### Investment/redemption

- product list/detail
- subscription
- top-up
- redemption below threshold
- redemption above threshold
- backoffice approve redemption
- backoffice cancel redemption
- customer investment position shows `availableInvestmentAmount`
- redemption history shows pending/completed states

### Money movement/reversal

- local transfer success
- failed fulfilment creates traceable reversal exception
- reversal cannot run unless failed fulfilment is verified by unique end-to-end reference
- maker-checker reversal approval
- no duplicate reversal on retry

### Notifications

- OTP/SMS send
- email send
- push send
- backoffice notification inbox
- provider failure logged without breaking core transaction

### SmartCore

- customer/account lookup
- CAD account alignment
- ledger posting
- batch posting idempotency
- transaction correlation id traceability

## Rollback Rules

Rollback must be prepared before every pilot/prod release:

- rollback artifact for every deployed service
- DB snapshot before migrations/config changes
- `app_config` backup before maker-checker config changes
- RabbitMQ queue definitions captured
- gateway route rollback ready

Rollback triggers:

- login/auth outage
- wallet/ledger mismatch
- SmartCore posting inconsistency
- failed migration
- RabbitMQ queue declaration crash
- redemption reserves not released on cancellation
- automatic settlement runs against the wrong threshold/mode
- customer balance or investment available balance is materially wrong

## Post-Deployment Monitoring

For the first pilot/prod window, monitor:

- ALB 4xx/5xx
- service startup and health endpoints
- Flyway schema history
- RabbitMQ notification/reversal queues and DLQs
- utility email/push send logs
- redemption pending/completed/cancelled counts
- failed fulfilment/reversal exceptions
- SmartCore posting failures
- RDS CPU/connections/locks
- Redis health
- customer-facing login and transaction complaints

## Go/No-Go Signoff

Pilot can proceed only when:

- all release group services are deployed from recorded commits
- all service health checks pass
- core mobile and backoffice smoke tests pass
- redemption mode/threshold behavior is verified
- notification events are confirmed
- internal/test endpoints are blocked publicly
- rollback path is documented and artifacts are available

Production can proceed only when:

- pilot smoke tests pass with evidence
- app_config drift is reviewed
- RDS backup/restore is tested
- WAF/TLS/Route 53 are signed off
- secrets are removed from plain source/runtime files
- maker-checker is enabled for high-risk config and money-moving operations
- finance/ops accept redemption threshold and liquidity exposure
