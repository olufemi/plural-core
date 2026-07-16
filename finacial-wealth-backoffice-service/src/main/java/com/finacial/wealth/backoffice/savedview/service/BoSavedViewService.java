package com.finacial.wealth.backoffice.savedview.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finacial.wealth.backoffice.savedview.dto.SavedViewRequest;
import com.finacial.wealth.backoffice.savedview.dto.SavedViewResponse;
import com.finacial.wealth.backoffice.savedview.entity.BoSavedView;
import com.finacial.wealth.backoffice.savedview.repo.BoSavedViewRepository;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoSavedViewService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final BoSavedViewRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<SavedViewResponse> list(Long adminUserId, String moduleKey) {
        String normalizedModuleKey = normalize(moduleKey);
        return repository.findByAdminUserIdAndModuleKeyOrderByDefaultViewDescUpdatedAtDesc(adminUserId, normalizedModuleKey)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SavedViewResponse save(Long adminUserId, SavedViewRequest request) {
        String normalizedModuleKey = normalize(request.moduleKey());
        if (request.defaultView()) {
            clearDefaults(adminUserId, normalizedModuleKey);
        }
        BoSavedView view = new BoSavedView();
        view.setAdminUserId(adminUserId);
        view.setModuleKey(normalizedModuleKey);
        view.setName(requireText(request.name(), "name"));
        view.setFiltersJson(writeJson(request.filters()));
        view.setDefaultView(request.defaultView());
        return toResponse(repository.save(view));
    }

    @Transactional
    public SavedViewResponse setDefault(Long adminUserId, Long viewId) {
        BoSavedView view = repository.findByIdAndAdminUserId(viewId, adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("Saved view not found"));
        clearDefaults(adminUserId, view.getModuleKey());
        view.setDefaultView(true);
        return toResponse(repository.save(view));
    }

    @Transactional
    public Map<String, Object> delete(Long adminUserId, Long viewId) {
        BoSavedView view = repository.findByIdAndAdminUserId(viewId, adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("Saved view not found"));
        repository.delete(view);
        return Map.of("deleted", true, "id", viewId);
    }

    private void clearDefaults(Long adminUserId, String moduleKey) {
        repository.findByAdminUserIdAndModuleKeyAndDefaultViewTrue(adminUserId, moduleKey)
                .forEach(view -> {
                    view.setDefaultView(false);
                    repository.save(view);
                });
    }

    private SavedViewResponse toResponse(BoSavedView view) {
        return new SavedViewResponse(
                view.getId(),
                view.getModuleKey(),
                view.getName(),
                readJson(view.getFiltersJson()),
                view.isDefaultView(),
                view.getCreatedAt(),
                view.getUpdatedAt()
        );
    }

    private String normalize(String value) {
        return requireText(value, "moduleKey").trim().toUpperCase(Locale.ROOT);
    }

    private String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    private String writeJson(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Map.of() : value);
        } catch (Exception ex) {
            throw new IllegalArgumentException("filters must be valid JSON", ex);
        }
    }

    private Map<String, Object> readJson(String value) {
        try {
            return objectMapper.readValue(value, MAP_TYPE);
        } catch (Exception ex) {
            return Map.of();
        }
    }
}
