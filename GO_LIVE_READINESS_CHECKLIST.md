# Go-Live Readiness Checklist

## Purpose

This checklist turns the current solution state into a practical pilot and production readiness pack for the July 1 rollout target.

It is meant to answer:

- what must be in the release baseline
- which services must be deployed together
- which DB tables, columns, and seed rows must exist
- which config values must be prepared per environment
- which smoke tests must pass in pilot before production

## Release Baseline

- Stable rollout branch: `feature/after-first-user-experience-test-on-dev`
- Long-running architecture branch: `codex/multi-market-onboarding-architecture-hardening`
- Rule: all go-live packaging, QA, pilot fixes, and production deployment preparation should be driven from the stable branch
- Rule: after every stable-branch fix, merge stable into the long-running architecture branch on the same day

## Solution Areas In Scope

The current release shape spans these areas:

- multi-market onboarding and market orchestration
- FXPeer market-aware adoption for NG and CAD paths
- referral program runtime and admin support
- investment valuation-mode guard and legacy safety
- dashboard/login referral-code exposure
- backoffice product-detail compatibility and referral-program admin APIs
- BVN OTP and BVN face/liveness verification for NGN add-account
- SmartCore-backed account, ledger, posting, and customer integrations
- client-facing mobile, web/backoffice, and API integration readiness
- notification channels: OTP, push notification, email, and ops alerts
- AWS pilot-to-production infrastructure readiness
- `app_config` runtime data/config governance
- backoffice P2P FX operational bridges for market offers, seller offers, escrow operations, ratings, and receipts
- backoffice VAS/Sochitel catalog visibility and airtime reversal retry support

## Services To Treat As A Coordinated Release Group

### Must verify and package together

- `finacial-wealth-api-profiling`
- `finacial-wealth-api-fxpeer-exchange`
- `finacial-wealth-api-sessionmanager`
- `finacial-wealth-api-utility`
- `finacial-wealth-backoffice-service`
- `finacial-wealth-gateway`

### Critical dependency to verify even if not newly changed

- `finacial-wealth-api-transactions`
- `identity-face-service`
- SmartCore services:
  - API Gateway
  - Core Banking
  - Ledger/Posting
  - Account
  - Customer
  - Admin Ops
  - Integration/Worker
- Redis / ElastiCache
- RabbitMQ / Amazon MQ
- MySQL / RDS
- SendGrid or active email provider
- OTP/SMS provider
- Push notification provider
- external KYC/BVN provider
- internal ML face/liveness provider behind `identity-face-service`

Reason:

- referral reward payout depends on the existing transactions batch endpoint
- FXPeer uses `/peer-to-peer/batch-post-with-type`
- a healthy transactions deployment is required even if that service is not reworked heavily in this release
- gateway owns public API exposure and must block internal-only pilot/test routes
- identity-face-service is required for BVN face/liveness verification and must be version-aligned with profiling
- SmartCore owns the ledger/accounting truth for customer, account, ledger, and posting operations
- notification providers are part of user-visible onboarding, login, device-change, transfer, and ops-alert flows
- Redis, RabbitMQ, and MySQL are runtime dependencies, not optional infrastructure
- FXPeer owns P2P FX offers, orders, escrow, ratings, and receipts; backoffice now exposes secured wrappers for the currently available downstream contracts
- FxPeer/Sochitel VAS owns provider categories/products and airtime reversal cases; backoffice now exposes secured wrappers for visibility and retry, while deeper reconciliation/refund workflow remains a production-hardening item

## AWS Infrastructure Readiness

Architecture baseline:

- AWS account for pilot environment, with a clear promotion path to production
- 2-AZ VPC with public, private application, and private data subnets
- Internet Gateway for public entry
- Application Load Balancer for Plural
- Application Load Balancer for SmartCore
- Route 53 hosted zone and DNS records
- ACM TLS certificates attached to public HTTPS entry points
- AWS WAF in front of public ALB/CloudFront entry points
- NAT Gateway for private subnet egress
- SSM Session Manager for controlled server access, no SSH dependency
- EC2 application nodes for Plural services
- EC2 application nodes for SmartCore services
- RDS MySQL for Plural database
- RDS MySQL for SmartCore database
- ElastiCache Redis/Valkey
- Amazon MQ RabbitMQ for production, or EC2 RabbitMQ only for approved pilot cost-saving
- S3 for media, reports, receipts, exports, and backups
- ECR for container images if container deployment is used
- CloudWatch logs, metrics, alarms, and dashboards
- SNS email/Slack alert fan-out
- Secrets Manager / SSM Parameter Store
- KMS encryption keys for database, S3, secrets, and backups
- AWS Backup or equivalent snapshot/restore coverage

Cost baseline from AWS Pricing Calculator PDF:

- source: `Plural_Pilot-To-Prod WAS Estimate (1).pdf`
- export date: June 27, 2026
- region: Europe (London)
- upfront estimate: `0.00 USD`
- monthly estimate: `892.17 USD`
- 12-month estimate: `10,706.04 USD`
- note: AWS Pricing Calculator excludes applicable taxes and final cost depends on actual usage

Estimated monthly service breakdown:

- Amazon EC2 Plural app servers: `109.79 USD`
- Amazon EC2 SmartCore/Core Banking app servers: `54.90 USD`
- Amazon RDS for MySQL: `187.35 USD`
- Elastic Load Balancing: `39.86 USD`
- Amazon ElastiCache Redis: `26.28 USD`
- Amazon MQ RabbitMQ: `24.72 USD`
- Amazon S3: `2.46 USD`
- Amazon CloudWatch: `47.31 USD`
- Amazon VPC / NAT / public IPv4: `346.60 USD`
- AWS WAF: `37.00 USD`
- Amazon Route 53: `3.40 USD`
- AWS Secrets Manager: `12.50 USD`

Cost-governance checks:

- [ ] Attach the AWS estimate PDF to the go-live pack
- [ ] Confirm the calculator estimate matches the final architecture diagram
- [ ] Confirm monthly estimate owner accepts `892.17 USD` as the current pilot-to-prod AWS baseline
- [ ] Confirm whether the RDS estimate is still Single-AZ; price Multi-AZ before production signoff

## Backoffice Security Runtime Controls

The backoffice pilot is expected to become production, so these controls must be reviewed during deployment:

- `BO_MFA_REQUIRED`: set to `true` for production only after the first super-admin MFA setup and recovery runbook are confirmed.
- `BO_IDLE_TIMEOUT_SECONDS`: standard admin idle timeout exposed to FE through `/bo/auth/me`.
- `BO_PRIVILEGED_IDLE_TIMEOUT_SECONDS`: shorter timeout for privileged roles such as `SUPER_ADMIN`.
- `BO_EXPIRY_WARNING_SECONDS`: FE warning window before session expiry.
- `/bo/auth/refresh` rotates refresh tokens; FE must persist the returned refresh token.
- `/bo/auth/sessions` and `/bo/auth/sessions/{sessionId}` support active-session cleanup/revocation.
- Approval policies for product, `app_config`, referral, campaign, and customer block/unblock can be enabled through `BACKOFFICE_APPROVAL_POLICY_ENABLE_PROD.sql` when the checker inbox flow is ready.
- [ ] Confirm NAT Gateway count is intentional because VPC/NAT is the largest line item in this estimate
- [ ] Confirm WAF, Route 53, and Secrets Manager are included in business cost communication
- [ ] Confirm taxes, support plan, data egress, backups, snapshots, and log growth may increase actual monthly spend

Pilot / Production decision points:

- [ ] Confirm whether pilot uses Single-AZ RDS and production uses Multi-AZ RDS
- [ ] Price Single-AZ RDS and Multi-AZ RDS side by side; this is likely the biggest material infra cost difference
- [ ] Confirm pilot NAT Gateway count and production NAT Gateway per-AZ target
- [ ] Confirm pilot RabbitMQ on EC2 vs production Amazon MQ RabbitMQ
- [ ] Confirm WAF is optional only for pilot and mandatory for production
- [ ] Confirm CloudWatch alarm coverage is basic for pilot and full for production
- [ ] Confirm production uses AWS Backup, RDS automated backups, and tested restore runbooks
- [ ] Confirm production has separate security groups for ALB, app nodes, data stores, and admin/ops paths
- [ ] Confirm SSM Session Manager works before disabling direct SSH access
- [ ] Confirm all public endpoints are HTTPS-only with valid ACM certificates

Go / No-Go:

- GO for pilot if every public endpoint resolves through Route 53, terminates TLS correctly, health checks pass, and private services are not directly internet reachable
- NO-GO for production if RDS Multi-AZ, backup restore test, WAF, secrets management, and CloudWatch alarms are not signed off

## Integration Readiness

### SmartCore

Required flows:

- customer creation/update from Plural
- CAD account readiness and RegWalletInfo alignment
- NGN add-account readiness through `add_account_details`
- ledger posting and batch posting
- reversal/failure handling for partially successful external calls
- account status and balance lookup
- transaction reference/correlation-id traceability across Plural and SmartCore

- [ ] Confirm SmartCore base URLs, auth keys, and service discovery names per environment
- [ ] Confirm SmartCore database is migrated and seeded before Plural smoke tests
- [ ] Confirm SmartCore ledger/posting endpoints are idempotent where Plural retries may occur
- [ ] Confirm Plural logs include correlation IDs passed to SmartCore
- [ ] Confirm failed SmartCore posting does not leave customer-facing success with incomplete ledger state
- [ ] Confirm reconciliation report can tie Plural transaction/reference to SmartCore posting entries

### Client Integrations

Clients in scope:

- mobile app
- backoffice FE
- admin users
- API clients/partners if exposed in pilot

Required checks:

- [ ] Confirm all FE routes match gateway routes
- [ ] Confirm CORS allows required headers and blocks unknown origins
- [ ] Confirm auth/session expiration behavior is understood by mobile and backoffice
- [ ] Confirm backoffice create product validates mandatory fields and does not return false 200 success
- [ ] Confirm product list/detail responses include fields required by FE, including `subscriptionCutOffTime`, `createdAt`, and create-product request-body fields
- [ ] Confirm mobile device-change OTP flow works with current device-binding behavior
- [ ] Confirm mobile login recognizes phone/unique-identification formats produced by onboarding
- [ ] Confirm client-facing error messages are deterministic for OTP, face, timeout, provider failure, and account-not-found states

### BVN OTP And Face/Liveness

Current FE contract:

- `OTP_ONLY`: OTP must pass; face ignored
- `FACE_ONLY`: face must pass; OTP ignored
- `OTP_AND_FACE`: both must pass
- `OTP_OR_FACE`: face is used when face data is supplied, otherwise OTP
- explicit `verificationMethod=OTP` means OTP success should pass
- explicit `verificationMethod=FACE` means face failure should fail
- missing verification method with both inputs can allow either path as backward-compatible fallback

Plural-to-Identity expectations:

- FE may send email as the public subject reference
- Profiling resolves the internal subject reference before calling Identity
- for NGN BVN face, use `add_account_details.walletId` for the NGN account where available
- do not use CAD `reg_wallet_info.walletId` for NGN face verification
- Identity handles liveness scoring and face-match pass/fail decision
- Plural should treat Identity failure as verification failure, not as a soft success

- [ ] Confirm `identity-face-service` and internal ML provider are deployed together
- [ ] Confirm liveness session creation works from Plural through Identity to ML
- [ ] Confirm liveness verify works from Plural through Identity to ML
- [ ] Confirm BVN face comparison works from Plural through Identity to ML/BVN image source
- [ ] Confirm `bvnImageAvailable=false` is surfaced clearly so FE can visually fall back to OTP flow
- [ ] Confirm PII in Identity/ML logs is minimized before production

### Notifications And Email

Channels in scope:

- OTP/SMS
- email through SendGrid or the active provider
- push notification through APNS/FCM
- operational alerts through SNS/email/Slack

- [ ] Confirm SendGrid/API key is stored in Secrets Manager/SSM or encrypted config, not plain source
- [ ] Confirm email sender domain has SPF, DKIM, and DMARC aligned
- [ ] Confirm Gmail/Outlook spam placement is monitored during pilot
- [ ] Confirm transfer notification email send success/failure is logged with provider response
- [ ] Confirm OTP SMS send success/failure is logged with provider response
- [ ] Confirm push notification token update works on login and device change
- [ ] Confirm ops alerts are wired for service down, ALB 5xx, RDS pressure, queue pressure, Redis pressure, and high application error rate

## Pre-Freeze Hygiene

- [ ] Remove untracked build artifacts from source trees before packaging
- [ ] Specifically clean `finacial-wealth-api-profiling/BOOT-INF/`
- [ ] Confirm there are no local-only temporary DB workarounds left undocumented
- [ ] Confirm no test/mock bypass endpoint is reachable through the public gateway
- [ ] Confirm all mock flags and bypass flags are disabled outside approved dev/local environments
- [ ] Confirm packaged jars/images were built from the stable branch head
- [ ] Record exact git commit/hash for each service artifact sent to pilot or prod

## Internal-Only Test And Bypass Controls

### CAD onboarding bypass

Purpose:

- controlled dev/local creation of CAD-ready users without external KYC calls
- used only for pilot data setup and internal QA acceleration

Route:

- service-local only: `POST /internal/test/onboarding/cad-user`

Required controls:

- `test.onboarding.bypass.enabled` must default to `false`
- bypass must only work with active Spring profile `dev` or `local`
- `test.onboarding.bypass.key` must be set only in approved dev/local environments
- gateway must block `/internal/test/onboarding/**` and any prefixed route such as `/api/profiling/internal/test/onboarding/**`
- route must not be documented as a public FE/API route

Go / No-Go:

- GO for dev/local only if profiling starts with `dev` or `local`, bypass key is configured, and the call is made directly inside the server/private network
- NO-GO for pilot/prod internet exposure if the route is reachable through `https://finacialwealth.com/api/...`
- NO-GO for prod if `test.onboarding.bypass.enabled=true` in any production-like profile

Verification:

- [ ] Direct internal dev call succeeds only with valid `X-Test-Bypass-Key`
- [ ] Direct internal dev call fails with invalid/missing key
- [ ] Public gateway call to `/api/profiling/internal/test/onboarding/cad-user` returns not found/blocked
- [ ] App fails startup if bypass is enabled under any profile other than `dev` or `local`
- [ ] Prod/prod-like config review confirms bypass flag is absent or explicitly `false`

## Java And Build Compatibility

- `finacial-wealth-api-profiling` runs on Java 8
- `finacial-wealth-api-fxpeer-exchange` runs on Java 21
- `finacial-wealth-backoffice-service` runs on Java 21
- `sessionmanager`, `utility`, and `transactions` must be verified against the current environment JDK/runtime actually used in pilot/prod

- [ ] Confirm build agents and deployment hosts use the correct Java version per service
- [ ] Confirm no Java 11+ APIs leaked into profiling
- [ ] Confirm final artifacts were built without stale Docker/cache layers

## Schema And Data Preparation

## 1. Profiling Market Tables

### `MARKET_DEFINITION`

Purpose:

- canonical market metadata for multi-market orchestration

Required seeded rows:

- `CA_RETAIL`
- `NG_RETAIL`

Key expectations:

- bootstrap upserts both rows at startup
- table audit columns are strict and must remain healthy:
  - `created_by`
  - `created_date`

- [ ] Verify `MARKET_DEFINITION` exists
- [ ] Verify `CA_RETAIL` row exists and is enabled
- [ ] Verify `NG_RETAIL` row exists and is enabled
- [ ] Verify bootstrap logs show both rows being ensured on startup
- [ ] Verify `created_by` and `created_date` are no longer being inserted as null

### `CUSTOMER_MARKET_PROFILE`

Purpose:

- per-customer multi-market lifecycle tracking

Expected fields to validate in pilot data:

- `customerId`
- `marketCode`
- `status`
- `kycStatus`
- `accountProvisionStatus`
- `walletProvisionStatus`
- `smartCoreCustomerId`
- `smartCoreAccountId`
- `walletId`
- `localAccountNumber`
- `virtualAccountNumber`

- [ ] Verify table exists
- [ ] Verify new CAD/NG customer flows create or update profiles
- [ ] Verify legacy customers entering NG add-account flow get usable market profiles

## 2. Profiling Referral Tables

### `referral_programs`

Purpose:

- product-scoped referral program configuration

Required readiness:

- at least one active `P2P` program for pilot if P2P referral is expected to work

Key enum-backed fields:

- `product_type`: `P2P`, `AIRTIME`, `INVESTMENT`
- `reward_target`: `REFERRER_ONLY`, `REFEREE_ONLY`, `BOTH`
- `reward_mode`: `FLAT_AMOUNT`, `PERCENTAGE_OF_TRANSACTION`
- `reward_currency_mode`: `TRADE_CURRENCY`, `FIXED_CURRENCY`
- `status`: `DRAFT`, `ACTIVE`, `PAUSED`, `ENDED`

- [ ] Verify table exists
- [ ] Verify active program row exists for `P2P` if referral is being piloted
- [ ] Verify `program_code` is unique
- [ ] Verify `qualifying_transaction_count`, reward values, and date windows are correct

### `referral_attributions`

Purpose:

- applied referral state, qualification, payout status, and idempotency

Key enum-backed fields:

- `status`: `APPLIED`, `QUALIFIED_PENDING_PAYOUT`, `REWARDED`, `CANCELLED`

Key operational fields:

- `qualified_transaction_id`
- `qualified_amount`
- `qualified_currency_code`
- `reward_currency_code`
- `referrer_reward_amount`
- `referee_reward_amount`
- `referrer_reward_paid`
- `referee_reward_paid`
- `referrer_payout_reference`
- `referee_payout_reference`

- [ ] Verify table exists
- [ ] Verify uniqueness by `(referee_wallet_id, product_type)`
- [ ] Verify payout references are written after reward posting
- [ ] Verify failed payout leaves attribution pending instead of corrupting trade flow

### `referral_program_audit`

- [ ] Verify table exists
- [ ] Verify create/update/activate/pause/end actions are being audited

## 3. FXPeer Investment Table Change

### `fx_investment_position`

Required column:

- `valuation_method`

Purpose:

- pin valuation mode to the position instead of the mutable product

- [ ] Verify column exists in every environment
- [ ] Verify new positions stamp `valuation_method`
- [ ] Verify legacy positions with null valuation method are still valued safely

## 4. Existing Customer Referral Fields

The release relies on customer referral fields already present on wallet/customer records.

- [ ] Verify `referralCode` and `referralCodeLink` exist on stored customer data
- [ ] Verify legacy users without these values are backfilled lazily on login
- [ ] Verify malformed historic links are corrected on login

## Environment Config Inventory

## 1. App Config Rows

`app_config` is a runtime control table for the platform. Treat it as production configuration data, not casual seed data.

Mandatory controls:

- [ ] Export full `app_config` from dev, pilot, and prod before go-live comparison
- [ ] Classify every key as `business config`, `feature flag`, `provider endpoint`, `credential/secret`, `template`, `limit`, or `legacy`
- [ ] Move credentials/secrets to Secrets Manager/SSM Parameter Store where possible
- [ ] If a secret must remain in DB temporarily, confirm it is encrypted and has an owner-approved retirement plan
- [ ] Produce an idempotent upsert script for required rows
- [ ] Produce a rollback script or backup snapshot before modifying prod config rows
- [ ] Compare dev/pilot/prod config drift before every release candidate
- [ ] Confirm every mandatory config key has startup validation or an explicit smoke test
- [ ] Confirm config changes are auditable by owner, date, reason, and environment
- [ ] Register every backoffice-editable key in `bo_app_config_registry` with owner service, value type, sensitivity, editability, and validation rule
- [ ] Confirm `bo_app_config_change_history` captures every pilot/prod config edit and masks sensitive values
- [ ] Confirm stale/mock/test config values are not present in pilot/prod
- [ ] Confirm all service timeouts and retry values are explicit, not inherited accidentally from dev

Config categories to review:

- environment base URLs
- gateway and internal service URLs
- SmartCore URLs and auth values
- external KYC/BVN provider URLs and auth values
- Identity/liveness/face verification URLs and thresholds
- SendGrid/email provider config
- OTP/SMS provider config
- push notification config
- referral link base and referral GL accounts
- feature flags and bypass flags
- fee/rate/limit config
- currency/country/market config
- notification templates
- product/investment config
- timeout/retry/circuit-breaker config

Go / No-Go:

- GO only if the pilot/prod `app_config` set is exported, reviewed, backed up, and every release-critical key is present
- NO-GO if any provider credential, bypass flag, mock endpoint, or unknown dev URL remains in pilot/prod

### Release-critical keys to inventory

### Referral link base

- `onboardRefLink`

Expected behavior:

- login/onboarding should produce a normalized share link such as `https://.../<code>`

- [ ] Dev value present
- [ ] Pilot value present
- [ ] Prod value present
- [ ] Value format verified so link concatenation is correct

### Referral funding GL config

These are required when referral reward payout is enabled:

- `NGN_REFERRAL_GGL_ACCOUNT`
- `NGN_REFERRAL_GGL_CODE`
- `CAD_REFERRAL_GGL_ACCOUNT`
- `CAD_REFERRAL_GGL_CODE`

Rules:

- `*_ACCOUNT` values are encrypted in DB
- `*_CODE` values are plain
- FXPeer decrypts only the account values before posting

- [ ] Dev rows present
- [ ] Pilot rows present
- [ ] Prod rows present
- [ ] Encryption confirmed for `*_ACCOUNT`
- [ ] `NGN` and `CAD` codes correct
- [ ] GL funding accounts are approved by finance/operations

### BVN face/liveness config

Required when face verification is enabled:

- Identity service base URL
- liveness session endpoint
- liveness verify endpoint
- BVN face validation endpoint
- internal provider timeout
- request/response logging flag for debug only

- [ ] Dev rows/properties present
- [ ] Pilot rows/properties present
- [ ] Prod rows/properties present
- [ ] Debug request/response logging is disabled or PII-redacted before production
- [ ] Identity/liveness thresholds are owned by Identity, not duplicated silently in Plural

### Notification config

Required checks:

- SendGrid/API email key
- email sender name/address
- email domain verification config
- OTP/SMS provider auth
- push notification/APNS/FCM config
- ops alert recipients or SNS topics

- [ ] Email send path tested
- [ ] Email failure path logged
- [ ] OTP send path tested
- [ ] OTP failure path logged
- [ ] Push token update tested
- [ ] SNS/email/Slack ops alert tested

## 2. Service Property / Secret Review

### Profiling

- BreezePay merchant id/code/auth/subscription values
- request authorizer values
- profile/environment switches
- any SDK onboarding provider values

### FXPeer

- transaction-service connectivity
- profiling-service connectivity
- encryption/decryption compatibility for app config

### Sessionmanager

- JWT signing/verification
- login response mapping

### Utility

- OTP encryption key
- referral base-link lookup working against app config

### Backoffice

- profiling integration host/auth
- approval/admin settings

- [ ] Build a per-environment config sheet for all secrets and endpoints
- [ ] Mark each value as `plain`, `encrypted`, or `secret manager only`
- [ ] Confirm pilot and prod have matching key material where required

### Infrastructure-backed config

- Route 53 public hostnames
- ALB DNS names and health-check paths
- ACM certificate ARNs
- WAF Web ACL association IDs
- RDS endpoints
- Redis/ElastiCache endpoint
- RabbitMQ/Amazon MQ endpoint
- S3 bucket names
- CloudWatch log group names and retention
- ECR repository names
- KMS key aliases

- [ ] Confirm application properties and `app_config` use the final DNS/service names, not temporary IPs
- [ ] Confirm secrets/config are loaded through the deployment script or environment exports consistently
- [ ] Confirm any value required by the deployment script has the same name as the application property

## Feature/Business Data Readiness

- [ ] Decide whether referral pilot is enabled at launch or deferred
- [ ] If enabled, create and activate the `P2P` referral program before QA signoff
- [ ] Confirm reward mode for pilot:
  - `FLAT_AMOUNT` or `PERCENTAGE_OF_TRANSACTION`
- [ ] Confirm reward target for pilot:
  - `REFERRER_ONLY`, `REFEREE_ONLY`, or `BOTH`
- [ ] Confirm qualifying transaction count
- [ ] Confirm min qualifying amount, min reward, and max reward
- [ ] Confirm reward currency mode:
  - `TRADE_CURRENCY` or `FIXED_CURRENCY`
- [ ] Confirm the business actually wants NG product `UNIT_PRICE` changes deferred or disabled if not fully QA’d

## Pilot Smoke-Test Matrix

## 1. Profiling / Onboarding

- [ ] New CAD onboarding succeeds
- [ ] New NG onboarding/add-account path succeeds
- [ ] NGN OTP verification succeeds when `verificationMethod=OTP`
- [ ] NGN face verification succeeds when `verificationMethod=FACE`
- [ ] Face failure blocks onboarding when `verificationMethod=FACE`
- [ ] OTP success still passes when `verificationMethod=OTP`, even if face data is absent
- [ ] `bvnImageAvailable=false` leads to FE fallback to OTP flow
- [ ] Legacy CAD user still logs in normally
- [ ] Legacy NG user can add account without breaking existing data
- [ ] `CA_RETAIL` and `NG_RETAIL` market definitions are present after startup
- [ ] `CUSTOMER_MARKET_PROFILE` rows are created/updated correctly
- [ ] CAD bypass-created user can login only after all expected real tables are populated correctly
- [ ] CAD bypass remains blocked outside `dev` or `local`

## 2. Login / Session / Dashboard

- [ ] Login response returns `referralCode`
- [ ] Login response returns normalized `referralCodeLink`
- [ ] JWT claims include `referralCode`
- [ ] JWT claims include `referralCodeLink`
- [ ] Legacy customer with no referral values gets backfilled on login
- [ ] Device-change OTP confirm is idempotent when the same device is already active
- [ ] Login works with normalized phone/unique-identification format expected by mobile

## 3. P2P Trade And Referral

- [ ] Trade works with no referral code
- [ ] New user can apply valid referral code before first trade
- [ ] Invalid referral code returns correct user-facing error
- [ ] Already-applied referral returns correct user-facing error
- [ ] Referrer must have prior completed trade
- [ ] Referee with prior completed trade is rejected
- [ ] First qualified trade moves attribution to qualified state
- [ ] Reward payout posts debit from referral GL and credit to beneficiary
- [ ] Successful payout marks attribution `REWARDED`
- [ ] Failed payout does not roll back the successful trade

## 4. Investment

- [ ] New `RATE` investments accrue correctly
- [ ] Existing NG money-market positions no longer flatline at `5000.00`
- [ ] Top-up logic respects position valuation mode
- [ ] Liquidation logic respects position valuation mode
- [ ] Future history rows continue from corrected live value

## 5. Backoffice

- [ ] `GET /bo/backoffice/investments/products/{productCode}` works
- [ ] Product edit/activate flow works in UI/API
- [ ] Referral-program admin APIs work:
  - create
  - update
  - activate
  - pause
  - end
  - get active by product

## 6. Transactions Dependency

- [ ] `/peer-to-peer/batch-post-with-type` is healthy
- [ ] Batch reward posting handles debit/credit pairs correctly
- [ ] Reward settlement references are traceable in downstream records

## 7. SmartCore Integration

- [ ] Customer creation/update succeeds
- [ ] Account creation/readiness succeeds
- [ ] Ledger/posting succeeds
- [ ] Posting failure/reversal scenario is tested
- [ ] Plural reference maps to SmartCore reference in logs and database
- [ ] SmartCore service timeout behavior is known and user-facing error is clean

## 8. Notifications

- [ ] OTP SMS is delivered
- [ ] OTP SMS failure is logged and returned cleanly
- [ ] Transfer email is delivered
- [ ] Transfer email failure is logged and returned cleanly
- [ ] SendGrid/provider response status is visible in logs
- [ ] Push notification token update is persisted on login
- [ ] Ops alert fires for a controlled failure or manual alarm test

## 9. Infrastructure Smoke

- [ ] Route 53 DNS resolves correctly
- [ ] ACM certificate is valid
- [ ] WAF is associated and logs/metrics are visible
- [ ] ALB target groups are healthy
- [ ] Private app nodes cannot be reached directly from the internet
- [ ] RDS is reachable only from approved service security groups
- [ ] Redis/ElastiCache is reachable only from approved service security groups
- [ ] RabbitMQ/Amazon MQ is reachable only from approved service security groups
- [ ] S3 write/read smoke test succeeds for expected buckets
- [ ] CloudWatch logs are flowing from all deployed services
- [ ] CloudWatch alarms have tested notification destinations

## Production Cutover Checklist

## Before deployment

- [ ] Freeze stable branch for release candidate
- [ ] Tag the release candidate commits
- [ ] Build artifacts fresh with no stale caches
- [ ] Confirm DB DDL required by release is already applied
- [ ] Confirm app config rows exist in prod before service rollout
- [ ] Confirm finance-approved referral GL accounts are funded and active
- [ ] Confirm AWS infrastructure baseline is provisioned and smoke-tested
- [ ] Confirm SmartCore services and databases are migrated before dependent Plural flows
- [ ] Confirm `app_config` backup/export is captured immediately before production edits
- [ ] Confirm all external provider endpoints and credentials are production-approved
- [ ] Confirm SendGrid/email domain authentication is complete
- [ ] Confirm WAF, Route 53, ACM TLS, and ALB health checks are production-ready

## Deployment order

Recommended order:

1. `finacial-wealth-api-profiling`
2. `finacial-wealth-gateway`
3. `finacial-wealth-api-utility`
4. `finacial-wealth-api-sessionmanager`
5. `finacial-wealth-api-fxpeer-exchange`
6. `finacial-wealth-backoffice-service`
7. verify `finacial-wealth-api-transactions` and `identity-face-service` dependency health

Reason:

- profiling owns market bootstrap and referral runtime
- gateway owns public route exposure and must block internal-only test/bypass paths
- utility/sessionmanager depend on referral data exposure
- FXPeer depends on profiling runtime and transactions health
- backoffice depends on profiling referral admin endpoints
- identity-face-service is a runtime dependency for face/liveness verification

## After deployment

- [ ] Check profiling startup logs for market bootstrap success
- [ ] Check gateway blocks `/api/profiling/internal/test/onboarding/**`
- [ ] Check no audit/bootstrap startup exceptions occur
- [ ] Check login returns referral data correctly
- [ ] Check referral program active lookup works
- [ ] Execute at least one controlled P2P referral test
- [ ] Execute at least one controlled investment valuation test
- [ ] Execute one backoffice product edit test
- [ ] Execute one backoffice global search test: `/bo/backoffice/search?q=<known customer/product>`
- [ ] Execute one saved view create/list/delete test: `/bo/backoffice/saved-views`
- [ ] Execute one dependency health test: `/bo/backoffice/system/integration-health`
- [ ] Execute one SmartCore posting test and reconcile references
- [ ] Execute one OTP notification test
- [ ] Execute one email notification test
- [ ] Execute one face/liveness verification test if enabled
- [ ] Confirm CloudWatch dashboard and alarms show live metrics

## Rollback Notes

- Do not rollback only one service in the referral stack without checking compatibility across:
  - profiling
  - utility
  - sessionmanager
  - FXPeer
  - backoffice

- If referral runtime must be disabled quickly:
  - pause or end the active referral program in profiling/backoffice
  - do not delete attribution records
  - keep referral GL config intact for reconciliation

- If profiling bootstrap fails in a future environment:
  - inspect `MARKET_DEFINITION` audit columns first
  - confirm the deployed artifact contains the bootstrap audit fix
  - do not leave relaxed null constraints undocumented

## Known Open Items To Resolve Before Final Go-Live Signoff

- [ ] Decide whether referral is pilot-enabled or production-enabled on day one
- [ ] Produce a full env-by-env secret/config matrix outside source control
- [ ] Confirm whether transactions service needs its own explicit release note for referral payout dependency
- [ ] Confirm if historical investment rows need one-time backfill beyond live-position recovery
- [ ] Confirm if any additional products beyond `P2P` should be turned on for referral later, but keep them disabled for initial rollout unless separately QA’d

## Recommended Working Approach From Now To July 1

- treat every remaining change as a checklist item, not an ad hoc fix
- keep pilot findings and prod readiness notes in this file
- update this document after each deployment, DB change, or config decision
- require a pass across all smoke sections before moving from pilot to production
