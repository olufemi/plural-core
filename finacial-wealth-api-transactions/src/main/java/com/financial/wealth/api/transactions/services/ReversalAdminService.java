package com.financial.wealth.api.transactions.services;

import com.financial.wealth.api.transactions.domain.SuccessDebitLog;
import com.financial.wealth.api.transactions.models.ApiResponseModel;
import com.financial.wealth.api.transactions.repo.SuccessDebitLogRepo;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ReversalAdminService {

    private static final List<String> REVERSAL_STATUSES = Arrays.asList("PENDING", "FAILED", "SUCCESS");

    private final SuccessDebitLogRepo successDebitLogRepo;

    public ReversalAdminService(SuccessDebitLogRepo successDebitLogRepo) {
        this.successDebitLogRepo = successDebitLogRepo;
    }

    public ApiResponseModel getSummary() {
        List<Map<String, Object>> grouped = buildGroupedCases(null);
        long success = grouped.stream().filter(m -> "SUCCESS".equals(m.get("status"))).count();
        long pending = grouped.stream().filter(m -> "PENDING".equals(m.get("status"))).count();
        long failed = grouped.stream().filter(m -> "FAILED".equals(m.get("status"))).count();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalCount", grouped.size());
        summary.put("successfulCount", success);
        summary.put("pendingCount", pending);
        summary.put("failedCount", failed);

        ApiResponseModel response = new ApiResponseModel();
        response.setStatusCode(200);
        response.setDescription("Reversal summary fetched successfully");
        response.setData(summary);
        return response;
    }

    public ApiResponseModel getCases(String status) {
        List<Map<String, Object>> grouped = buildGroupedCases(status);
        ApiResponseModel response = new ApiResponseModel();
        response.setStatusCode(200);
        response.setDescription("Reversal cases fetched successfully");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("count", grouped.size());
        payload.put("items", grouped);
        response.setData(payload);
        return response;
    }

    private List<Map<String, Object>> buildGroupedCases(String statusFilter) {
        Collection<String> statusesToLoad = REVERSAL_STATUSES;
        if (statusFilter != null && !statusFilter.trim().isEmpty()) {
            statusesToLoad = Collections.singletonList(statusFilter.toUpperCase());
        }

        List<SuccessDebitLog> logs = successDebitLogRepo.findByReversalStatusIn(statusesToLoad);
        Map<String, List<SuccessDebitLog>> grouped = logs.stream()
                .collect(Collectors.groupingBy(log -> rootTransactionId(log.getTransactionId()), LinkedHashMap::new, Collectors.toList()));

        List<Map<String, Object>> items = new ArrayList<>();
        for (Map.Entry<String, List<SuccessDebitLog>> entry : grouped.entrySet()) {
            String overallStatus = deriveOverallStatus(entry.getValue());
            if (statusFilter != null && !statusFilter.trim().isEmpty() && !overallStatus.equalsIgnoreCase(statusFilter)) {
                continue;
            }
            items.add(toCaseRecord(entry.getKey(), overallStatus, entry.getValue()));
        }

        items.sort((a, b) -> {
            Instant ai = (Instant) a.get("requestedAt");
            Instant bi = (Instant) b.get("requestedAt");
            if (ai == null && bi == null) return 0;
            if (ai == null) return 1;
            if (bi == null) return -1;
            return bi.compareTo(ai);
        });
        return items;
    }

    private Map<String, Object> toCaseRecord(String rootTransactionId, String overallStatus, List<SuccessDebitLog> logs) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("transactionId", rootTransactionId);
        item.put("status", overallStatus);
        item.put("requestedAt", logs.stream().map(SuccessDebitLog::getReversalRequestedAt).filter(Objects::nonNull).min(Instant::compareTo).orElse(null));
        item.put("completedAt", logs.stream().map(SuccessDebitLog::getReversalCompletedAt).filter(Objects::nonNull).max(Instant::compareTo).orElse(null));
        item.put("retryCount", logs.stream().mapToInt(SuccessDebitLog::getRetryCount).max().orElse(0));
        item.put("lastError", logs.stream().map(SuccessDebitLog::getReversalLastError).filter(Objects::nonNull).filter(s -> !s.trim().isEmpty()).findFirst().orElse(null));
        item.put("legs", logs.stream().map(this::toLegRecord).collect(Collectors.toList()));
        return item;
    }

    private Map<String, Object> toLegRecord(SuccessDebitLog log) {
        Map<String, Object> leg = new LinkedHashMap<>();
        leg.put("transactionId", log.getTransactionId());
        leg.put("payloadType", log.getPayloadType());
        leg.put("status", log.getReversalStatus());
        leg.put("retryCount", log.getRetryCount());
        leg.put("requestedAt", log.getReversalRequestedAt());
        leg.put("completedAt", log.getReversalCompletedAt());
        leg.put("lastError", log.getReversalLastError());
        return leg;
    }

    private String deriveOverallStatus(List<SuccessDebitLog> logs) {
        LinkedHashSet<String> statuses = logs.stream()
                .map(SuccessDebitLog::getReversalStatus)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (statuses.contains("PENDING")) {
            return "PENDING";
        }
        if (statuses.contains("FAILED")) {
            return "FAILED";
        }
        if (!statuses.isEmpty() && Collections.singleton("SUCCESS").equals(statuses)) {
            return "SUCCESS";
        }
        return "PENDING";
    }

    private String rootTransactionId(String transactionId) {
        if (transactionId == null) {
            return "UNKNOWN";
        }
        if (transactionId.endsWith("-NGN_GL")) {
            return transactionId.substring(0, transactionId.length() - "-NGN_GL".length());
        }
        return transactionId;
    }
}
