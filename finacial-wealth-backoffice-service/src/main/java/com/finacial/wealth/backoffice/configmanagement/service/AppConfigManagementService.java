package com.finacial.wealth.backoffice.configmanagement.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finacial.wealth.backoffice.auth.service.AdminAuditService;
import com.finacial.wealth.backoffice.configmanagement.dto.AppConfigHistoryResponse;
import com.finacial.wealth.backoffice.configmanagement.dto.AppConfigRegistryRequest;
import com.finacial.wealth.backoffice.configmanagement.dto.AppConfigResponse;
import com.finacial.wealth.backoffice.configmanagement.dto.AppConfigRollbackRequest;
import com.finacial.wealth.backoffice.configmanagement.dto.AppConfigUpdateRequest;
import com.finacial.wealth.backoffice.configmanagement.entity.AppConfigEntry;
import com.finacial.wealth.backoffice.configmanagement.entity.BoAppConfigChangeHistory;
import com.finacial.wealth.backoffice.configmanagement.entity.BoAppConfigRegistry;
import com.finacial.wealth.backoffice.configmanagement.repo.AppConfigEntryRepository;
import com.finacial.wealth.backoffice.configmanagement.repo.BoAppConfigChangeHistoryRepository;
import com.finacial.wealth.backoffice.configmanagement.repo.BoAppConfigRegistryRepository;
import com.finacial.wealth.backoffice.notification.entity.BackofficeNotificationSeverity;
import com.finacial.wealth.backoffice.notification.service.BackofficeNotificationService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppConfigManagementService {

    private static final String MASK = "***MASKED***";
    private static final List<String> CONFIG_PERMISSIONS = List.of("app_config.view", "app_config.manage");

    private final AppConfigEntryRepository appConfigEntryRepository;
    private final BoAppConfigRegistryRepository registryRepository;
    private final BoAppConfigChangeHistoryRepository historyRepository;
    private final AdminAuditService adminAuditService;
    private final BackofficeNotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public Page<AppConfigResponse> list(String search, Integer page, Integer size) {
        PageRequest pageRequest = PageRequest.of(safePage(page), safeSize(size));
        String q = search == null ? "" : search.trim();
        Page<AppConfigEntry> entries = q.isBlank()
                ? appConfigEntryRepository.findAll(pageRequest)
                : appConfigEntryRepository.findByConfigNameContainingIgnoreCaseOrConfigDescriptionContainingIgnoreCase(q, q, pageRequest);
        return entries.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public AppConfigResponse get(String configName) {
        AppConfigEntry entry = appConfigEntryRepository.findFirstByConfigNameIgnoreCase(required(configName, "configName"))
                .orElseThrow(() -> new IllegalArgumentException("App config not found"));
        return toResponse(entry);
    }

    @Transactional
    public AppConfigResponse register(String configName, AppConfigRegistryRequest request, Long actorAdminId, HttpServletRequest httpRequest) {
        if (request == null) {
            throw new IllegalArgumentException("Registry request is required");
        }
        String normalizedName = required(configName, "configName");
        AppConfigEntry entry = appConfigEntryRepository.findFirstByConfigNameIgnoreCase(normalizedName)
                .orElseThrow(() -> new IllegalArgumentException("App config key does not exist in app_config"));
        BoAppConfigRegistry registry = registryRepository.findFirstByConfigNameIgnoreCase(normalizedName)
                .orElseGet(() -> BoAppConfigRegistry.builder().configName(entry.getConfigName()).build());

        registry.setOwnerService(normalizeText(request.ownerService(), "SHARED").toUpperCase(Locale.ROOT));
        registry.setValueType(normalizeValueType(request.valueType()));
        registry.setEditable(Boolean.TRUE.equals(request.editable()));
        registry.setSensitive(Boolean.TRUE.equals(request.sensitive()));
        registry.setValidationRegex(blankToNull(request.validationRegex()));
        registry.setDescription(blankToNull(request.description()));
        registryRepository.save(registry);

        adminAuditService.audit("APP_CONFIG_REGISTER", actorAdminId, "AppConfig", entry.getId(),
                httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"),
                Map.of("configName", entry.getConfigName(), "editable", registry.isEditable(), "sensitive", registry.isSensitive()));
        return toResponse(entry, registry);
    }

    @Transactional
    public AppConfigResponse update(String configName, AppConfigUpdateRequest request, Long actorAdminId, HttpServletRequest httpRequest) {
        String normalizedName = required(configName, "configName");
        String newValue = request == null ? null : request.configValue();
        String reason = request == null ? null : request.reason();
        if (newValue == null) {
            throw new IllegalArgumentException("configValue is required");
        }
        if (reason == null || reason.trim().length() < 8) {
            throw new IllegalArgumentException("reason must be at least 8 characters");
        }

        AppConfigEntry entry = appConfigEntryRepository.findFirstByConfigNameIgnoreCase(normalizedName)
                .orElseThrow(() -> new IllegalArgumentException("App config not found"));
        BoAppConfigRegistry registry = registryRepository.findFirstByConfigNameIgnoreCase(entry.getConfigName())
                .orElseThrow(() -> new IllegalArgumentException("App config key must be registered before update"));
        if (!registry.isEditable()) {
            throw new IllegalArgumentException("App config key is not marked editable");
        }

        validateValue(registry, newValue);
        String oldValue = entry.getConfigValue();
        entry.setConfigValue(newValue);
        appConfigEntryRepository.save(entry);

        boolean sensitive = registry.isSensitive();
        historyRepository.save(BoAppConfigChangeHistory.builder()
                .appConfigId(entry.getId())
                .configName(entry.getConfigName())
                .oldValue(sensitive ? MASK : oldValue)
                .newValue(sensitive ? MASK : newValue)
                .actorAdminId(actorAdminId)
                .reason(reason.trim())
                .build());

        Map<String, Object> auditMeta = new LinkedHashMap<>();
        auditMeta.put("configName", entry.getConfigName());
        auditMeta.put("oldValue", sensitive ? MASK : oldValue);
        auditMeta.put("newValue", sensitive ? MASK : newValue);
        auditMeta.put("reason", reason.trim());
        adminAuditService.audit("APP_CONFIG_UPDATE", actorAdminId, "AppConfig", entry.getId(),
                httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"), auditMeta);

        notificationService.notifyUsersWithAnyPermission(CONFIG_PERMISSIONS,
                "APP_CONFIG",
                BackofficeNotificationSeverity.WARNING,
                "App config updated",
                "App config " + entry.getConfigName() + " was updated.",
                "AppConfig",
                entry.getConfigName(),
                Map.of("configName", entry.getConfigName(), "actorAdminId", actorAdminId));

        return toResponse(entry, registry);
    }

    @Transactional(readOnly = true)
    public AppConfigUpdateRequest buildRollbackUpdateRequest(String configName, Long historyId, AppConfigRollbackRequest request) {
        BoAppConfigChangeHistory history = rollbackHistory(configName, historyId);
        String reason = request == null ? null : request.reason();
        if (reason == null || reason.trim().length() < 8) {
            throw new IllegalArgumentException("reason must be at least 8 characters");
        }
        return new AppConfigUpdateRequest(
                history.getOldValue(),
                "Rollback to history #" + history.getId() + ": " + reason.trim()
        );
    }

    @Transactional
    public AppConfigResponse rollback(String configName, Long historyId, AppConfigRollbackRequest request,
            Long actorAdminId, HttpServletRequest httpRequest) {
        AppConfigUpdateRequest updateRequest = buildRollbackUpdateRequest(configName, historyId, request);
        return update(configName, updateRequest, actorAdminId, httpRequest);
    }

    @Transactional(readOnly = true)
    public Page<AppConfigHistoryResponse> history(String configName, Integer page, Integer size) {
        return historyRepository.findByConfigNameIgnoreCaseOrderByCreatedAtDesc(
                required(configName, "configName"),
                PageRequest.of(safePage(page), safeSize(size)))
                .map(history -> new AppConfigHistoryResponse(
                history.getId(),
                history.getAppConfigId(),
                history.getConfigName(),
                history.getOldValue(),
                history.getNewValue(),
                history.getActorAdminId(),
                history.getReason(),
                history.getCreatedAt()));
    }

    private AppConfigResponse toResponse(AppConfigEntry entry) {
        return toResponse(entry, registryRepository.findFirstByConfigNameIgnoreCase(entry.getConfigName()).orElse(null));
    }

    private BoAppConfigChangeHistory rollbackHistory(String configName, Long historyId) {
        String normalizedName = required(configName, "configName");
        if (historyId == null) {
            throw new IllegalArgumentException("historyId is required");
        }
        BoAppConfigChangeHistory history = historyRepository.findById(historyId)
                .orElseThrow(() -> new IllegalArgumentException("App config history not found"));
        if (!normalizedName.equalsIgnoreCase(history.getConfigName())) {
            throw new IllegalArgumentException("History item does not belong to configName");
        }
        if (history.getOldValue() == null || MASK.equals(history.getOldValue())) {
            throw new IllegalArgumentException("This history item cannot be rolled back because the previous value is not available");
        }
        return history;
    }

    private AppConfigResponse toResponse(AppConfigEntry entry, BoAppConfigRegistry registry) {
        boolean registered = registry != null;
        boolean sensitive = registered && registry.isSensitive();
        return new AppConfigResponse(
                entry.getId(),
                entry.getConfigName(),
                entry.getConfigDescription(),
                sensitive ? MASK : entry.getConfigValue(),
                registered,
                registered && registry.isEditable(),
                sensitive,
                registered ? registry.getOwnerService() : null,
                registered ? registry.getValueType() : null);
    }

    private void validateValue(BoAppConfigRegistry registry, String value) {
        String type = normalizeValueType(registry.getValueType());
        switch (type) {
            case "BOOLEAN" -> {
                if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
                    throw new IllegalArgumentException("BOOLEAN config must be true or false");
                }
            }
            case "NUMBER" -> {
                try {
                    new java.math.BigDecimal(value);
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("NUMBER config must be numeric");
                }
            }
            case "JSON" -> {
                try {
                    objectMapper.readTree(value);
                } catch (Exception ex) {
                    throw new IllegalArgumentException("JSON config must be valid JSON");
                }
            }
            case "URL" -> {
                if (!value.startsWith("http://") && !value.startsWith("https://")) {
                    throw new IllegalArgumentException("URL config must start with http:// or https://");
                }
            }
            default -> {
            }
        }
        if (registry.getValidationRegex() != null && !value.matches(registry.getValidationRegex())) {
            throw new IllegalArgumentException("App config value does not match validation rule");
        }
    }

    private String normalizeValueType(String valueType) {
        String normalized = normalizeText(valueType, "STRING").toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "STRING", "JSON", "NUMBER", "BOOLEAN", "URL", "SECRET" -> normalized;
            default -> throw new IllegalArgumentException("Unsupported valueType");
        };
    }

    private String required(String value, String field) {
        if (value == null || value.trim().isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    private String normalizeText(String value, String fallback) {
        return value == null || value.trim().isBlank() ? fallback : value.trim();
    }

    private String blankToNull(String value) {
        return value == null || value.trim().isBlank() ? null : value.trim();
    }

    private int safePage(Integer page) {
        return page == null || page < 0 ? 0 : page;
    }

    private int safeSize(Integer size) {
        if (size == null || size < 1) {
            return 20;
        }
        return Math.min(size, 100);
    }
}
