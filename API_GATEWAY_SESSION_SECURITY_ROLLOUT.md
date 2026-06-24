# API Gateway and Session Security Rollout

## Purpose

This document tracks the security hardening plan for Plural's API gateway, session-manager, JWT verification, and mobile device handling.

The immediate goal is to reduce the risk of forged or replayed JWTs while avoiding frontend changes for the first rollout.

## Current Context

Primary services reviewed:

- `finacial-wealth-gateway`
- `finacial-wealth-api-sessionmanager`
- `finacial-wealth-api-profiling`

Current mobile login payload already includes the device fields needed for server-side device binding:

```json
{
  "emailAddress": "user@example.com",
  "password": "********",
  "uuid": "314EB75C-3A66-48BA-B5B1-4D81CC94C181",
  "appType": null,
  "pushNotificationToken": "firebase-token",
  "appVersion": "1.0",
  "deviceId": "314EB75C-3A66-48BA-B5B1-4D81CC94C181",
  "devicePublicSpkiB64": "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE..."
}
```

The gateway currently delegates token verification to session-manager through `/session/verify`.

Session-manager currently verifies JWT signature and expiry, with optional blacklist checking. The important risk is that the verification flow does not yet prove that the token was actually issued by a successful login session. If the shared HS256 JWT secret is leaked, an attacker could generate a signed token without calling the login API.

## Device Field Meaning

Use these meanings consistently across backend and mobile:

- `deviceId`: stable app/device installation identifier sent by the mobile app.
- `uuid`: legacy user-device identifier. It may currently be the same value as `deviceId`; keep supporting that while migrating.
- `devicePublicSpkiB64`: public key used to verify device-held private-key signatures.
- `pushNotificationToken`: notification routing only. Do not treat this as a security identity because it can rotate.
- `appVersion` and `appType`: device/app metadata for compatibility, risk checks, and support diagnostics.

## Phase 1: Backend-Only Hardening

Status: implemented on branch `feature/after-first-user-experience-test-on-dev-more-security`.

Frontend impact: no frontend change expected.

Reason: the existing login payload already sends `deviceId`, `uuid`, and `devicePublicSpkiB64`, and authenticated calls already send `Authorization: Bearer <token>`.

Phase 1 should preserve:

- existing login endpoint request shape
- existing token response field name
- existing `Authorization: Bearer <token>` usage
- existing gateway routing behavior

Implemented backend work:

1. Added a real issued-token/session id to JWTs.
   - Add `jti` as a random token id, not the user id.
   - Add `sid` as a login session id.
   - Add `token_type=ACCESS`.

2. Persist issued sessions server-side.
   - Store `jti`, `sid`, `userId`, `customerId`, `deviceId`, `uuid`, issued time, expiry time, and status in Redis or a session table.
   - Mark the session active only after login succeeds.

3. Strengthened `/session/verify`.
   - Continue checking JWT signature and expiry.
   - Validate issuer.
   - Validate subject or audience where applicable.
   - Validate `token_type`.
   - Confirm `jti` exists in the server-side issued-token store.
   - Confirm the stored session is active and not revoked.
   - Confirm stored expiry has not passed.
   - Confirm stored user/customer/device data matches token claims.

4. Improved logout.
   - On logout, revoke `jti` or `sid` in the issued-token store.
   - Keep blacklist support as defense-in-depth, but do not rely on blacklist alone.

5. Removed production default JWT secret risk.
   - Do not allow production to start with the fallback default `SESSION_MANAGER_JWT_KEY`.
   - Require a strong secret in the environment or secret manager.

6. Kept the gateway compatible.
   - The gateway can continue calling session-manager `/session/verify`.
   - No frontend route or header change is required in this phase.

Phase 1 expected result:

- A token signed with the leaked secret is no longer enough.
- The token must also exist in the server-side issued-session store.
- Logging in from the current mobile app still works without payload changes.

## Phase 2: Device Header Binding

Status: tracked for later.

Frontend impact: yes, small frontend/mobile change.

Required frontend/mobile change:

- Send `X-Device-Id` on authenticated API requests.

Backend behavior:

- Gateway or session-manager verifies that `X-Device-Id` matches the `deviceId` claim and the stored issued session.
- Requests with a missing or mismatched `X-Device-Id` are rejected.

Why this helps:

- It makes simple token replay from a different device easier to detect and block.

Rollout note:

- Implement behind a feature flag first.
- Start in report-only/log-only mode before enforcement.
- Communicate with FE before enabling enforcement.

## Phase 3: Signed Sensitive Requests

Status: tracked for later.

Frontend impact: yes, meaningful frontend/mobile change.

Required frontend/mobile change:

- Use the private key matching `devicePublicSpkiB64` to sign sensitive requests.
- Send headers such as:
  - `X-Device-Id`
  - `X-Device-Kid`
  - `X-Consent-Ts`
  - `X-Consent-Nonce`
  - `X-Consent-Sig`

Backend behavior:

- Verify timestamp freshness.
- Verify nonce has not been reused.
- Verify request body hash.
- Verify signature using the stored device public key.
- Apply this first to sensitive APIs such as transfers, FXPeer acceptance, wallet movement, PIN changes, and beneficiary/device changes.

Existing foundation:

- Profiling already has consent/device-signature style header handling in the consent verification flow.
- Phase 3 should reuse or standardize that pattern instead of inventing a separate signing contract.

Why this helps:

- A stolen JWT alone cannot authorize high-risk actions unless the attacker also has the device private key.

## Frontend Impact Conclusion

Phase 1 should not require frontend changes if the backend preserves the existing request and response contract.

Phase 2 requires frontend/mobile to add `X-Device-Id` to authenticated requests.

Phase 3 requires frontend/mobile to sign selected sensitive requests with the device private key and send signature headers.

Summary:

| Phase | Backend Change | Frontend Change | When |
| --- | --- | --- | --- |
| Phase 1 | Issued-session/JTI validation, stronger JWT verification, safer secret handling | No | Now |
| Phase 2 | Enforce device header binding | Yes, small | Later |
| Phase 3 | Signed sensitive requests | Yes, meaningful | Later |

## Message To Frontend Team

For the immediate backend security hardening, no frontend change is expected. The existing login payload already includes `deviceId`, `uuid`, and `devicePublicSpkiB64`, and normal API calls can continue using `Authorization: Bearer <token>`.

Later, we plan two additional hardening phases that will need FE/mobile support:

1. Add `X-Device-Id` to authenticated requests.
2. Sign selected sensitive requests using the device private key and send signature headers.

These later phases should be planned with FE before enforcement. Backend should release them behind feature flags or report-only mode first.

## Tracking Items

- [x] Phase 1: Add `jti`, `sid`, and `token_type` to session-manager JWTs.
- [x] Phase 1: Persist issued sessions server-side.
- [x] Phase 1: Update `/session/verify` to require an active issued session.
- [x] Phase 1: Revoke issued session on logout.
- [x] Phase 1: Fail production startup when JWT secret uses an unsafe default.
- [ ] Phase 2: Define and communicate `X-Device-Id` requirement.
- [ ] Phase 2: Add report-only backend validation for `X-Device-Id`.
- [ ] Phase 2: Enforce `X-Device-Id` after FE rollout.
- [ ] Phase 3: Standardize signed request headers and canonical payload rules.
- [ ] Phase 3: Add signed request enforcement for sensitive APIs.
