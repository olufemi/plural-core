package com.finacial.wealth.backoffice.auth.dto;

public record PasswordRecoveryStartResponse(
        String status,
        String challengeId,
        String emailAddress,
        String message
) {}
