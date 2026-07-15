package com.finacial.wealth.backoffice.configmanagement.controller;

import com.finacial.wealth.backoffice.approval.policy.service.ApprovalPolicyService;
import com.finacial.wealth.backoffice.approval.service.ApprovalService;
import com.finacial.wealth.backoffice.configmanagement.dto.AppConfigHistoryResponse;
import com.finacial.wealth.backoffice.configmanagement.dto.AppConfigRegistryRequest;
import com.finacial.wealth.backoffice.configmanagement.dto.AppConfigResponse;
import com.finacial.wealth.backoffice.configmanagement.dto.AppConfigRollbackRequest;
import com.finacial.wealth.backoffice.configmanagement.dto.AppConfigUpdateRequest;
import com.finacial.wealth.backoffice.configmanagement.service.AppConfigManagementService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/backoffice/app-config", "/bo/backoffice/app-config"})
@RequiredArgsConstructor
public class AppConfigManagementController {

    private final AppConfigManagementService appConfigManagementService;
    private final ApprovalPolicyService approvalPolicyService;
    private final ApprovalService approvalService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('app_config.view','app_config.manage','ROLE_SUPER_ADMIN')")
    public Page<AppConfigResponse> list(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return appConfigManagementService.list(search, page, size);
    }

    @GetMapping("/{configName}")
    @PreAuthorize("hasAnyAuthority('app_config.view','app_config.manage','ROLE_SUPER_ADMIN')")
    public AppConfigResponse get(@PathVariable String configName) {
        return appConfigManagementService.get(configName);
    }

    @PutMapping("/{configName}/registry")
    @PreAuthorize("hasAnyAuthority('app_config.manage','ROLE_SUPER_ADMIN')")
    public AppConfigResponse register(
            @PathVariable String configName,
            @RequestBody AppConfigRegistryRequest request,
            @RequestAttribute("boAdminUserId") Long adminUserId,
            HttpServletRequest httpRequest) {
        return appConfigManagementService.register(configName, request, adminUserId, httpRequest);
    }

    @PatchMapping("/{configName}")
    @PreAuthorize("hasAnyAuthority('app_config.manage','ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> update(
            @PathVariable String configName,
            @RequestBody AppConfigUpdateRequest request,
            @RequestAttribute("boAdminUserId") Long adminUserId,
            HttpServletRequest httpRequest) {
        if (approvalPolicyService.requiresApproval("APP_CONFIG_UPDATE")) {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(approvalService.submitAppConfigUpdate(configName, request, adminUserId, httpRequest));
        }
        return ResponseEntity.ok(appConfigManagementService.update(configName, request, adminUserId, httpRequest));
    }

    @GetMapping("/{configName}/history")
    @PreAuthorize("hasAnyAuthority('app_config.manage','ROLE_SUPER_ADMIN')")
    public Page<AppConfigHistoryResponse> history(
            @PathVariable String configName,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return appConfigManagementService.history(configName, page, size);
    }

    @PostMapping("/{configName}/history/{historyId}/rollback")
    @PreAuthorize("hasAnyAuthority('app_config.manage','ROLE_SUPER_ADMIN')")
    public ResponseEntity<?> rollback(
            @PathVariable String configName,
            @PathVariable Long historyId,
            @RequestBody AppConfigRollbackRequest request,
            @RequestAttribute("boAdminUserId") Long adminUserId,
            HttpServletRequest httpRequest) {
        if (approvalPolicyService.requiresApproval("APP_CONFIG_UPDATE")) {
            AppConfigUpdateRequest rollbackRequest = appConfigManagementService.buildRollbackUpdateRequest(configName, historyId, request);
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(approvalService.submitAppConfigUpdate(configName, rollbackRequest, adminUserId, httpRequest));
        }
        return ResponseEntity.ok(appConfigManagementService.rollback(configName, historyId, request, adminUserId, httpRequest));
    }
}
