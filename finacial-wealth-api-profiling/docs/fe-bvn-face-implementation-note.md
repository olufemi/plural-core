# FE Implementation Note: BVN OTP/Face Verification

Update on BVN ownership verification for the NGN add-account.

Existing OTP flow still works as-is, so the current FE payload is not broken.

Endpoint:

```http
POST /walletmgt/add-other-currency-account
```

## Existing OTP Payload

This can remain unchanged:

```json
{
  "countryCode": "NG",
  "country": "Nigeria",
  "bvn": "12345678901",
  "requestId": "otp-request-id",
  "otp": 123456
}
```

## New Optional Fields

```json
{
  "verificationMethod": "OTP | FACE",
  "liveFaceBase64": "base64-live-face-image",
  "livenessSessionReference": "optional-liveness-session-ref"
}
```

## Recommended FE Behavior

### OTP

For OTP, first call:

```http
POST /walletmgt/validate/bvn
```

This validates the BVN and sends OTP.

If the user chooses OTP, send:

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

### Face

For Face, first call:

```http
POST /walletmgt/bvn-face/validate-bvn
```

```json
{
  "bvn": "12345678901"
}
```

This validates/caches the BVN details required for face comparison. It does not send OTP.

If the user chooses Face, send:

```json
{
  "countryCode": "NG",
  "country": "Nigeria",
  "bvn": "12345678901",
  "verificationMethod": "FACE",
  "liveFaceBase64": "base64-live-face-image",
  "livenessSessionReference": "optional-liveness-session-ref"
}
```

## Backend Rule Already Implemented

* If FE sends `verificationMethod=FACE`, face verification must pass. OTP will not rescue a failed face check.
* If FE sends `verificationMethod=OTP`, OTP must pass. Face is ignored.
* If FE sends no `verificationMethod` but sends both OTP and face data, backend allows either one to pass.
* If FE sends no `verificationMethod` and only OTP data, backend uses OTP.
* If FE sends no `verificationMethod` and only face data, backend uses face.

## Liveness Handling

Profiling now handles liveness on behalf of FE. FE should call only the Plural profiling APIs below.

### Validate BVN For Face

FE calls this before liveness/session when the user chooses Face:

```http
POST /walletmgt/bvn-face/validate-bvn
```

```json
{
  "bvn": "12345678901"
}
```

Sample success response:

```json
{
  "statusCode": 200,
  "description": "BVN validated successfully",
  "data": {
    "bvnValidated": true,
    "bvnImageAvailable": true
  }
}
```

Sample failure response:

```json
{
  "statusCode": 400,
  "description": "Unable to process request"
}
```

### Create Liveness Session

FE calls:

```http
POST /walletmgt/bvn-face/liveness/session
```

```json
{
  "subjectReference": "CUS-001",
  "requestReference": "REQ-LIVE-SESSION-001",
  "consentReference": "CONSENT-001",
  "channel": "WEB"
}
```

Sample success response:

```json
{
  "statusCode": 200,
  "description": "Liveness session created",
  "data": {
    "success": true,
    "code": "00",
    "correlationId": "REQ-LIVE-SESSION-001",
    "result": {
      "livenessSessionId": "8c8a9b3d-8897-4e13-9879-7b0f9c5b1f9a",
      "sessionReference": "LIVE-SESSION-REF-001",
      "provider": "SMARTCORE_IDENTITY",
      "providerSessionId": "PROVIDER-SESSION-001",
      "expiresAt": "2026-07-06T12:30:00Z",
      "status": "PENDING"
    }
  }
}
```

FE should keep `data.result.sessionReference` and send it as `livenessSessionReference` in the add-account request.

Sample failure response:

```json
{
  "statusCode": 400,
  "description": "Unable to create liveness session"
}
```

### Add Account With Face And Liveness

For the final add-account call, FE should send:

```json
{
  "countryCode": "NG",
  "country": "Nigeria",
  "bvn": "12345678901",
  "verificationMethod": "FACE",
  "liveFaceBase64": "base64-live-face-image",
  "livenessSessionReference": "session-reference-from-create-session"
}
```

When `livenessSessionReference` is present, profiling handles the liveness verification and BVN face comparison internally before account creation continues.

Sample success response:

```json
{
  "statusCode": 200,
  "description": "Account added successfully.",
  "data": {
    "accountNumber": "0123456789",
    "countryCode": "NG",
    "countryName": "Nigeria",
    "faceVerification": {
      "verificationId": "7d00a1e6-6632-4b9f-8a59-3a60ef5d4ad8",
      "similarityScore": 0.92,
      "confidenceScore": 0.96,
      "decision": "APPROVED",
      "matchOutcome": "MATCH",
      "provider": "SMARTCORE_IDENTITY",
      "providerReference": "FACE-VERIFY-001",
      "livenessVerification": {
        "sessionReference": "LIVE-SESSION-REF-001",
        "livenessScore": 95,
        "spoofScore": 5,
        "confidenceScore": 92,
        "decision": "APPROVED",
        "provider": "SMARTCORE_IDENTITY",
        "providerReference": "LIVE-VERIFY-001"
      }
    }
  }
}
```

Sample liveness failure response:

```json
{
  "statusCode": 400,
  "description": "Liveness verification failed",
  "data": {
    "sessionReference": "LIVE-SESSION-REF-001",
    "livenessScore": 62,
    "spoofScore": 41,
    "confidenceScore": 70,
    "decision": "REJECTED",
    "reasons": [
      "LIVENESS_SCORE_BELOW_THRESHOLD"
    ]
  }
}
```

Sample face failure response:

```json
{
  "statusCode": 400,
  "description": "Face verification failed",
  "data": {
    "similarityScore": 0.48,
    "confidenceScore": 0.81,
    "decision": "REJECTED",
    "matchOutcome": "NO_MATCH",
    "livenessVerification": {
      "sessionReference": "LIVE-SESSION-REF-001",
      "decision": "APPROVED"
    }
  }
}
```

Recommended account-opening flow:

1. Call `/walletmgt/bvn-face/validate-bvn`.
2. Call `/walletmgt/bvn-face/liveness/session`.
3. Capture the live face image.
4. Call `/walletmgt/add-other-currency-account` with `verificationMethod=FACE`, `liveFaceBase64`, and `livenessSessionReference`.
5. Let profiling complete the liveness and BVN face checks internally.

## Liveness Decision Rule

FE should not calculate final pass/fail from:

```json
{
  "livenessScore": 95,
  "spoofScore": 5,
  "confidenceScore": 92
}
```

Profiling owns the final pass/fail decision for the add-account flow. FE should display the response from profiling.

## Useful Failure Messages

FE can display these directly:

* `Please validate BVN with OTP`
* `Please complete face verification`
* `BVN image not available for face verification`
* `Liveness verification failed`
* `Face verification failed`
* `BVN ownership verification failed`
* `Transaction is already completed!`
