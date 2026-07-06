# BVN OTP/Face Verification Flow

This service owns the Plural Nigeria account-addition flow for BVN ownership verification before provisioning an NGN account. The current production-safe default remains OTP, and face verification is optional/configurable for pilot.

## Services Involved

* Profiling service: `finacial-wealth-api-profiling`
* Utility service: OTP send/validate
* Identity face service: biometric face comparison
* Breeze virtual account service: NGN virtual account provisioning, when `fin.wealth.goto.breeze=1`

## Configuration

```properties
fin.wealth.identity.face.base.url=${IDENTITY_FACE_BASE_URL:http://localhost:60008}
fin.wealth.identity.face.minimum-score=${IDENTITY_FACE_MINIMUM_SCORE:0.85}
fin.wealth.identity.bvn.verification-mode=${BVN_VERIFICATION_MODE:OTP_OR_FACE}
```

Allowed `BVN_VERIFICATION_MODE` values:

* `OTP_ONLY`: OTP must pass. Face payload is ignored.
* `FACE_ONLY`: face verification must pass. OTP is ignored.
* `OTP_AND_FACE`: OTP and face verification must both pass.
* `OTP_OR_FACE`: default. Explicit `verificationMethod` controls the path. If no method is supplied and both OTP and face data are supplied, either one can pass.

## Step 1: Validate BVN and Send OTP

Endpoint:

```http
POST /wallet-mgt/validate/bvn?bvn={bvn}
```

The service fetches or refreshes BVN details, stores them in `bvn_lookup`, sends an OTP to the BVN phone number, and returns `requestId`/`processId`.

The cached BVN record must contain `base64Image` before face verification can work.

## Step 2: Add NGN Account

Endpoint:

```http
POST /wallet-mgt/add-other-currency-account
```

Existing OTP-only FE payload still works:

```json
{
  "countryCode": "NG",
  "country": "Nigeria",
  "bvn": "12345678901",
  "requestId": "otp-request-id",
  "otp": 123456
}
```

Explicit OTP payload:

```json
{
  "countryCode": "NG",
  "country": "Nigeria",
  "bvn": "12345678901",
  "requestId": "otp-request-id",
  "otp": 123456,
  "verificationMethod": "OTP"
}
```

Explicit face payload:

```json
{
  "countryCode": "NG",
  "country": "Nigeria",
  "bvn": "12345678901",
  "verificationMethod": "FACE",
  "liveFaceBase64": "base64-live-face-image",
  "livenessSessionReference": "optional-session-reference"
}
```

Fallback either-method payload:

```json
{
  "countryCode": "NG",
  "country": "Nigeria",
  "bvn": "12345678901",
  "requestId": "otp-request-id",
  "otp": 123456,
  "liveFaceBase64": "base64-live-face-image"
}
```

For `OTP_OR_FACE`, behavior is:

* `verificationMethod=FACE`: face must pass. OTP will not rescue a failed face check.
* `verificationMethod=OTP`: OTP must pass. Face is ignored.
* No `verificationMethod` and both OTP + face are supplied: OTP is tried first; if OTP fails, face is tried.
* No `verificationMethod` and only OTP is supplied: OTP must pass.
* No `verificationMethod` and only face is supplied: face must pass.

`countryCode` and `country` remain supported for backward compatibility. The service currently forces this endpoint to Nigeria internally:

```java
rq.setCountryCode("NG");
rq.setCountry("Nigeria");
```

## Face Verification Call

When face verification is required, profiling calls:

```http
POST {IDENTITY_FACE_BASE_URL}/api/v1/verification/compare
```

Request body sent to identity face service:

```json
{
  "requestReference": "NG-BVN-FACE-{requestId-or-generated-id}",
  "imageABase64": "liveFaceBase64",
  "imageBBase64": "bvn_lookup.base64Image",
  "purpose": "NG_BVN_ACCOUNT_OPENING",
  "livenessSessionReference": "optional-session-reference"
}
```

Profiling does not log biometric base64 payloads.

## Face Pass Criteria

Profiling treats face verification as passed only when identity face service returns:

```json
{
  "success": true,
  "data": {
    "decision": "APPROVED",
    "matchOutcome": "MATCH",
    "similarityScore": 0.85
  }
}
```

The `similarityScore` must be greater than or equal to `fin.wealth.identity.face.minimum-score`. Liveness/spoof rules should be owned by identity face service; profiling should obey the final `decision`.

When face succeeds, useful metadata is returned in the add-account response under `faceVerification`, including `verificationId`, `similarityScore`, `confidenceScore`, `decision`, `matchOutcome`, `provider`, and `providerReference`.

## Common Failure Messages

* `Please validate BVN with OTP`: OTP path needs a valid `requestId`/OTP process.
* `Please complete face verification`: face path was selected but `liveFaceBase64` is missing.
* `BVN image not available for face verification`: cached BVN has no `base64Image`.
* `Face verification failed`: identity face service rejected the comparison or returned an error.
* `BVN ownership verification failed`: fallback mode was attempted but neither OTP nor face passed.
* `Transaction is already completed!`: OTP process was already used.

## Deployment Notes

Deploy `finacial-wealth-api-profiling` for this flow.

For pilot with current OTP behavior preserved:

```properties
BVN_VERIFICATION_MODE=OTP_OR_FACE
IDENTITY_FACE_MINIMUM_SCORE=0.85
IDENTITY_FACE_BASE_URL=http://<identity-face-service-host>:60008
```

If identity face service is not deployed yet, existing OTP-only FE payloads still work as long as FE does not send `verificationMethod=FACE` or `liveFaceBase64`.

## Smoke Test Checklist

1. Call `/wallet-mgt/validate/bvn` and confirm OTP request data is returned.
2. Call `/wallet-mgt/add-other-currency-account` with the existing OTP payload and confirm account creation.
3. Call the same endpoint with an invalid OTP and confirm failure.
4. If identity face service is deployed, call with `verificationMethod=FACE` and a valid `liveFaceBase64`.
5. Confirm no logs contain face base64 data.
