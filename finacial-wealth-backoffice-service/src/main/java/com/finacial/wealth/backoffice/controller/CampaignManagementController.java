/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.backoffice.controller;

import com.finacial.wealth.backoffice.approval.policy.service.ApprovalPolicyService;
import com.finacial.wealth.backoffice.approval.service.ApprovalService;
import com.finacial.wealth.backoffice.campaign.model.ApproveCampaignRequest;
import com.finacial.wealth.backoffice.campaign.model.CreateCampaignRequest;
import com.finacial.wealth.backoffice.campaign.model.UpdateCampaignRequest;
import com.finacial.wealth.backoffice.integrations.profiling.CampaignManagementService;
import com.finacial.wealth.backoffice.model.ApiResponseModel;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author olufemioshin
 */
@RestController
@RequestMapping("/backoffice/campaigns")
public class CampaignManagementController {

    private final CampaignManagementService service;
    private final ApprovalPolicyService approvalPolicyService;
    private final ApprovalService approvalService;

    public CampaignManagementController(CampaignManagementService service,
            ApprovalPolicyService approvalPolicyService,
            ApprovalService approvalService) {
        this.service = service;
        this.approvalPolicyService = approvalPolicyService;
        this.approvalService = approvalService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('campaign.manage','ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponseModel> create(@RequestBody CreateCampaignRequest req,
            HttpServletRequest httpRequest) {
        if (approvalPolicyService.requiresApproval("CAMPAIGN_CHANGE")) {
            Long adminUserId = (Long) httpRequest.getAttribute("boAdminUserId");
            return approvalAccepted(approvalService.submitCampaignCreate(req, adminUserId, httpRequest));
        }
        return ResponseEntity.ok(service.createCampaign(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('campaign.manage','ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponseModel> update(@PathVariable Long id, @RequestBody UpdateCampaignRequest req,
            HttpServletRequest httpRequest) {
        if (approvalPolicyService.requiresApproval("CAMPAIGN_CHANGE")) {
            Long adminUserId = (Long) httpRequest.getAttribute("boAdminUserId");
            return approvalAccepted(approvalService.submitCampaignUpdate(id, req, adminUserId, httpRequest));
        }
        return ResponseEntity.ok(service.updateCampaign(id, req));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyAuthority('campaign.approve','ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponseModel> approve(@PathVariable Long id, @RequestBody ApproveCampaignRequest req,
            HttpServletRequest httpRequest) {
        if (approvalPolicyService.requiresApproval("CAMPAIGN_CHANGE")) {
            Long adminUserId = (Long) httpRequest.getAttribute("boAdminUserId");
            return approvalAccepted(approvalService.submitCampaignApprove(id, req, adminUserId, httpRequest));
        }
        return ResponseEntity.ok(service.approveCampaign(id, req));
    }

    @PostMapping("/{id}/stop")
    @PreAuthorize("hasAnyAuthority('campaign.manage','ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponseModel> stop(@PathVariable Long id, HttpServletRequest httpRequest) {
        if (approvalPolicyService.requiresApproval("CAMPAIGN_CHANGE")) {
            Long adminUserId = (Long) httpRequest.getAttribute("boAdminUserId");
            return approvalAccepted(approvalService.submitCampaignStatus(id, "STOP", adminUserId, httpRequest));
        }
        return ResponseEntity.ok(service.stopCampaign(id));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('campaign.manage','ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponseModel> cancel(@PathVariable Long id, HttpServletRequest httpRequest) {
        if (approvalPolicyService.requiresApproval("CAMPAIGN_CHANGE")) {
            Long adminUserId = (Long) httpRequest.getAttribute("boAdminUserId");
            return approvalAccepted(approvalService.submitCampaignStatus(id, "CANCEL", adminUserId, httpRequest));
        }
        return ResponseEntity.ok(service.cancelCampaign(id));
    }

    @PostMapping("/{id}/restart")
    @PreAuthorize("hasAnyAuthority('campaign.manage','ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponseModel> restart(@PathVariable Long id, HttpServletRequest httpRequest) {
        if (approvalPolicyService.requiresApproval("CAMPAIGN_CHANGE")) {
            Long adminUserId = (Long) httpRequest.getAttribute("boAdminUserId");
            return approvalAccepted(approvalService.submitCampaignStatus(id, "RESTART", adminUserId, httpRequest));
        }
        return ResponseEntity.ok(service.restartCampaign(id));
    }

    @GetMapping("/get-all")
    @PreAuthorize("hasAnyAuthority('campaign.view','campaign.manage','campaign.approve','ROLE_SUPER_ADMIN')")
    public ApiResponseModel list() {
        return service.listCampaigns();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('campaign.view','campaign.manage','campaign.approve','ROLE_SUPER_ADMIN')")
    public ApiResponseModel get(@PathVariable Long id) {
        return service.getCampaign(id);
    }

    @GetMapping("/{id}/audit")
    @PreAuthorize("hasAnyAuthority('campaign.view','campaign.manage','campaign.approve','audit.view','ROLE_SUPER_ADMIN')")
    public ApiResponseModel audit(@PathVariable Long id) {
        return service.getCampaignAudit(id);
    }

    private ResponseEntity<ApiResponseModel> approvalAccepted(Map<String, Object> data) {
        ApiResponseModel response = new ApiResponseModel();
        response.setStatusCode(202);
        response.setDescription("Pending approval");
        response.setData(data);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
