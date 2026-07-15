package com.finacial.wealth.backoffice.configmanagement.dto;

import java.time.Instant;

public record AppConfigHistoryResponse(
        Long id,
        Long appConfigId,
        String configName,
        String oldValue,
        String newValue,
        Long actorAdminId,
        String reason,
        Instant createdAt
) {
}
