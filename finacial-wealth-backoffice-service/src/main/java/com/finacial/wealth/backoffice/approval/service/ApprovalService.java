package com.finacial.wealth.backoffice.approval.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finacial.wealth.backoffice.approval.dto.ApprovalDecisionRequest;
import com.finacial.wealth.backoffice.approval.dto.ApprovalResubmitRequest;
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
import com.finacial.wealth.backoffice.integrations.fxpeer.model.LiquidationApprovalRequest;
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
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApprovalService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final BoApprovalRequestRepository approvalRequestRepository;
    private final BoApprovalEventRepository approvalEventRepository;
    private final FxPeerExchangeClient fxPeerExchangeClient;
    private final TransactionsClient transactionsClient;
    private final ObjectMapper objectMapper;
    private final AdminAuditService adminAuditService;

    @Transactional
    public Map<String, Object> listApprovals(String status, Integer page, Integer size) {
        syncPendingLiquidations();

        List<ApprovalStatus> statuses = resolveStatuses(status);
        List<BoApprovalRequest> approvals = new ArrayList<>(approvalRequestRepository.findByStatusIn(statuses));
        approvals.sort(Comparator.comparing(BoApprovalRequest::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));

        int safePage = safePage(page);
        int safeSize = safeSize(size);
        int from = Math.min(safePage * safeSize, approvals.size());
        int to = Math.min(from + safeSize, approvals.size());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("content", approvals.subList(from, to).stream().map(this::toApprovalRow).toList());
        data.put("page", safePage);
        data.put("size", safeSize);
        data.put("totalElements", approvals.size());
        data.put("totalPages", approvals.isEmpty() ? 0 : (int) Math.ceil((double) approvals.size() / safeSize));
        return data;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getApproval(Long id) {
        BoApprovalRequest approval = approvalRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Approval request not found"));

        Map<String, Object> response = toApprovalRow(approval);
        response.put("events", approvalEventRepository.findByApprovalRequestIdOrderByCreatedAtDesc(id)
                .stream()
                .map(this::toEventRow)
                .toList());
        return response;
    }

    @Transactional
    public Map<String, Object> approve(Long approvalId, Long actorAdminId, HttpServletRequest request) {
        BoApprovalRequest approval = getApprovalForDecision(approvalId, actorAdminId);

        Map<String, Object> executionResponse = switch (approval.getEntityType()) {
            case FXPEER_LIQUIDATION -> approveLiquidation(approval);
            case FXPEER_AIRTIME_REVERSAL -> approveFxpeerAirtimeReversal(approval, request.getHeader("Authorization"));
            case TRANSACTIONS_REVERSAL -> approveTransactionsReversal(approval);
        };

        approval.setStatus(ApprovalStatus.APPROVED);
        approval.setCheckerAdminId(actorAdminId);
        approval.setApprovedAt(Instant.now());
        approvalRequestRepository.save(approval);

        createEvent(approval, ApprovalEventType.APPROVED, actorAdminId, "Approval completed", executionResponse);
        audit(request, actorAdminId, approval.getId(), "APPROVAL_APPROVE", Map.of(
                "entityRef", approval.getEntityRef(),
                "entityType", approval.getEntityType().name()
        ));
        return toApprovalRow(approval);
    }

    @Transactional
    public Map<String, Object> reject(Long approvalId, Long actorAdminId, ApprovalDecisionRequest decision, HttpServletRequest request) {
        BoApprovalRequest approval = getApprovalForDecision(approvalId, actorAdminId);

        approval.setStatus(ApprovalStatus.IN_REMEDIATION);
        approval.setCheckerAdminId(actorAdminId);
        approval.setRejectedAt(Instant.now());
        approval.setRejectionReason(trimToNull(decision.reason()));
        approvalRequestRepository.save(approval);

        createEvent(approval, ApprovalEventType.REJECTED, actorAdminId, trimToNull(decision.reason()), Map.of());
        audit(request, actorAdminId, approval.getId(), "APPROVAL_REJECT", Map.of(
                "entityRef", approval.getEntityRef(),
                "entityType", approval.getEntityType() == null ? null : approval.getEntityType().name(),
                "reason", trimToNull(decision.reason())
        ));
        return toApprovalRow(approval);
    }

    @Transactional
    public Map<String, Object> resubmit(Long approvalId, Long actorAdminId, ApprovalResubmitRequest request, HttpServletRequest httpRequest) {
        BoApprovalRequest approval = approvalRequestRepository.findById(approvalId)
                .orElseThrow(() -> new IllegalArgumentException("Approval request not found"));

        if (approval.getStatus() != ApprovalStatus.IN_REMEDIATION) {
            throw new IllegalArgumentException("Only approvals in remediation can be resubmitted");
        }
        if (approval.getCheckerAdminId() != null && approval.getCheckerAdminId().equals(actorAdminId)) {
            throw new IllegalArgumentException("Checker cannot resubmit the same approval they rejected");
        }

        approval.setStatus(ApprovalStatus.RESUBMITTED);
        approval.setMakerAdminId(actorAdminId);
        approval.setSubmittedAt(approval.getSubmittedAt() == null ? Instant.now() : approval.getSubmittedAt());
        approval.setResubmittedAt(Instant.now());
        approval.setRemediationNotes(trimToNull(request.notes()));
        approval.setRejectionReason(null);
        approvalRequestRepository.save(approval);

        createEvent(approval, ApprovalEventType.RESUBMITTED, actorAdminId, trimToNull(request.notes()), Map.of());
        audit(httpRequest, actorAdminId, approval.getId(), "APPROVAL_RESUBMIT", Map.of(
                "entityRef", approval.getEntityRef(),
                "entityType", approval.getEntityType() == null ? null : approval.getEntityType().name(),
                "notes", trimToNull(request.notes())
        ));
        return toApprovalRow(approval);
    }

    private Map<String, Object> approveLiquidation(BoApprovalRequest approval) {
        LiquidationApprovalRequest liquidationApprovalRequest = new LiquidationApprovalRequest();
        liquidationApprovalRequest.setOrderRef(approval.getEntityRef());
        return fxPeerExchangeClient.approveLiquidation(liquidationApprovalRequest);
    }

    private Map<String, Object> approveFxpeerAirtimeReversal(BoApprovalRequest approval, String auth) {
        String caseRef = extractCaseRef(approval);
        return fxPeerExchangeClient.retryAirtimeReversal(auth, caseRef);
    }

    private Map<String, Object> approveTransactionsReversal(BoApprovalRequest approval) {
        String caseRef = extractCaseRef(approval);
        return transactionsClient.retryReversal(caseRef);
    }

    private String extractCaseRef(BoApprovalRequest approval) {
        Map<String, Object> payload = readJsonMap(approval.getPayloadJson());
        String caseRef = stringValue(payload.get("caseRef"));
        if (caseRef != null) {
            return caseRef;
        }
        String entityRef = stringValue(approval.getEntityRef());
        if (entityRef != null && entityRef.contains("::")) {
            return entityRef.substring(0, entityRef.indexOf("::"));
        }
        return entityRef;
    }

    private void syncPendingLiquidations() {
        Map<String, Object> response = fxPeerExchangeClient.getAdminLiquidations(
                "LIQUIDATION_PENDING_APPROVAL,LIQUIDATION_PROCESSING",
                null,
                null,
                null,
                0,
                500
        );

        for (Map<String, Object> row : extractContent(response)) {
            String orderRef = stringValue(row.get("orderRef"));
            if (orderRef == null) {
                continue;
            }

            Optional<BoApprovalRequest> existing = approvalRequestRepository.findByEntityTypeAndEntityRef(
                    ApprovalEntityType.FXPEER_LIQUIDATION,
                    orderRef
            );
            if (existing.isPresent()) {
                continue;
            }

            BoApprovalRequest created = approvalRequestRepository.save(
                    BoApprovalRequest.builder()
                            .module(ApprovalModule.INVESTMENT)
                            .subModule(ApprovalSubModule.LIQUIDATION)
                            .entityType(ApprovalEntityType.FXPEER_LIQUIDATION)
                            .entityRef(orderRef)
                            .actionType(ApprovalActionType.APPROVE)
                            .status(ApprovalStatus.PENDING)
                            .requesterEmail(stringValue(row.get("requester")))
                            .payloadJson(writeJson(row))
                            .submittedAt(Instant.now())
                            .build()
            );
            createEvent(created, ApprovalEventType.SYNCED, null, "Synced from exchange liquidation queue", row);
        }
    }

    private BoApprovalRequest getApprovalForDecision(Long approvalId, Long actorAdminId) {
        BoApprovalRequest approval = approvalRequestRepository.findById(approvalId)
                .orElseThrow(() -> new IllegalArgumentException("Approval request not found"));

        if (!(approval.getStatus() == ApprovalStatus.PENDING || approval.getStatus() == ApprovalStatus.RESUBMITTED)) {
            throw new IllegalArgumentException("Approval is not awaiting decision");
        }
        if (approval.getMakerAdminId() != null && approval.getMakerAdminId().equals(actorAdminId)) {
            throw new IllegalArgumentException("Maker cannot approve or reject their own request");
        }
        return approval;
    }

    private List<ApprovalStatus> resolveStatuses(String status) {
        if (status == null || status.isBlank()) {
            return List.of(ApprovalStatus.PENDING, ApprovalStatus.IN_REMEDIATION, ApprovalStatus.RESUBMITTED);
        }

        List<ApprovalStatus> statuses = new ArrayList<>();
        for (String value : status.split(",")) {
            statuses.add(ApprovalStatus.valueOf(value.trim().toUpperCase(Locale.ROOT)));
        }
        return statuses;
    }

    private List<Map<String, Object>> extractContent(Map<String, Object> response) {
        List<Map<String, Object>> topLevelRows = extractRowList(response, "content", "items");
        if (!topLevelRows.isEmpty()) {
            return topLevelRows;
        }

        Object data = response == null ? null : response.get("data");
        if (data instanceof List<?> list) {
            return toRowList(list);
        }
        if (data instanceof Map<?, ?> map) {
            return extractRowList(map, "content", "items", "data");
        }

        return Collections.emptyList();
    }

    private List<Map<String, Object>> extractRowList(Map<?, ?> source, String... keys) {
        if (source == null) {
            return Collections.emptyList();
        }
        for (String key : keys) {
            Object value = source.get(key);
            if (value instanceof List<?> list) {
                return toRowList(list);
            }
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> toRowList(List<?> list) {
        if (list.isEmpty() || list.stream().allMatch(Map.class::isInstance)) {
            return (List<Map<String, Object>>) list;
        }
        return Collections.emptyList();
    }

    private Map<String, Object> toApprovalRow(BoApprovalRequest approval) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", approval.getId());
        item.put("module", approval.getModule() != null ? approval.getModule().name() : null);
        item.put("subModule", approval.getSubModule() != null ? approval.getSubModule().name() : null);
        item.put("entityType", approval.getEntityType() != null ? approval.getEntityType().name() : null);
        item.put("entityRef", approval.getEntityRef());
        item.put("actionType", approval.getActionType() != null ? approval.getActionType().name() : null);
        item.put("status", approval.getStatus() != null ? approval.getStatus().name() : null);
        item.put("makerAdminId", approval.getMakerAdminId());
        item.put("checkerAdminId", approval.getCheckerAdminId());
        item.put("requesterEmail", approval.getRequesterEmail());
        item.put("rejectionReason", approval.getRejectionReason());
        item.put("remediationNotes", approval.getRemediationNotes());
        item.put("createdAt", approval.getCreatedAt());
        item.put("updatedAt", approval.getUpdatedAt());
        item.put("submittedAt", approval.getSubmittedAt());
        item.put("approvedAt", approval.getApprovedAt());
        item.put("rejectedAt", approval.getRejectedAt());
        item.put("resubmittedAt", approval.getResubmittedAt());
        item.put("payload", readJsonMap(approval.getPayloadJson()));
        return item;
    }

    private Map<String, Object> toEventRow(BoApprovalEvent event) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", event.getId());
        item.put("approvalRequestId", event.getApprovalRequestId());
        item.put("eventType", event.getEventType() != null ? event.getEventType().name() : null);
        item.put("actorAdminId", event.getActorAdminId());
        item.put("notes", event.getNotes());
        item.put("metadata", readJsonMap(event.getMetadataJson()));
        item.put("createdAt", event.getCreatedAt());
        return item;
    }

    private void createEvent(BoApprovalRequest approval, ApprovalEventType eventType, Long actorAdminId, String notes, Object metadata) {
        approvalEventRepository.save(BoApprovalEvent.builder()
                .approvalRequestId(approval.getId())
                .eventType(eventType)
                .actorAdminId(actorAdminId)
                .notes(notes)
                .metadataJson(writeJson(metadata))
                .build());
    }

    private void audit(HttpServletRequest request, Long actorAdminId, Long approvalId, String action, Map<String, Object> meta) {
        adminAuditService.audit(
                action,
                actorAdminId,
                "BoApprovalRequest",
                approvalId,
                request.getRemoteAddr(),
                request.getHeader("User-Agent"),
                meta
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

    private String stringValue(Object value) {
        if (value == null) {
            return null;
        }
        String stringValue = String.valueOf(value).trim();
        return stringValue.isEmpty() ? null : stringValue;
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private int safePage(Integer page) {
        return page == null ? 0 : Math.max(page, 0);
    }

    private int safeSize(Integer size) {
        return size == null ? 20 : Math.max(size, 1);
    }
}
