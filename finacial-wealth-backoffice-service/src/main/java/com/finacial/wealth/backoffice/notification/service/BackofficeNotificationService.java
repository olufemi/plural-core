package com.finacial.wealth.backoffice.notification.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finacial.wealth.backoffice.auth.entity.BoAdminRole;
import com.finacial.wealth.backoffice.auth.entity.BoAdminUser;
import com.finacial.wealth.backoffice.auth.entity.BoPermission;
import com.finacial.wealth.backoffice.auth.repo.BoAdminUserRepository;
import com.finacial.wealth.backoffice.notification.dto.BackofficeNotificationResponse;
import com.finacial.wealth.backoffice.notification.entity.BackofficeNotificationSeverity;
import com.finacial.wealth.backoffice.notification.entity.BackofficeNotificationStatus;
import com.finacial.wealth.backoffice.notification.entity.BoBackofficeNotification;
import com.finacial.wealth.backoffice.notification.repo.BoBackofficeNotificationRepository;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BackofficeNotificationService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final BoBackofficeNotificationRepository notificationRepository;
    private final BoAdminUserRepository adminUserRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void notifyUsersWithAnyPermission(Collection<String> permissionCodes,
            String category,
            BackofficeNotificationSeverity severity,
            String title,
            String message,
            String entityType,
            String entityRef,
            Object metadata) {
        Set<String> normalizedPermissionCodes = normalize(permissionCodes);
        Set<Long> notifiedAdminIds = new LinkedHashSet<>();

        for (BoAdminUser adminUser : adminUserRepository.findAll()) {
            if (adminUser.getStatus() != BoAdminUser.Status.ACTIVE) {
                continue;
            }
            if (!hasAnyPermissionOrSuperAdmin(adminUser, normalizedPermissionCodes)) {
                continue;
            }
            if (notifiedAdminIds.add(adminUser.getId())) {
                createForAdmin(adminUser.getId(), category, severity, title, message, entityType, entityRef, metadata);
            }
        }
    }

    @Transactional
    public void createForAdmin(Long recipientAdminId,
            String category,
            BackofficeNotificationSeverity severity,
            String title,
            String message,
            String entityType,
            String entityRef,
            Object metadata) {
        if (recipientAdminId == null) {
            return;
        }

        notificationRepository.save(BoBackofficeNotification.builder()
                .recipientAdminId(recipientAdminId)
                .category(defaultString(category, "GENERAL"))
                .severity(severity == null ? BackofficeNotificationSeverity.INFO : severity)
                .status(BackofficeNotificationStatus.UNREAD)
                .title(trimToLength(title, 190))
                .message(trimToLength(message, 500))
                .entityType(trimToLength(entityType, 120))
                .entityRef(trimToLength(entityRef, 190))
                .metadataJson(writeJson(metadata))
                .build());
    }

    @Transactional(readOnly = true)
    public Page<BackofficeNotificationResponse> list(Long adminUserId, boolean unreadOnly, Integer page, Integer size) {
        PageRequest pageRequest = PageRequest.of(safePage(page), safeSize(size));
        Page<BoBackofficeNotification> notifications = unreadOnly
                ? notificationRepository.findByRecipientAdminIdAndReadAtIsNullOrderByCreatedAtDesc(adminUserId, pageRequest)
                : notificationRepository.findByRecipientAdminIdOrderByCreatedAtDesc(adminUserId, pageRequest);
        return notifications.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> unreadCount(Long adminUserId) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("unreadCount", notificationRepository.countByRecipientAdminIdAndReadAtIsNull(adminUserId));
        return response;
    }

    @Transactional
    public BackofficeNotificationResponse markRead(Long notificationId, Long adminUserId) {
        BoBackofficeNotification notification = notificationRepository.findByIdAndRecipientAdminId(notificationId, adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        markRead(notification);
        return toResponse(notification);
    }

    @Transactional
    public Map<String, Object> markAllRead(Long adminUserId) {
        Page<BoBackofficeNotification> unread = notificationRepository
                .findByRecipientAdminIdAndReadAtIsNullOrderByCreatedAtDesc(adminUserId, PageRequest.of(0, 500));
        int count = 0;
        while (!unread.isEmpty()) {
            for (BoBackofficeNotification notification : unread.getContent()) {
                markRead(notification);
                count++;
            }
            unread = notificationRepository
                    .findByRecipientAdminIdAndReadAtIsNullOrderByCreatedAtDesc(adminUserId, PageRequest.of(0, 500));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("markedRead", count);
        return response;
    }

    private void markRead(BoBackofficeNotification notification) {
        if (notification.getReadAt() != null) {
            return;
        }
        notification.setStatus(BackofficeNotificationStatus.READ);
        notification.setReadAt(Instant.now());
        notificationRepository.save(notification);
    }

    private boolean hasAnyPermissionOrSuperAdmin(BoAdminUser adminUser, Set<String> permissionCodes) {
        if (adminUser.getRoles() == null || adminUser.getRoles().isEmpty()) {
            return false;
        }

        for (BoAdminRole role : adminUser.getRoles()) {
            if (role.getName() != null && "SUPER_ADMIN".equalsIgnoreCase(role.getName())) {
                return true;
            }
            if (role.getPermissions() == null) {
                continue;
            }
            for (BoPermission permission : role.getPermissions()) {
                if (permission.getCode() != null && permissionCodes.contains(permission.getCode().toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }

    private Set<String> normalize(Collection<String> permissionCodes) {
        if (permissionCodes == null || permissionCodes.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> normalized = new LinkedHashSet<>();
        for (String permissionCode : permissionCodes) {
            if (permissionCode != null && !permissionCode.isBlank()) {
                normalized.add(permissionCode.trim().toLowerCase());
            }
        }
        return normalized;
    }

    private BackofficeNotificationResponse toResponse(BoBackofficeNotification notification) {
        return new BackofficeNotificationResponse(
                notification.getId(),
                notification.getCategory(),
                notification.getSeverity(),
                notification.getStatus(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getEntityType(),
                notification.getEntityRef(),
                readJsonMap(notification.getMetadataJson()),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }

    private Map<String, Object> readJsonMap(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(value, MAP_TYPE);
        } catch (Exception ignored) {
            return Collections.emptyMap();
        }
    }

    private String writeJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String trimToLength(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }

    private int safePage(Integer page) {
        return page == null ? 0 : Math.max(page, 0);
    }

    private int safeSize(Integer size) {
        return size == null ? 20 : Math.min(Math.max(size, 1), 100);
    }
}
