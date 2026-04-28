package com.finacial.wealth.backoffice.reversal.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finacial.wealth.backoffice.approval.entity.ApprovalActionType;
import com.finacial.wealth.backoffice.approval.entity.ApprovalEntityType;
import com.finacial.wealth.backoffice.approval.entity.ApprovalEventType;
import com.finacial.wealth.backoffice.approval.entity.ApprovalModule;
import com.finacial.wealth.backoffice.approval.entity.ApprovalStatus;
import com.finacial.wealth.backoffice.approval.entity.ApprovalSubModule;
import com.finacial.wealth.backoffice.approval.entity.BoApprovalEvent;
import com.finacial.wealth.backoffice.approval.entity.BoApprovalRequest;
import com.finacial.wealth.backoffice.approval.repo.BoApprovalEventRepository;
import com.finacial.wealth.backoffice.approval.repo.BoApprovalRequestRepository;
import com.finacial.wealth.backoffice.auth.service.AdminAuditService;
import com.finacial.wealth.backoffice.integrations.fxpeer.FxPeerExchangeClient;
import com.finacial.wealth.backoffice.integrations.transactions.TransactionsClient;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReversalExceptionService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final FxPeerExchangeClient fxPeerExchangeClient;
    private final TransactionsClient transactionsClient;
    private final BoApprovalRequestRepository approvalRequestRepository;
    private final BoApprovalEventRepository approvalEventRepository;
    private final AdminAuditService adminAuditService;
    private final ObjectMapper objectMapper;

    public Map<String, Object> getSummary(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        Map<String, Object> fxSummary = fxPeerExchangeClient.getAirtimeReversalSummary(auth);
        Map<String, Object> txSummary = transactionsClient.getReversalSummary();

        Map<String, Object> fxData = extractMap(fxSummary.get("data"));
        Map<String, Object> txData = extractMap(txSummary.get("data"));

        long fxTotal = longValue(fxData.get("totalCount"));
        long fxPending = longValue(fxData.get("pendingCount"));
        long fxFailed = longValue(fxData.get("failedCount"));
        long fxSuccess = longValue(fxData.get("successfulCount"));

        long txTotal = longValue(txData.get("totalCount"));
        long txPending = longValue(txData.get("pendingCount"));
        long txFailed = longValue(txData.get("failedCount"));
        long txSuccess = longValue(txData.get("successfulCount"));

        Map<String, Object> sources = new LinkedHashMap<>();
        sources.put("FXPEER_AIRTIME", normalizeSummary("FXPEER_AIRTIME", fxSummary, fxData));
        sources.put("TRANSACTIONS", normalizeSummary("TRANSACTIONS", txSummary, txData));

        Map<String, Object> combined = new LinkedHashMap<>();
        combined.put("totalCount", fxTotal + txTotal);
        combined.put("pendingCount", fxPending + txPending);
        combined.put("failedCount", fxFailed + txFailed);
        combined.put("successfulCount", fxSuccess + txSuccess);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("summary", combined);
        response.put("sources", sources);
        return response;
    }

    public Map<String, Object> listCases(String source, String status, Integer page, Integer size, HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        List<Map<String, Object>> items = new ArrayList<>();

        if (source == null || source.isBlank() || source.equalsIgnoreCase("FXPEER_AIRTIME")) {
            items.addAll(fetchFxpeerCases(auth, status));
        }
        if (source == null || source.isBlank() || source.equalsIgnoreCase("TRANSACTIONS")) {
            items.addAll(fetchTransactionCases(status));
        }

        items.sort(Comparator.comparing(this::requestedAtComparator, Comparator.nullsLast(Comparator.reverseOrder())));

        int safePage = page == null ? 0 : Math.max(page, 0);
        int safeSize = size == null ? 20 : Math.max(size, 1);
        int from = Math.min(safePage * safeSize, items.size());
        int to = Math.min(from + safeSize, items.size());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("content", items.subList(from, to));
        data.put("page", safePage);
        data.put("size", safeSize);
        data.put("totalElements", items.size());
        data.put("totalPages", items.isEmpty() ? 0 : (int) Math.ceil((double) items.size() / safeSize));
        return data;
    }

    @Transactional
    public Map<String, Object> requestManualReversal(String source, String caseRef, String notes, Long actorAdminId, HttpServletRequest request) {
        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("source is required");
        }
        if (caseRef == null || caseRef.isBlank()) {
            throw new IllegalArgumentException("caseRef is required");
        }

        String normalizedSource = source.trim().toUpperCase(Locale.ROOT);
        ApprovalEntityType entityType = resolveEntityType(normalizedSource);
        ApprovalSubModule subModule = resolveSubModule(normalizedSource);
        Map<String, Object> caseSnapshot = fetchCaseSnapshot(normalizedSource, caseRef.trim(), request.getHeader("Authorization"));
        String caseStatus = stringValue(caseSnapshot.get("status"));
        if (!"PENDING".equalsIgnoreCase(caseStatus) && !"FAILED".equalsIgnoreCase(caseStatus)) {
            throw new IllegalArgumentException("Only FAILED or PENDING reversal cases can be submitted for manual reversal");
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("source", normalizedSource);
        payload.put("caseRef", caseRef.trim());
        payload.put("notes", trimToNull(notes));
        payload.put("requestedByAdminId", actorAdminId);
        payload.put("caseSnapshot", caseSnapshot);

        BoApprovalRequest approvalRequest = approvalRequestRepository.save(
                BoApprovalRequest.builder()
                        .module(ApprovalModule.REVERSAL)
                        .subModule(subModule)
                        .entityType(entityType)
                        .entityRef(caseRef.trim() + "::" + Instant.now().toEpochMilli())
                        .actionType(ApprovalActionType.MANUAL_REVERSAL)
                        .status(ApprovalStatus.PENDING)
                        .makerAdminId(actorAdminId)
                        .submittedAt(Instant.now())
                        .payloadJson(writeJson(payload))
                        .build()
        );

        approvalEventRepository.save(BoApprovalEvent.builder()
                .approvalRequestId(approvalRequest.getId())
                .eventType(ApprovalEventType.REQUESTED)
                .actorAdminId(actorAdminId)
                .notes(trimToNull(notes))
                .metadataJson(writeJson(payload))
                .build());

        adminAuditService.audit(
                "REVERSAL_MANUAL_REQUEST",
                actorAdminId,
                "BoApprovalRequest",
                approvalRequest.getId(),
                request.getRemoteAddr(),
                request.getHeader("User-Agent"),
                Map.of("source", normalizedSource, "caseRef", caseRef.trim(), "notes", trimToNull(notes))
        );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("approvalRequestId", approvalRequest.getId());
        response.put("status", approvalRequest.getStatus().name());
        response.put("source", normalizedSource);
        response.put("caseRef", caseRef.trim());
        response.put("notes", trimToNull(notes));
        return response;
    }

    private Map<String, Object> normalizeSummary(String source, Map<String, Object> rawResponse, Map<String, Object> rawData) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("source", source);
        item.put("statusCode", rawResponse == null ? null : rawResponse.get("statusCode"));
        item.put("description", rawResponse == null ? null : rawResponse.get("description"));
        item.put("totalCount", longValue(rawData.get("totalCount")));
        item.put("pendingCount", longValue(rawData.get("pendingCount")));
        item.put("failedCount", longValue(rawData.get("failedCount")));
        item.put("successfulCount", longValue(rawData.get("successfulCount")));
        return item;
    }

    private List<Map<String, Object>> fetchFxpeerCases(String auth, String status) {
        Map<String, Object> response = fxPeerExchangeClient.getAirtimeReversalCases(auth, status);
        Map<String, Object> data = extractMap(response.get("data"));
        List<Map<String, Object>> items = extractList(data.get("items"));
        return items.stream().map(item -> normalizeCase("FXPEER_AIRTIME", item, "processId")).toList();
    }

    private List<Map<String, Object>> fetchTransactionCases(String status) {
        Map<String, Object> response = transactionsClient.getReversalCases(status);
        Map<String, Object> data = extractMap(response.get("data"));
        List<Map<String, Object>> items = extractList(data.get("items"));
        return items.stream().map(item -> normalizeCase("TRANSACTIONS", item, "transactionId")).toList();
    }

    private Map<String, Object> fetchCaseSnapshot(String source, String caseRef, String auth) {
        List<Map<String, Object>> cases = "FXPEER_AIRTIME".equals(source)
                ? fetchFxpeerCases(auth, null)
                : fetchTransactionCases(null);
        return cases.stream()
                .filter(item -> caseRef.equals(stringValue(item.get("caseRef"))))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Reversal case not found"));
    }

    private Map<String, Object> normalizeCase(String source, Map<String, Object> raw, String refKey) {
        Map<String, Object> item = new LinkedHashMap<>();
        String caseRef = stringValue(raw.get(refKey));
        item.put("source", source);
        item.put("caseRef", caseRef);
        item.put("status", stringValue(raw.get("status")));
        item.put("requestedAt", raw.get("requestedAt"));
        item.put("completedAt", raw.get("completedAt"));
        item.put("retryCount", raw.get("retryCount"));
        item.put("lastError", raw.get("lastError"));
        item.put("serviceType", stringValue(raw.get("serviceType")));
        item.put("operator", stringValue(raw.get("operator")));
        item.put("product", stringValue(raw.get("product")));
        item.put("providerError", stringValue(raw.get("providerError")));
        item.put("legs", raw.getOrDefault("legs", Collections.emptyList()));
        item.put("raw", raw);
        item.put("openManualRequests", findOpenManualRequests(source, caseRef));
        return item;
    }

    private List<Map<String, Object>> findOpenManualRequests(String source, String caseRef) {
        if (caseRef == null) {
            return Collections.emptyList();
        }
        return approvalRequestRepository.findByStatusIn(List.of(ApprovalStatus.PENDING, ApprovalStatus.IN_REMEDIATION, ApprovalStatus.RESUBMITTED))
                .stream()
                .filter(req -> req.getModule() == ApprovalModule.REVERSAL)
                .filter(req -> {
                    Map<String, Object> payload = readJsonMap(req.getPayloadJson());
                    return source.equals(stringValue(payload.get("source"))) && caseRef.equals(stringValue(payload.get("caseRef")));
                })
                .map(req -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("approvalRequestId", req.getId());
                    item.put("status", req.getStatus() == null ? null : req.getStatus().name());
                    item.put("makerAdminId", req.getMakerAdminId());
                    item.put("createdAt", req.getCreatedAt());
                    item.put("submittedAt", req.getSubmittedAt());
                    return item;
                })
                .toList();
    }

    private ApprovalEntityType resolveEntityType(String source) {
        return switch (source) {
            case "FXPEER_AIRTIME" -> ApprovalEntityType.FXPEER_AIRTIME_REVERSAL;
            case "TRANSACTIONS" -> ApprovalEntityType.TRANSACTIONS_REVERSAL;
            default -> throw new IllegalArgumentException("Unsupported reversal source");
        };
    }

    private ApprovalSubModule resolveSubModule(String source) {
        return switch (source) {
            case "FXPEER_AIRTIME" -> ApprovalSubModule.AIRTIME_REVERSAL;
            case "TRANSACTIONS" -> ApprovalSubModule.TRANSACTION_REVERSAL;
            default -> throw new IllegalArgumentException("Unsupported reversal source");
        };
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractList(Object value) {
        if (value instanceof List<?> list && (list.isEmpty() || list.stream().allMatch(Map.class::isInstance))) {
            return (List<Map<String, Object>>) list;
        }
        return Collections.emptyList();
    }

    private Map<String, Object> extractMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> copy = new LinkedHashMap<>();
            map.forEach((k, v) -> copy.put(String.valueOf(k), v));
            return copy;
        }
        return Collections.emptyMap();
    }

    private Instant requestedAtComparator(Map<String, Object> item) {
        Object value = item.get("requestedAt");
        if (value instanceof Instant instant) {
            return instant;
        }
        if (value instanceof String text) {
            try {
                return Instant.parse(text);
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private long longValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception ignored) {
            return 0L;
        }
    }

    private String stringValue(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            return null;
        }
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
}
