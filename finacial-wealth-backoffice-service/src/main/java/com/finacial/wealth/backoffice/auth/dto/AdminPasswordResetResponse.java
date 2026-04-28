package com.finacial.wealth.backoffice.auth.dto;

public record AdminPasswordResetResponse(
        Long adminId,
        String email,
        String temporaryPassword,
        String message
) {}
