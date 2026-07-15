package com.finacial.wealth.backoffice.controller;

import com.finacial.wealth.backoffice.approval.policy.service.ApprovalPolicyService;
import com.finacial.wealth.backoffice.approval.service.ApprovalService;
import com.finacial.wealth.backoffice.integrations.profiling.ReferralProgramManagementService;
import com.finacial.wealth.backoffice.model.ApiResponseModel;
import com.finacial.wealth.backoffice.referral.model.CreateReferralProgramRequest;
import com.finacial.wealth.backoffice.referral.model.UpdateReferralProgramRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/backoffice/referral-programs")
public class ReferralProgramManagementController {

    private final ReferralProgramManagementService service;
    private final ApprovalPolicyService approvalPolicyService;
    private final ApprovalService approvalService;

    public ReferralProgramManagementController(ReferralProgramManagementService service,
            ApprovalPolicyService approvalPolicyService,
            ApprovalService approvalService) {
        this.service = service;
        this.approvalPolicyService = approvalPolicyService;
        this.approvalService = approvalService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('referral.program.manage','ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponseModel> create(@RequestBody CreateReferralProgramRequest req,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            HttpServletRequest httpRequest) {
        if (approvalPolicyService.requiresApproval("REFERRAL_PROGRAM_CHANGE")) {
            Long adminUserId = (Long) httpRequest.getAttribute("boAdminUserId");
            return approvalAccepted(approvalService.submitReferralProgramCreate(req, userId, adminUserId, httpRequest));
        }
        return ResponseEntity.ok(service.createReferralProgram(req, userId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('referral.program.manage','ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponseModel> update(@PathVariable Long id,
            @RequestBody UpdateReferralProgramRequest req,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            HttpServletRequest httpRequest) {
        if (approvalPolicyService.requiresApproval("REFERRAL_PROGRAM_CHANGE")) {
            Long adminUserId = (Long) httpRequest.getAttribute("boAdminUserId");
            return approvalAccepted(approvalService.submitReferralProgramUpdate(id, req, userId, adminUserId, httpRequest));
        }
        return ResponseEntity.ok(service.updateReferralProgram(id, req, userId));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAnyAuthority('referral.program.manage','ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponseModel> activate(@PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            HttpServletRequest httpRequest) {
        if (approvalPolicyService.requiresApproval("REFERRAL_PROGRAM_CHANGE")) {
            Long adminUserId = (Long) httpRequest.getAttribute("boAdminUserId");
            return approvalAccepted(approvalService.submitReferralProgramStatus(id, "ACTIVATE", userId, adminUserId, httpRequest));
        }
        return ResponseEntity.ok(service.activateReferralProgram(id, userId));
    }

    @PostMapping("/{id}/pause")
    @PreAuthorize("hasAnyAuthority('referral.program.manage','ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponseModel> pause(@PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            HttpServletRequest httpRequest) {
        if (approvalPolicyService.requiresApproval("REFERRAL_PROGRAM_CHANGE")) {
            Long adminUserId = (Long) httpRequest.getAttribute("boAdminUserId");
            return approvalAccepted(approvalService.submitReferralProgramStatus(id, "PAUSE", userId, adminUserId, httpRequest));
        }
        return ResponseEntity.ok(service.pauseReferralProgram(id, userId));
    }

    @PostMapping("/{id}/end")
    @PreAuthorize("hasAnyAuthority('referral.program.manage','ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponseModel> end(@PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            HttpServletRequest httpRequest) {
        if (approvalPolicyService.requiresApproval("REFERRAL_PROGRAM_CHANGE")) {
            Long adminUserId = (Long) httpRequest.getAttribute("boAdminUserId");
            return approvalAccepted(approvalService.submitReferralProgramStatus(id, "END", userId, adminUserId, httpRequest));
        }
        return ResponseEntity.ok(service.endReferralProgram(id, userId));
    }

    @GetMapping("/get-all")
    @PreAuthorize("hasAnyAuthority('referral.program.view','referral.program.manage','ROLE_SUPER_ADMIN')")
    public ApiResponseModel list(@RequestParam(value = "productType", required = false) String productType) {
        return service.listReferralPrograms(productType);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('referral.program.view','referral.program.manage','ROLE_SUPER_ADMIN')")
    public ApiResponseModel get(@PathVariable Long id) {
        return service.getReferralProgram(id);
    }

    @GetMapping("/{id}/audit")
    @PreAuthorize("hasAnyAuthority('referral.program.view','referral.program.manage','audit.view','ROLE_SUPER_ADMIN')")
    public ApiResponseModel audit(@PathVariable Long id) {
        return service.getReferralProgramAudit(id);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyAuthority('referral.program.view','referral.program.manage','ROLE_SUPER_ADMIN')")
    public ApiResponseModel active(@RequestParam("productType") String productType) {
        return service.getActiveReferralProgram(productType);
    }

    private ResponseEntity<ApiResponseModel> approvalAccepted(Map<String, Object> data) {
        ApiResponseModel response = new ApiResponseModel();
        response.setStatusCode(202);
        response.setDescription("Pending approval");
        response.setData(data);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
