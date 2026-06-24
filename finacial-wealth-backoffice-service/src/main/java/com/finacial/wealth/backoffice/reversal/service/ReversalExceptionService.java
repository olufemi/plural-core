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

    private static final String SOURCE_FXPEER_AIRTIME = "FXPEER_AIRTIME";
    private static final String SOURCE_TRANSACTIONS = "TRANSACTIONS";
    private static final String SOURCE_TRANSACTIONS_INTERBANK = "TRANSACTIONS_INTERBANK";
    private static final String SOURCE_TRANSACTIONS_LOCAL_TRANSFER = "TRANSACTIONS_LOCAL_TRANSFER";
    private static final List<String> TRANSACTION_SOURCE_ALIASES = List.of(
            SOURCE_TRANSACTIONS,
            SOURCE_TRANSACTIONS_INTERBANK,
            SOURCE_TRANSACTIONS_LOCAL_TRANSFER,
            "INTERBANK",
            "LOCAL_TRANSFER",
            "LOCAL"
    );

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
        List<Map<String, Object>> transactionCases = fetchTransactionCases(null);

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
        sources.put(SOURCE_FXPEER_AIRTIME, normalizeSummary(SOURCE_FXPEER_AIRTIME, fxSummary, fxData));
        Map<String, Object> transactionSources = summarizeCaseSources(transactionCases);
        if (transactionSources.isEmpty()) {
            Map<String, Object> transactionSummary = normalizeSummary(SOURCE_TRANSACTIONS_INTERBANK, txSummary, txData);
            transactionSummary.put("sourceGroup", SOURCE_TRANSACTIONS);
            transactionSummary.put("aliases", List.of(SOURCE_TRANSACTIONS, "INTERBANK"));
            transactionSources.put(SOURCE_TRANSACTIONS_INTERBANK, transactionSummary);
        }
        sources.putAll(transactionSources);

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
        if (source == null || source.isBlank() || isTransactionSource(source)) {
            List<Map<String, Object>> transactionCases = fetchTransactionCases(status);
            String normalizedSource = normalizeSource(source);
            if (normalizedSource != null && !SOURCE_TRANSACTIONS.equals(normalizedSource)) {
                transactionCases = transactionCases.stream()
                        .filter(item -> normalizedSource.equals(item.get("source")))
                        .toList();
            }
            items.addAll(transactionCases);
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

        String normalizedSource = normalizeSource(source);
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
        return items.stream().map(item -> normalizeCase(SOURCE_FXPEER_AIRTIME, item, "processId")).toList();
    }

    private List<Map<String, Object>> fetchTransactionCases(String status) {
        Map<String, Object> response = transactionsClient.getReversalCases(status);
        Map<String, Object> data = extractMap(response.get("data"));
        List<Map<String, Object>> items = extractList(data.get("items"));
        return items.stream().map(item -> normalizeCase(deriveTransactionSource(item), item, "transactionId")).toList();
    }

    private Map<String, Object> fetchCaseSnapshot(String source, String caseRef, String auth) {
        List<Map<String, Object>> cases = SOURCE_FXPEER_AIRTIME.equals(source)
                ? fetchFxpeerCases(auth, null)
                : fetchTransactionCases(null);
        return cases.stream()
                .filter(item -> caseRef.equals(stringValue(item.get("caseRef"))))
                .filter(item -> sameSourceGroup(source, stringValue(item.get("source"))))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Reversal case not found"));
    }

    private Map<String, Object> normalizeCase(String source, Map<String, Object> raw, String refKey) {
        Map<String, Object> item = new LinkedHashMap<>();
        String caseRef = stringValue(raw.get(refKey));
        item.put("source", source);
        item.put("sourceGroup", ownerSource(source));
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
                    return sameSourceGroup(source, stringValue(payload.get("source"))) && caseRef.equals(stringValue(payload.get("caseRef")));
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
        return switch (ownerSource(source)) {
            case SOURCE_FXPEER_AIRTIME -> ApprovalEntityType.FXPEER_AIRTIME_REVERSAL;
            case SOURCE_TRANSACTIONS -> ApprovalEntityType.TRANSACTIONS_REVERSAL;
            default -> throw new IllegalArgumentException("Unsupported reversal source");
        };
    }

    private ApprovalSubModule resolveSubModule(String source) {
        return switch (ownerSource(source)) {
            case SOURCE_FXPEER_AIRTIME -> ApprovalSubModule.AIRTIME_REVERSAL;
            case SOURCE_TRANSACTIONS -> ApprovalSubModule.TRANSACTION_REVERSAL;
            default -> throw new IllegalArgumentException("Unsupported reversal source");
        };
    }

    private Map<String, Object> summarizeCaseSources(List<Map<String, Object>> cases) {
        Map<String, Object> summaries = new LinkedHashMap<>();
        if (cases == null) {
            return summaries;
        }
        Map<String, List<Map<String, Object>>> grouped = cases.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        item -> stringValue(item.get("source")),
                        LinkedHashMap::new,
                        java.util.stream.Collectors.toList()
                ));
        grouped.forEach((source, items) -> {
            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("source", source);
            summary.put("sourceGroup", ownerSource(source));
            summary.put("totalCount", items.size());
            summary.put("pendingCount", countByStatus(items, "PENDING"));
            summary.put("failedCount", countByStatus(items, "FAILED"));
            summary.put("successfulCount", countByStatus(items, "SUCCESS"));
            summary.put("reconRequiredCount", countByStatus(items, "RECON_REQUIRED"));
            summaries.put(source, summary);
        });
        return summaries;
    }

    private long countByStatus(List<Map<String, Object>> items, String status) {
        return items.stream()
                .filter(item -> status.equalsIgnoreCase(stringValue(item.get("status"))))
                .count();
    }

    private String deriveTransactionSource(Map<String, Object> raw) {
        String text = (stringValue(raw.get("serviceType")) + " "
                + stringValue(raw.get("product")) + " "
                + stringValue(raw.get("source")) + " "
                + extractLegPayloadTypes(raw)).toUpperCase(Locale.ROOT);
        if (text.contains("LOCAL_TRANSFER") || text.contains("LOCALTRANSFER") || text.contains("WALLET_TO_WALLET")) {
            return SOURCE_TRANSACTIONS_LOCAL_TRANSFER;
        }
        return SOURCE_TRANSACTIONS_INTERBANK;
    }

    private String extractLegPayloadTypes(Map<String, Object> raw) {
        List<Map<String, Object>> legs = extractList(raw.get("legs"));
        return legs.stream()
                .map(leg -> stringValue(leg.get("payloadType")))
                .filter(Objects::nonNull)
                .reduce("", (left, right) -> left + " " + right);
    }

    private boolean isTransactionSource(String source) {
        String normalized = normalizeSource(source);
        return normalized != null && TRANSACTION_SOURCE_ALIASES.contains(normalized);
    }

    private String normalizeSource(String source) {
        if (source == null || source.isBlank()) {
            return null;
        }
        String normalized = source.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        return switch (normalized) {
            case "FXPEER_AIRTIME", "AIRTIME" -> SOURCE_FXPEER_AIRTIME;
            case "TRANSACTIONS", "TRANSACTION" -> SOURCE_TRANSACTIONS;
            case "INTERBANK", "TRANSACTIONS_INTERBANK", "NIP", "BANK_TRANSFER" -> SOURCE_TRANSACTIONS_INTERBANK;
            case "LOCAL", "LOCAL_TRANSFER", "LOCALTRANSFER", "WALLET_TO_WALLET", "TRANSACTIONS_LOCAL_TRANSFER" -> SOURCE_TRANSACTIONS_LOCAL_TRANSFER;
            default -> normalized;
        };
    }

    private String ownerSource(String source) {
        String normalized = normalizeSource(source);
        if (SOURCE_FXPEER_AIRTIME.equals(normalized)) {
            return SOURCE_FXPEER_AIRTIME;
        }
        if (normalized != null && TRANSACTION_SOURCE_ALIASES.contains(normalized)) {
            return SOURCE_TRANSACTIONS;
        }
        return normalized;
    }

    private boolean sameSourceGroup(String left, String right) {
        String normalizedLeft = normalizeSource(left);
        String normalizedRight = normalizeSource(right);
        return Objects.equals(normalizedLeft, normalizedRight)
                || Objects.equals(ownerSource(normalizedLeft), ownerSource(normalizedRight));
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
