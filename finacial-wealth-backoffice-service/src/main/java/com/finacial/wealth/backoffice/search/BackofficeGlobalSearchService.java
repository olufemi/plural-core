package com.finacial.wealth.backoffice.search;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finacial.wealth.backoffice.approval.entity.BoApprovalRequest;
import com.finacial.wealth.backoffice.approval.repo.BoApprovalRequestRepository;
import com.finacial.wealth.backoffice.integrations.fxpeer.FxPeerExchangeClient;
import com.finacial.wealth.backoffice.integrations.profiling.ProfilingClient;
import com.finacial.wealth.backoffice.integrations.transactions.TransactionsClient;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BackofficeGlobalSearchService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };
    private static final Set<String> ALL_TYPES = Set.of("CUSTOMER", "PRODUCT", "APPROVAL", "GROUP");

    private final ProfilingClient profilingClient;
    private final FxPeerExchangeClient fxPeerExchangeClient;
    private final TransactionsClient transactionsClient;
    private final BoApprovalRequestRepository approvalRepository;
    private final ObjectMapper objectMapper;

    public Map<String, Object> search(String q, List<String> types, int page, int size) {
        String query = q == null ? "" : q.trim();
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        Set<String> selectedTypes = normalizeTypes(types);

        List<Map<String, Object>> sections = new ArrayList<>();
        if (selectedTypes.contains("CUSTOMER")) {
            sections.add(section("CUSTOMER", () -> customerSearch(safePage, safeSize)));
        }
        if (selectedTypes.contains("PRODUCT")) {
            sections.add(section("PRODUCT", fxPeerExchangeClient::getInvestmentProducts));
        }
        if (selectedTypes.contains("APPROVAL")) {
            sections.add(section("APPROVAL", () -> approvalSearch(query, safePage, safeSize)));
        }
        if (selectedTypes.contains("GROUP")) {
            sections.add(section("GROUP", () -> transactionsClient.listGroupSavingsGroups(null, query, safePage, safeSize)));
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("query", query);
        response.put("types", selectedTypes);
        response.put("page", safePage);
        response.put("size", safeSize);
        response.put("sections", sections);
        response.put("note", "Global search aggregates live downstream backoffice data. A section can be unavailable without failing the whole search.");
        return response;
    }

    private Map<String, Object> customerSearch(int page, int size) {
        return objectMapper.convertValue(profilingClient.getAllCustomers(page, size, "id,desc"), MAP_TYPE);
    }

    private Map<String, Object> approvalSearch(String query, int page, int size) {
        List<Map<String, Object>> approvals = approvalRepository
                .findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .filter(approval -> query.isBlank() || approvalContains(approval, query))
                .map(this::approvalResult)
                .toList();
        return Map.of("content", approvals, "page", page, "size", size);
    }

    private Map<String, Object> approvalResult(BoApprovalRequest approval) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", approval.getId());
        result.put("module", approval.getModule());
        result.put("subModule", approval.getSubModule());
        result.put("entityType", approval.getEntityType());
        result.put("entityRef", approval.getEntityRef());
        result.put("actionType", approval.getActionType());
        result.put("status", approval.getStatus());
        result.put("makerAdminId", approval.getMakerAdminId());
        result.put("checkerAdminId", approval.getCheckerAdminId());
        result.put("createdAt", approval.getCreatedAt());
        result.put("updatedAt", approval.getUpdatedAt());
        return result;
    }

    private boolean approvalContains(BoApprovalRequest approval, String query) {
        String needle = query.toLowerCase(Locale.ROOT);
        return String.valueOf(approval.getId()).contains(needle)
                || contains(approval.getEntityRef(), needle)
                || contains(String.valueOf(approval.getStatus()), needle)
                || contains(String.valueOf(approval.getModule()), needle)
                || contains(String.valueOf(approval.getSubModule()), needle)
                || contains(String.valueOf(approval.getEntityType()), needle);
    }

    private boolean contains(String value, String needle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
    }

    private Map<String, Object> section(String type, Supplier<Map<String, Object>> supplier) {
        Instant start = Instant.now();
        Map<String, Object> section = new LinkedHashMap<>();
        section.put("type", type);
        try {
            section.put("status", "AVAILABLE");
            section.put("data", supplier.get());
        } catch (RuntimeException ex) {
            section.put("status", "UNAVAILABLE");
            section.put("error", ex.getClass().getSimpleName());
            section.put("message", "Source is currently unavailable.");
        }
        section.put("durationMs", Duration.between(start, Instant.now()).toMillis());
        return section;
    }

    private Set<String> normalizeTypes(Collection<String> types) {
        if (types == null || types.isEmpty()) {
            return ALL_TYPES;
        }
        Set<String> normalized = types.stream()
                .filter(type -> type != null && !type.isBlank())
                .map(type -> type.trim().toUpperCase(Locale.ROOT))
                .filter(ALL_TYPES::contains)
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
        return normalized.isEmpty() ? ALL_TYPES : normalized;
    }
}
