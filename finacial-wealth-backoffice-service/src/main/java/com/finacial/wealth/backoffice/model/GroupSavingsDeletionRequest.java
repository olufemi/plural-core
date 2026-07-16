package com.finacial.wealth.backoffice.model;

import java.util.LinkedHashMap;
import java.util.Map;

public record GroupSavingsDeletionRequest(
        String reason,
        String expectedStatus
) {
    public Map<String, Object> toDownstreamPayload(Long groupId) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason is required");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("groupId", groupId);
        body.put("reason", reason.trim());
        if (expectedStatus != null && !expectedStatus.isBlank()) {
            body.put("expectedStatus", expectedStatus.trim());
        }
        return body;
    }
}
