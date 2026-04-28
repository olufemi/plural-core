package com.finacial.wealth.backoffice.auth.dto;

public record PasswordRecoveryCompleteRequest(
        String challengeId,
        String code,
        String newPassword,
        String confirmPassword
) {}
