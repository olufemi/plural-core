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
import com.finacial.wealth.backoffice.configmanagement.dto.AppConfigUpdateRequest;
import com.finacial.wealth.backoffice.configmanagement.service.AppConfigManagementService;
import com.finacial.wealth.backoffice.campaign.model.ApproveCampaignRequest;
import com.finacial.wealth.backoffice.campaign.model.CreateCampaignRequest;
import com.finacial.wealth.backoffice.campaign.model.UpdateCampaignRequest;
import com.finacial.wealth.backoffice.integrations.fxpeer.FxPeerExchangeClient;
import com.finacial.wealth.backoffice.integrations.fxpeer.model.InvestmentProductUpsertRequest;
import com.finacial.wealth.backoffice.integrations.fxpeer.model.LiquidationApprovalRequest;
import com.finacial.wealth.backoffice.integrations.profiling.BackofficeCustomerService;
import com.finacial.wealth.backoffice.integrations.profiling.CampaignManagementService;
import com.finacial.wealth.backoffice.integrations.profiling.ReferralProgramManagementService;
import com.finacial.wealth.backoffice.integrations.transactions.TransactionsClient;
import com.finacial.wealth.backoffice.model.ApiResponseModel;
import com.finacial.wealth.backoffice.model.BlockUserRequest;
import com.finacial.wealth.backoffice.notification.entity.BackofficeNotificationSeverity;
import com.finacial.wealth.backoffice.notification.service.BackofficeNotificationService;
import com.finacial.wealth.backoffice.referral.model.CreateReferralProgramRequest;
import com.finacial.wealth.backoffice.referral.model.UpdateReferralProgramRequest;
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
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    private final AppConfigManagementService appConfigManagementService;
    private final BackofficeCustomerService backofficeCustomerService;
    private final ReferralProgramManagementService referralProgramManagementService;
    private final CampaignManagementService campaignManagementService;
    private final ObjectMapper objectMapper;
    private final AdminAuditService adminAuditService;
    private final BackofficeNotificationService notificationService;

    @Value("${bo.downstream.internal-token:}")
    private String downstreamInternalToken;

    @Transactional
    public Map<String, Object> submitInvestmentProductCreate(InvestmentProductUpsertRequest payload,
            Long actorAdminId, HttpServletRequest request) {
        String productCode = requireText(payload == null ? null : payload.getProductCode(), "productCode");
        Map<String, Object> approvalPayload = approvalPayload(
                "INVESTMENT_PRODUCT_CREATE",
                productCode,
                payload);
        approvalPayload.put("beforeSnapshot", null);
        approvalPayload.put("afterSnapshot", payload);
        approvalPayload.put("snapshotSource", "BACKOFFICE_REQUEST");
        BoApprovalRequest approval = approvalRequestRepository.save(
                BoApprovalRequest.builder()
                        .module(ApprovalModule.INVESTMENT)
                        .subModule(ApprovalSubModule.PRODUCT)
                        .entityType(ApprovalEntityType.FXPEER_INVESTMENT_PRODUCT)
                        .entityRef(uniqueEntityRef("INVESTMENT_PRODUCT_CREATE", productCode))
                        .actionType(ApprovalActionType.CREATE)
                        .status(ApprovalStatus.PENDING)
                        .makerAdminId(actorAdminId)
                        .payloadJson(writeJson(approvalPayload))
                        .submittedAt(Instant.now())
                        .build()
        );
        createEvent(approval, ApprovalEventType.REQUESTED, actorAdminId,
                "Investment product create submitted for approval", approvalPayload);
        notifyApprovalPending(approval, "Investment product approval pending",
                "An investment product create request is waiting for checker approval.");
        Map<String, Object> auditMeta = new LinkedHashMap<>();
        auditMeta.put("productCode", productCode);
        auditMeta.put("approvalId", approval.getId());
        auditMeta.put("beforeSnapshot", null);
        auditMeta.put("afterSnapshot", payload);
        audit(request, actorAdminId, approval.getId(), "INVESTMENT_PRODUCT_CREATE_APPROVAL_REQUESTED", auditMeta);
        return toApprovalSubmittedResponse(approval);
    }

    @Transactional
    public Map<String, Object> submitInvestmentProductUpdate(String productCode, InvestmentProductUpsertRequest payload,
            Long actorAdminId, HttpServletRequest request) {
        String normalizedProductCode = requireText(productCode, "productCode");
        payload.setProductCode(normalizedProductCode);
        Map<String, Object> beforeSnapshot = fetchCurrentInvestmentProductSnapshot(
                request == null ? null : request.getHeader("Authorization"),
                normalizedProductCode
        );
        Map<String, Object> approvalPayload = approvalPayload(
                "INVESTMENT_PRODUCT_UPDATE",
                normalizedProductCode,
                payload);
        approvalPayload.put("beforeSnapshot", beforeSnapshot);
        approvalPayload.put("afterSnapshot", payload);
        approvalPayload.put("snapshotSource", beforeSnapshot == null ? "BACKOFFICE_REQUEST_ONLY" : "FXPEER_CURRENT_PRODUCT");
        BoApprovalRequest approval = approvalRequestRepository.save(
                BoApprovalRequest.builder()
                        .module(ApprovalModule.INVESTMENT)
                        .subModule(ApprovalSubModule.PRODUCT)
                        .entityType(ApprovalEntityType.FXPEER_INVESTMENT_PRODUCT)
                        .entityRef(uniqueEntityRef("INVESTMENT_PRODUCT_UPDATE", normalizedProductCode))
                        .actionType(ApprovalActionType.UPDATE)
                        .status(ApprovalStatus.PENDING)
                        .makerAdminId(actorAdminId)
                        .payloadJson(writeJson(approvalPayload))
                        .submittedAt(Instant.now())
                        .build()
        );
        createEvent(approval, ApprovalEventType.REQUESTED, actorAdminId,
                "Investment product update submitted for approval", approvalPayload);
        notifyApprovalPending(approval, "Investment product approval pending",
                "An investment product update request is waiting for checker approval.");
        Map<String, Object> auditMeta = new LinkedHashMap<>();
        auditMeta.put("productCode", normalizedProductCode);
        auditMeta.put("approvalId", approval.getId());
        auditMeta.put("beforeSnapshot", beforeSnapshot);
        auditMeta.put("afterSnapshot", payload);
        audit(request, actorAdminId, approval.getId(), "INVESTMENT_PRODUCT_UPDATE_APPROVAL_REQUESTED", auditMeta);
        return toApprovalSubmittedResponse(approval);
    }

    @Transactional
    public Map<String, Object> submitAppConfigUpdate(String configName, AppConfigUpdateRequest payload,
            Long actorAdminId, HttpServletRequest request) {
        String normalizedConfigName = requireText(configName, "configName");
        Map<String, Object> approvalPayload = approvalPayload(
                "APP_CONFIG_UPDATE",
                normalizedConfigName,
                payload);
        BoApprovalRequest approval = approvalRequestRepository.save(
                BoApprovalRequest.builder()
                        .module(ApprovalModule.CONFIG)
                        .subModule(ApprovalSubModule.APP_CONFIG)
                        .entityType(ApprovalEntityType.APP_CONFIG)
                        .entityRef(uniqueEntityRef("APP_CONFIG_UPDATE", normalizedConfigName))
                        .actionType(ApprovalActionType.UPDATE)
                        .status(ApprovalStatus.PENDING)
                        .makerAdminId(actorAdminId)
                        .payloadJson(writeJson(approvalPayload))
                        .submittedAt(Instant.now())
                        .build()
        );
        createEvent(approval, ApprovalEventType.REQUESTED, actorAdminId,
                "App config update submitted for approval", approvalPayload);
        notifyApprovalPending(approval, "App config approval pending",
                "An app_config change is waiting for checker approval.");
        audit(request, actorAdminId, approval.getId(), "APP_CONFIG_UPDATE_APPROVAL_REQUESTED", Map.of(
                "configName", normalizedConfigName,
                "approvalId", approval.getId()
        ));
        return toApprovalSubmittedResponse(approval);
    }

    @Transactional
    public Map<String, Object> submitReferralProgramCreate(CreateReferralProgramRequest payload, String userId,
            Long actorAdminId, HttpServletRequest request) {
        String targetRef = payload == null || payload.getProgramCode() == null || payload.getProgramCode().isBlank()
                ? "NEW"
                : payload.getProgramCode().trim();
        return submitReferralApproval("CREATE", targetRef, payload, userId, actorAdminId, request);
    }

    @Transactional
    public Map<String, Object> submitReferralProgramUpdate(Long id, UpdateReferralProgramRequest payload, String userId,
            Long actorAdminId, HttpServletRequest request) {
        return submitReferralApproval("UPDATE", requireText(id == null ? null : String.valueOf(id), "id"),
                payload, userId, actorAdminId, request);
    }

    @Transactional
    public Map<String, Object> submitReferralProgramStatus(Long id, String operation, String userId,
            Long actorAdminId, HttpServletRequest request) {
        return submitReferralApproval(operation, requireText(id == null ? null : String.valueOf(id), "id"),
                null, userId, actorAdminId, request);
    }

    @Transactional
    public Map<String, Object> submitCampaignCreate(CreateCampaignRequest payload,
            Long actorAdminId, HttpServletRequest request) {
        String targetRef = payload == null || payload.getTitle() == null || payload.getTitle().isBlank()
                ? "NEW"
                : payload.getTitle().trim();
        return submitCampaignApproval("CREATE", targetRef, payload, actorAdminId, request);
    }

    @Transactional
    public Map<String, Object> submitCampaignUpdate(Long id, UpdateCampaignRequest payload,
            Long actorAdminId, HttpServletRequest request) {
        return submitCampaignApproval("UPDATE", requireText(id == null ? null : String.valueOf(id), "id"),
                payload, actorAdminId, request);
    }

    @Transactional
    public Map<String, Object> submitCampaignApprove(Long id, ApproveCampaignRequest payload,
            Long actorAdminId, HttpServletRequest request) {
        return submitCampaignApproval("APPROVE", requireText(id == null ? null : String.valueOf(id), "id"),
                payload, actorAdminId, request);
    }

    @Transactional
    public Map<String, Object> submitCampaignStatus(Long id, String operation,
            Long actorAdminId, HttpServletRequest request) {
        return submitCampaignApproval(operation, requireText(id == null ? null : String.valueOf(id), "id"),
                null, actorAdminId, request);
    }

    @Transactional
    public Map<String, Object> submitCustomerBlock(Long id, BlockUserRequest payload,
            Long actorAdminId, HttpServletRequest request) {
        return submitCustomerApproval("BLOCK", id, payload, actorAdminId, request);
    }

    @Transactional
    public Map<String, Object> submitCustomerUnblock(Long id, BlockUserRequest payload,
            Long actorAdminId, HttpServletRequest request) {
        return submitCustomerApproval("UNBLOCK", id, payload, actorAdminId, request);
    }

    @Transactional
    public Map<String, Object> listApprovals(String status, String module, String subModule, String actionType, String entityRef, Integer page, Integer size) {
        syncPendingLiquidations();

        List<ApprovalStatus> statuses = resolveStatuses(status);
        String moduleFilter = trimToNull(module);
        String subModuleFilter = trimToNull(subModule);
        String actionTypeFilter = trimToNull(actionType);
        String entityRefFilter = trimToNull(entityRef);

        List<BoApprovalRequest> approvals = new ArrayList<>(approvalRequestRepository.findByStatusIn(statuses));
        approvals = approvals.stream()
                .filter(item -> enumNameMatches(item.getModule(), moduleFilter))
                .filter(item -> enumNameMatches(item.getSubModule(), subModuleFilter))
                .filter(item -> enumNameMatches(item.getActionType(), actionTypeFilter))
                .filter(item -> entityRefFilter == null || (item.getEntityRef() != null && item.getEntityRef().toLowerCase(Locale.ROOT).contains(entityRefFilter.toLowerCase(Locale.ROOT))))
                .sorted(Comparator.comparing(BoApprovalRequest::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

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
        data.put("filters", Map.of(
                "status", status == null ? "" : status,
                "module", moduleFilter == null ? "" : moduleFilter,
                "subModule", subModuleFilter == null ? "" : subModuleFilter,
                "actionType", actionTypeFilter == null ? "" : actionTypeFilter,
                "entityRef", entityRefFilter == null ? "" : entityRefFilter
        ));
        return data;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> findByEntityRef(String entityRef) {
        String normalizedEntityRef = requireText(entityRef, "entityRef");
        List<Map<String, Object>> items = approvalRequestRepository.findAll().stream()
                .filter(item -> item.getEntityRef() != null && item.getEntityRef().toLowerCase(Locale.ROOT).contains(normalizedEntityRef.toLowerCase(Locale.ROOT)))
                .sorted(Comparator.comparing(BoApprovalRequest::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toApprovalRow)
                .toList();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("entityRef", normalizedEntityRef);
        response.put("content", items);
        response.put("totalElements", items.size());
        return response;
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

    @Transactional(readOnly = true)
    public Map<String, Object> getInvestmentProductApprovalHistory(String productCode) {
        String normalizedProductCode = requireText(productCode, "productCode");
        List<Map<String, Object>> items = approvalRequestRepository
                .findTop100ByEntityTypeAndEntityRefContainingOrderByCreatedAtDesc(
                        ApprovalEntityType.FXPEER_INVESTMENT_PRODUCT,
                        normalizedProductCode
                )
                .stream()
                .filter(approval -> normalizedProductCode.equalsIgnoreCase(
                        stringValue(readJsonMap(approval.getPayloadJson()).get("targetRef"))
                ))
                .map(this::toProductApprovalHistoryRow)
                .toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("productCode", normalizedProductCode);
        response.put("configurationAuditAvailable", !items.isEmpty());
        response.put("items", items);
        return response;
    }

    @Transactional
    public Map<String, Object> approve(Long approvalId, Long actorAdminId, HttpServletRequest request) {
        BoApprovalRequest approval = getApprovalForDecision(approvalId, actorAdminId);

        Map<String, Object> executionResponse = switch (approval.getEntityType()) {
            case FXPEER_LIQUIDATION -> approveLiquidation(approval);
            case FXPEER_AIRTIME_REVERSAL -> approveFxpeerAirtimeReversal(approval, request.getHeader("Authorization"));
            case TRANSACTIONS_REVERSAL -> approveTransactionsReversal(approval);
            case FXPEER_INVESTMENT_PRODUCT -> approveInvestmentProduct(approval);
            case APP_CONFIG -> approveAppConfigUpdate(approval, actorAdminId, request);
            case REFERRAL_PROGRAM -> approveReferralProgramChange(approval);
            case CAMPAIGN -> approveCampaignChange(approval);
            case CUSTOMER_PROFILE -> approveCustomerProfileChange(approval);
        };

        approval.setStatus(ApprovalStatus.APPROVED);
        approval.setCheckerAdminId(actorAdminId);
        approval.setApprovedAt(Instant.now());
        approvalRequestRepository.save(approval);

        createEvent(approval, ApprovalEventType.APPROVED, actorAdminId, "Approval completed", executionResponse);
        notifyApprovalDecision(approval, "Approval completed", BackofficeNotificationSeverity.INFO, "approved");
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
        notifyApprovalDecision(approval, "Approval needs remediation", BackofficeNotificationSeverity.WARNING, "rejected");
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
        notifyApprovalPending(approval, "Approval resubmitted", "A backoffice approval item was resubmitted and needs review.");
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
        return fxPeerExchangeClient.retryAirtimeReversal(auth, requireDownstreamInternalToken(), caseRef);
    }

    private Map<String, Object> approveTransactionsReversal(BoApprovalRequest approval) {
        String caseRef = extractCaseRef(approval);
        return transactionsClient.retryReversal(requireDownstreamInternalToken(), caseRef);
    }

    private String requireDownstreamInternalToken() {
        if (downstreamInternalToken == null || downstreamInternalToken.trim().isEmpty()) {
            throw new IllegalStateException("bo.downstream.internal-token is required for approved reversal execution");
        }
        return downstreamInternalToken;
    }

    private Map<String, Object> approveInvestmentProduct(BoApprovalRequest approval) {
        Map<String, Object> payload = readJsonMap(approval.getPayloadJson());
        String actionCode = stringValue(payload.get("actionCode"));
        String productCode = requireText(stringValue(payload.get("targetRef")), "productCode");
        InvestmentProductUpsertRequest request = objectMapper.convertValue(
                payload.get("request"),
                InvestmentProductUpsertRequest.class);
        request.setProductCode(productCode);
        if ("INVESTMENT_PRODUCT_CREATE".equals(actionCode)) {
            return fxPeerExchangeClient.createInvestmentProduct(request);
        }
        return fxPeerExchangeClient.updateInvestmentProduct(productCode, request);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchCurrentInvestmentProductSnapshot(String auth, String productCode) {
        if (productCode == null || productCode.isBlank()) {
            return null;
        }
        try {
            Map<String, Object> response = fxPeerExchangeClient.getInvestmentProduct(productCode);
            Object statusCode = response == null ? null : response.get("statusCode");
            if (statusCode != null && !"200".equals(String.valueOf(statusCode))) {
                return null;
            }
            Object data = response == null ? null : response.get("data");
            if (data instanceof Map<?, ?> map) {
                return (Map<String, Object>) map;
            }
        } catch (RuntimeException ignored) {
            // Snapshot is helpful for audit, but should not block submission for approval.
        }
        return null;
    }

    private Map<String, Object> toProductApprovalHistoryRow(BoApprovalRequest approval) {
        Map<String, Object> payload = readJsonMap(approval.getPayloadJson());
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("approvalId", approval.getId());
        row.put("actionCode", payload.get("actionCode"));
        row.put("actionType", approval.getActionType() == null ? null : approval.getActionType().name());
        row.put("status", approval.getStatus() == null ? null : approval.getStatus().name());
        row.put("makerAdminId", approval.getMakerAdminId());
        row.put("checkerAdminId", approval.getCheckerAdminId());
        row.put("submittedAt", approval.getSubmittedAt());
        row.put("approvedAt", approval.getApprovedAt());
        row.put("rejectedAt", approval.getRejectedAt());
        row.put("resubmittedAt", approval.getResubmittedAt());
        row.put("beforeSnapshot", payload.get("beforeSnapshot"));
        row.put("afterSnapshot", payload.get("afterSnapshot"));
        row.put("snapshotSource", payload.get("snapshotSource"));
        row.put("rejectionReason", approval.getRejectionReason());
        row.put("remediationNotes", approval.getRemediationNotes());
        return row;
    }

    private Map<String, Object> approveAppConfigUpdate(BoApprovalRequest approval,
            Long actorAdminId, HttpServletRequest httpRequest) {
        Map<String, Object> payload = readJsonMap(approval.getPayloadJson());
        String configName = requireText(stringValue(payload.get("targetRef")), "configName");
        AppConfigUpdateRequest request = objectMapper.convertValue(payload.get("request"), AppConfigUpdateRequest.class);
        Object response = appConfigManagementService.update(configName, request, actorAdminId, httpRequest);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("configName", configName);
        result.put("response", response);
        return result;
    }

    private Map<String, Object> approveReferralProgramChange(BoApprovalRequest approval) {
        Map<String, Object> payload = readJsonMap(approval.getPayloadJson());
        String operation = requireText(stringValue(payload.get("operation")), "operation");
        String userId = stringValue(payload.get("userId"));
        Object response;
        switch (operation) {
            case "CREATE" -> response = referralProgramManagementService.createReferralProgram(
                    objectMapper.convertValue(payload.get("request"), CreateReferralProgramRequest.class), userId);
            case "UPDATE" -> response = referralProgramManagementService.updateReferralProgram(
                    Long.valueOf(requireText(stringValue(payload.get("targetRef")), "id")),
                    objectMapper.convertValue(payload.get("request"), UpdateReferralProgramRequest.class), userId);
            case "ACTIVATE" -> response = referralProgramManagementService.activateReferralProgram(
                    Long.valueOf(requireText(stringValue(payload.get("targetRef")), "id")), userId);
            case "PAUSE" -> response = referralProgramManagementService.pauseReferralProgram(
                    Long.valueOf(requireText(stringValue(payload.get("targetRef")), "id")), userId);
            case "END" -> response = referralProgramManagementService.endReferralProgram(
                    Long.valueOf(requireText(stringValue(payload.get("targetRef")), "id")), userId);
            default -> throw new IllegalArgumentException("Unsupported referral approval operation");
        }
        return approvalExecutionResponse(operation, payload.get("targetRef"), response);
    }

    private Map<String, Object> approveCampaignChange(BoApprovalRequest approval) {
        Map<String, Object> payload = readJsonMap(approval.getPayloadJson());
        String operation = requireText(stringValue(payload.get("operation")), "operation");
        Object response;
        switch (operation) {
            case "CREATE" -> response = campaignManagementService.createCampaign(
                    objectMapper.convertValue(payload.get("request"), CreateCampaignRequest.class));
            case "UPDATE" -> response = campaignManagementService.updateCampaign(
                    Long.valueOf(requireText(stringValue(payload.get("targetRef")), "id")),
                    objectMapper.convertValue(payload.get("request"), UpdateCampaignRequest.class));
            case "APPROVE" -> response = campaignManagementService.approveCampaign(
                    Long.valueOf(requireText(stringValue(payload.get("targetRef")), "id")),
                    objectMapper.convertValue(payload.get("request"), ApproveCampaignRequest.class));
            case "STOP" -> response = campaignManagementService.stopCampaign(
                    Long.valueOf(requireText(stringValue(payload.get("targetRef")), "id")));
            case "CANCEL" -> response = campaignManagementService.cancelCampaign(
                    Long.valueOf(requireText(stringValue(payload.get("targetRef")), "id")));
            case "RESTART" -> response = campaignManagementService.restartCampaign(
                    Long.valueOf(requireText(stringValue(payload.get("targetRef")), "id")));
            default -> throw new IllegalArgumentException("Unsupported campaign approval operation");
        }
        return approvalExecutionResponse(operation, payload.get("targetRef"), response);
    }

    private Map<String, Object> approveCustomerProfileChange(BoApprovalRequest approval) {
        Map<String, Object> payload = readJsonMap(approval.getPayloadJson());
        String operation = requireText(stringValue(payload.get("operation")), "operation");
        Long customerId = Long.valueOf(requireText(stringValue(payload.get("targetRef")), "id"));
        BlockUserRequest request = objectMapper.convertValue(payload.get("request"), BlockUserRequest.class);
        Object response;
        switch (operation) {
            case "BLOCK" -> response = backofficeCustomerService.blockCustomer(customerId, request);
            case "UNBLOCK" -> response = backofficeCustomerService.unblockCustomer(customerId, request);
            default -> throw new IllegalArgumentException("Unsupported customer approval operation");
        }
        return approvalExecutionResponse(operation, payload.get("targetRef"), response);
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
            notifyApprovalPending(created, "New liquidation approval pending", "A liquidation request is waiting for checker approval.");
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

    private Map<String, Object> toApprovalSubmittedResponse(BoApprovalRequest approval) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("approvalRequired", true);
        response.put("approvalId", approval.getId());
        response.put("status", approval.getStatus().name());
        response.put("entityType", approval.getEntityType().name());
        response.put("entityRef", approval.getEntityRef());
        response.put("message", "Request submitted for maker-checker approval.");
        return response;
    }

    private Map<String, Object> approvalPayload(String actionCode, String targetRef, Object request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("actionCode", actionCode);
        payload.put("targetRef", targetRef);
        payload.put("request", request);
        return payload;
    }

    private Map<String, Object> approvalPayload(String actionCode, String operation, String targetRef,
            Object request, String userId) {
        Map<String, Object> payload = approvalPayload(actionCode, targetRef, request);
        payload.put("operation", operation);
        payload.put("userId", userId);
        return payload;
    }

    private Map<String, Object> submitReferralApproval(String operation, String targetRef, Object payload, String userId,
            Long actorAdminId, HttpServletRequest request) {
        ApprovalActionType actionType = ApprovalActionType.valueOf(operation);
        Map<String, Object> approvalPayload = approvalPayload("REFERRAL_PROGRAM_CHANGE", operation, targetRef, payload, userId);
        BoApprovalRequest approval = approvalRequestRepository.save(
                BoApprovalRequest.builder()
                        .module(ApprovalModule.REFERRAL)
                        .subModule(ApprovalSubModule.PROGRAM)
                        .entityType(ApprovalEntityType.REFERRAL_PROGRAM)
                        .entityRef(uniqueEntityRef("REFERRAL_PROGRAM_CHANGE", operation + "::" + targetRef))
                        .actionType(actionType)
                        .status(ApprovalStatus.PENDING)
                        .makerAdminId(actorAdminId)
                        .payloadJson(writeJson(approvalPayload))
                        .submittedAt(Instant.now())
                        .build()
        );
        createEvent(approval, ApprovalEventType.REQUESTED, actorAdminId,
                "Referral program " + operation.toLowerCase(Locale.ROOT) + " submitted for approval", approvalPayload);
        notifyApprovalPending(approval, "Referral program approval pending",
                "A referral program change is waiting for checker approval.");
        audit(request, actorAdminId, approval.getId(), "REFERRAL_PROGRAM_APPROVAL_REQUESTED", Map.of(
                "operation", operation,
                "targetRef", targetRef,
                "approvalId", approval.getId()
        ));
        return toApprovalSubmittedResponse(approval);
    }

    private Map<String, Object> submitCampaignApproval(String operation, String targetRef, Object payload,
            Long actorAdminId, HttpServletRequest request) {
        ApprovalActionType actionType = ApprovalActionType.valueOf(operation);
        Map<String, Object> approvalPayload = approvalPayload("CAMPAIGN_CHANGE", operation, targetRef, payload, null);
        BoApprovalRequest approval = approvalRequestRepository.save(
                BoApprovalRequest.builder()
                        .module(ApprovalModule.CAMPAIGN)
                        .subModule(ApprovalSubModule.MARKETING)
                        .entityType(ApprovalEntityType.CAMPAIGN)
                        .entityRef(uniqueEntityRef("CAMPAIGN_CHANGE", operation + "::" + targetRef))
                        .actionType(actionType)
                        .status(ApprovalStatus.PENDING)
                        .makerAdminId(actorAdminId)
                        .payloadJson(writeJson(approvalPayload))
                        .submittedAt(Instant.now())
                        .build()
        );
        createEvent(approval, ApprovalEventType.REQUESTED, actorAdminId,
                "Campaign " + operation.toLowerCase(Locale.ROOT) + " submitted for approval", approvalPayload);
        notifyApprovalPending(approval, "Campaign approval pending",
                "A campaign change is waiting for checker approval.");
        audit(request, actorAdminId, approval.getId(), "CAMPAIGN_APPROVAL_REQUESTED", Map.of(
                "operation", operation,
                "targetRef", targetRef,
                "approvalId", approval.getId()
        ));
        return toApprovalSubmittedResponse(approval);
    }

    private Map<String, Object> submitCustomerApproval(String operation, Long id, BlockUserRequest payload,
            Long actorAdminId, HttpServletRequest request) {
        String targetRef = requireText(id == null ? null : String.valueOf(id), "id");
        ApprovalActionType actionType = ApprovalActionType.valueOf(operation);
        Map<String, Object> approvalPayload = approvalPayload("CUSTOMER_BLOCK_UNBLOCK", operation, targetRef, payload, null);
        BoApprovalRequest approval = approvalRequestRepository.save(
                BoApprovalRequest.builder()
                        .module(ApprovalModule.CUSTOMER)
                        .subModule(ApprovalSubModule.PROFILE)
                        .entityType(ApprovalEntityType.CUSTOMER_PROFILE)
                        .entityRef(uniqueEntityRef("CUSTOMER_BLOCK_UNBLOCK", operation + "::" + targetRef))
                        .actionType(actionType)
                        .status(ApprovalStatus.PENDING)
                        .makerAdminId(actorAdminId)
                        .payloadJson(writeJson(approvalPayload))
                        .submittedAt(Instant.now())
                        .build()
        );
        createEvent(approval, ApprovalEventType.REQUESTED, actorAdminId,
                "Customer " + operation.toLowerCase(Locale.ROOT) + " submitted for approval", approvalPayload);
        notifyApprovalPending(approval, "Customer profile approval pending",
                "A customer block/unblock action is waiting for checker approval.");
        audit(request, actorAdminId, approval.getId(), "CUSTOMER_BLOCK_UNBLOCK_APPROVAL_REQUESTED", Map.of(
                "operation", operation,
                "customerId", targetRef,
                "approvalId", approval.getId()
        ));
        return toApprovalSubmittedResponse(approval);
    }

    private Map<String, Object> approvalExecutionResponse(String operation, Object targetRef, Object response) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("operation", operation);
        result.put("targetRef", targetRef);
        result.put("response", response);
        return result;
    }

    private String uniqueEntityRef(String actionCode, String targetRef) {
        String suffix = "::" + UUID.randomUUID().toString().substring(0, 8);
        int targetLimit = Math.max(1, 128 - actionCode.length() - 2 - suffix.length());
        String safeTargetRef = targetRef.length() <= targetLimit ? targetRef : targetRef.substring(0, targetLimit);
        return actionCode + "::" + safeTargetRef + suffix;
    }

    private String requireText(String value, String field) {
        if (value == null || value.trim().isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
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

    private void notifyApprovalPending(BoApprovalRequest approval, String title, String message) {
        notificationService.notifyUsersWithAnyPermission(
                notificationPermissionsFor(approval),
                "APPROVAL",
                BackofficeNotificationSeverity.INFO,
                title,
                message,
                approval.getEntityType() == null ? null : approval.getEntityType().name(),
                approval.getEntityRef(),
                toApprovalRow(approval)
        );
    }

    private void notifyApprovalDecision(BoApprovalRequest approval,
            String title,
            BackofficeNotificationSeverity severity,
            String decision) {
        String message = "Approval item " + approval.getEntityRef() + " was " + decision + ".";
        if (approval.getMakerAdminId() != null) {
            notificationService.createForAdmin(
                    approval.getMakerAdminId(),
                    "APPROVAL",
                    severity,
                    title,
                    message,
                    approval.getEntityType() == null ? null : approval.getEntityType().name(),
                    approval.getEntityRef(),
                    toApprovalRow(approval)
            );
            return;
        }

        notificationService.notifyUsersWithAnyPermission(
                List.of("approval.inbox.view"),
                "APPROVAL",
                severity,
                title,
                message,
                approval.getEntityType() == null ? null : approval.getEntityType().name(),
                approval.getEntityRef(),
                toApprovalRow(approval)
        );
    }

    private List<String> notificationPermissionsFor(BoApprovalRequest approval) {
        if (approval.getSubModule() == ApprovalSubModule.LIQUIDATION) {
            return List.of("approval.inbox.view", "investment.liquidation.approve");
        }
        if (approval.getEntityType() == ApprovalEntityType.FXPEER_AIRTIME_REVERSAL
                || approval.getEntityType() == ApprovalEntityType.TRANSACTIONS_REVERSAL) {
            return List.of("approval.inbox.view", "reversal.manual.approve");
        }
        if (approval.getEntityType() == ApprovalEntityType.FXPEER_INVESTMENT_PRODUCT) {
            return List.of("approval.inbox.view", "investment.product.approve");
        }
        if (approval.getEntityType() == ApprovalEntityType.APP_CONFIG) {
            return List.of("approval.inbox.view", "app_config.manage");
        }
        if (approval.getEntityType() == ApprovalEntityType.REFERRAL_PROGRAM) {
            return List.of("approval.inbox.view", "referral.program.manage");
        }
        if (approval.getEntityType() == ApprovalEntityType.CAMPAIGN) {
            return List.of("approval.inbox.view", "campaign.approve");
        }
        if (approval.getEntityType() == ApprovalEntityType.CUSTOMER_PROFILE) {
            return List.of("approval.inbox.view", "customer.profile.manage");
        }
        return List.of("approval.inbox.view");
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

    private boolean enumNameMatches(Enum<?> value, String expected) {
        return expected == null || (value != null && value.name().equalsIgnoreCase(expected));
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
