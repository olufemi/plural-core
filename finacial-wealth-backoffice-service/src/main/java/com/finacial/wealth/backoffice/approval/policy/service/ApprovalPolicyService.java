package com.finacial.wealth.backoffice.approval.policy.service;

import com.finacial.wealth.backoffice.approval.policy.dto.ApprovalPolicyRequest;
import com.finacial.wealth.backoffice.approval.policy.dto.ApprovalPolicyResponse;
import com.finacial.wealth.backoffice.approval.policy.entity.ApprovalPolicy;
import com.finacial.wealth.backoffice.approval.policy.repo.ApprovalPolicyRepository;
import com.finacial.wealth.backoffice.auth.service.AdminAuditService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApprovalPolicyService {

    private final ApprovalPolicyRepository repository;
    private final AdminAuditService adminAuditService;

    @Transactional(readOnly = true)
    public List<ApprovalPolicyResponse> listActivePolicies() {
        return repository.findByActiveTrueOrderByModuleAscSubModuleAscActionCodeAsc()
                .stream()
                .map(ApprovalPolicyResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ApprovalPolicyResponse getPolicy(String actionCode) {
        return ApprovalPolicyResponse.from(findPolicy(actionCode));
    }

    @Transactional(readOnly = true)
    public boolean requiresApproval(String actionCode) {
        return repository.findById(normalizeActionCode(actionCode))
                .filter(ApprovalPolicy::isActive)
                .map(ApprovalPolicy::isApprovalRequired)
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> approvalRequiredResponse(String actionCode, String entityRef) {
        ApprovalPolicy policy = findPolicy(actionCode);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("approvalRequired", true);
        response.put("actionCode", policy.getActionCode());
        response.put("module", policy.getModule());
        response.put("subModule", policy.getSubModule());
        response.put("entityRef", entityRef);
        response.put("status", "PENDING_APPROVAL_REQUIRED");
        response.put("message", "This action is configured for maker-checker approval and was not applied directly.");
        response.put("checkerPermission", policy.getCheckerPermission());
        response.put("slaHours", policy.getSlaHours());
        return response;
    }

    @Transactional
    public ApprovalPolicyResponse updatePolicy(String actionCode, ApprovalPolicyRequest request,
            Long actorAdminId, HttpServletRequest httpRequest) {
        ApprovalPolicy policy = findPolicy(actionCode);
        boolean beforeApprovalRequired = policy.isApprovalRequired();
        String beforeCheckerPermission = policy.getCheckerPermission();

        if (request.approvalRequired() != null) {
            policy.setApprovalRequired(request.approvalRequired());
        }
        if (request.thresholdAmount() != null) {
            policy.setThresholdAmount(request.thresholdAmount());
        }
        if (request.thresholdCurrency() != null) {
            policy.setThresholdCurrency(blankToNull(request.thresholdCurrency()));
        }
        if (request.checkerPermission() != null) {
            policy.setCheckerPermission(blankToNull(request.checkerPermission()));
        }
        if (request.slaHours() != null) {
            if (request.slaHours() < 0) {
                throw new IllegalArgumentException("slaHours cannot be negative");
            }
            policy.setSlaHours(request.slaHours());
        }
        if (request.active() != null) {
            policy.setActive(request.active());
        }
        policy.setUpdatedByAdminId(actorAdminId);

        ApprovalPolicy saved = repository.save(policy);
        auditPolicyUpdate(saved, beforeApprovalRequired, beforeCheckerPermission, actorAdminId, httpRequest);

        return ApprovalPolicyResponse.from(saved);
    }

    private void auditPolicyUpdate(ApprovalPolicy saved, boolean beforeApprovalRequired,
            String beforeCheckerPermission, Long actorAdminId, HttpServletRequest httpRequest) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("actionCode", saved.getActionCode());
        metadata.put("beforeApprovalRequired", beforeApprovalRequired);
        metadata.put("afterApprovalRequired", saved.isApprovalRequired());
        metadata.put("beforeCheckerPermission", beforeCheckerPermission);
        metadata.put("afterCheckerPermission", saved.getCheckerPermission());

        adminAuditService.audit("APPROVAL_POLICY_UPDATE", actorAdminId, "ApprovalPolicy", null,
                httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"), metadata);
    }

    private ApprovalPolicy findPolicy(String actionCode) {
        return repository.findById(normalizeActionCode(actionCode))
                .orElseThrow(() -> new IllegalArgumentException("Approval policy not found"));
    }

    private String normalizeActionCode(String actionCode) {
        if (actionCode == null || actionCode.isBlank()) {
            throw new IllegalArgumentException("actionCode is required");
        }
        return actionCode.trim().toUpperCase(Locale.ROOT);
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
