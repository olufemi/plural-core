package com.finacial.wealth.backoffice.savedview.dto;

import java.time.Instant;
import java.util.Map;

public record SavedViewResponse(
        Long id,
        String moduleKey,
        String name,
        Map<String, Object> filters,
        boolean defaultView,
        Instant createdAt,
        Instant updatedAt
) {
}
