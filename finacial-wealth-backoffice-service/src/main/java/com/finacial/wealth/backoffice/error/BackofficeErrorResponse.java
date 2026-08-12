package com.finacial.wealth.backoffice.error;

import java.time.Instant;

public record BackofficeErrorResponse(
        int status,
        String code,
        String message,
        String requestId,
        String path,
        Instant timestamp,
        Object details
) {
}
