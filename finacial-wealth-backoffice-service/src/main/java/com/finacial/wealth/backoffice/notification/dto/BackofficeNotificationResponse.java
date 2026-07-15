package com.finacial.wealth.backoffice.notification.dto;

import com.finacial.wealth.backoffice.notification.entity.BackofficeNotificationSeverity;
import com.finacial.wealth.backoffice.notification.entity.BackofficeNotificationStatus;
import java.time.Instant;
import java.util.Map;

public record BackofficeNotificationResponse(
        Long id,
        String category,
        BackofficeNotificationSeverity severity,
        BackofficeNotificationStatus status,
        String title,
        String message,
        String entityType,
        String entityRef,
        Map<String, Object> metadata,
        Instant createdAt,
        Instant readAt
) {
}
