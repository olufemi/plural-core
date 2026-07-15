/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.backoffice.controller;

import com.finacial.wealth.backoffice.approval.policy.service.ApprovalPolicyService;
import com.finacial.wealth.backoffice.approval.service.ApprovalService;
import com.finacial.wealth.backoffice.integrations.profiling.BackofficeCustomerService;
import com.finacial.wealth.backoffice.model.ApiResponse;
import com.finacial.wealth.backoffice.model.BlockUserRequest;
import com.finacial.wealth.backoffice.model.RegWalletInfoBackofficeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 *
 * @author olufemioshin
 */
@RestController
@RequestMapping("/backoffice/profiling")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Customer support and customer 360 backoffice endpoints.")
public class ProfilingManagementController {

    private final BackofficeCustomerService backofficeCustomerService;
    private final ApprovalPolicyService approvalPolicyService;
    private final ApprovalService approvalService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    @Operation(
            summary = "List customers",
            description = "Returns paginated customer records from profiling for backoffice support and operations teams.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ApiResponse<Page<RegWalletInfoBackofficeResponse>> getAllCustomers(
            @Parameter(description = "Zero-based page number")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort expression, for example id,desc")
            @RequestParam(defaultValue = "id,desc") String sort
    ) {
        return backofficeCustomerService.getAllCustomers(page, size, sort);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    @Operation(
            summary = "Get customer profile",
            description = "Returns the customer profile used to anchor backoffice customer support screens. Use the profiling record `id` returned by the customer list endpoint.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ApiResponse<RegWalletInfoBackofficeResponse> getCustomerById(
            @Parameter(description = "Profiling record id returned by the list customers endpoint")
            @PathVariable("id") Long id
    ) {
        return backofficeCustomerService.getCustomerById(id);
    }

    @GetMapping("/{id}/customer-360")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    @Operation(
            summary = "Get customer 360",
            description = "Returns a consolidated customer support view including profile, KYC status summary, wallets/accounts, devices, referral context, access status, basic activity timeline, and investment sections where available.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public Map<String, Object> getCustomer360(
            @Parameter(description = "Profiling record id returned by the list customers endpoint")
            @PathVariable("id") Long id
    ) {
        return backofficeCustomerService.getCustomer360(id);
    }

    @GetMapping("/{id}/investment-summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    @Operation(
            summary = "Get customer investment summary",
            description = "Aggregates customer profile, orders, liquidations, and positions into a frontend-friendly summary response. Use the profiling record `id` returned by the customer list endpoint.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public Map<String, Object> getCustomerInvestmentSummary(
            @Parameter(description = "Profiling record id returned by the list customers endpoint")
            @PathVariable("id") Long id
    ) {
        return backofficeCustomerService.getCustomerInvestmentSummary(id);
    }

    @GetMapping("/{id}/orders")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    @Operation(
            summary = "List customer investment and topup requests",
            description = "Returns customer-level order history with status and type filtering for customer detail screens. Use the profiling record `id` returned by the customer list endpoint.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public Map<String, Object> getCustomerInvestmentOrders(
            @Parameter(description = "Profiling record id returned by the list customers endpoint")
            @PathVariable("id") Long id,
            @Parameter(description = "Optional order type such as SUBSCRIPTION or TOPUP")
            @RequestParam(required = false) String type,
            @Parameter(description = "Optional comma-separated order status filter")
            @RequestParam(required = false) String status,
            @Parameter(description = "Zero-based page number")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") Integer size
    ) {
        return backofficeCustomerService.getCustomerInvestmentOrders(id, type, status, page, size);
    }

    @GetMapping("/{id}/liquidations")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    @Operation(
            summary = "List customer liquidation requests",
            description = "Returns customer liquidation requests and their current statuses for the customer module. Use the profiling record `id` returned by the customer list endpoint.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public Map<String, Object> getCustomerLiquidations(
            @Parameter(description = "Profiling record id returned by the list customers endpoint")
            @PathVariable("id") Long id,
            @Parameter(description = "Optional comma-separated liquidation status filter")
            @RequestParam(required = false) String status,
            @Parameter(description = "Zero-based page number")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") Integer size
    ) {
        return backofficeCustomerService.getCustomerLiquidations(id, status, page, size);
    }

    @GetMapping("/{id}/positions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    @Operation(
            summary = "List customer investment positions",
            description = "Returns customer positions across investment products for customer detail and portfolio support screens. Use the profiling record `id` returned by the customer list endpoint.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public Map<String, Object> getCustomerInvestmentPositions(
            @Parameter(description = "Profiling record id returned by the list customers endpoint")
            @PathVariable("id") Long id,
            @Parameter(description = "Zero-based page number")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") Integer size
    ) {
        return backofficeCustomerService.getCustomerInvestmentPositions(id, page, size);
    }

    @PatchMapping("/{id}/block")
    @PreAuthorize("hasAnyAuthority('customer.profile.manage','ROLE_SUPER_ADMIN')")
    @Operation(
            summary = "Block a customer",
            description = "Blocks a customer account in profiling. If CUSTOMER_BLOCK_UNBLOCK approval policy is enabled, the action is submitted for maker-checker approval and not applied immediately.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> blockCustomer(
            @PathVariable("id") Long id,
            @RequestBody BlockUserRequest request,
            HttpServletRequest httpRequest
    ) {
        if (approvalPolicyService.requiresApproval("CUSTOMER_BLOCK_UNBLOCK")) {
            Long adminUserId = (Long) httpRequest.getAttribute("boAdminUserId");
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(approvalService.submitCustomerBlock(id, request, adminUserId, httpRequest));
        }
        return ResponseEntity.ok(backofficeCustomerService.blockCustomer(id, request));
    }

    @PatchMapping("/{id}/unblock")
    @PreAuthorize("hasAnyAuthority('customer.profile.manage','ROLE_SUPER_ADMIN')")
    @Operation(
            summary = "Unblock a customer",
            description = "Reverses a prior customer block in profiling. If CUSTOMER_BLOCK_UNBLOCK approval policy is enabled, the action is submitted for maker-checker approval and not applied immediately.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<?> unblockCustomer(
            @PathVariable("id") Long id,
            @RequestBody BlockUserRequest request,
            HttpServletRequest httpRequest
    ) {
        if (approvalPolicyService.requiresApproval("CUSTOMER_BLOCK_UNBLOCK")) {
            Long adminUserId = (Long) httpRequest.getAttribute("boAdminUserId");
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(approvalService.submitCustomerUnblock(id, request, adminUserId, httpRequest));
        }
        return ResponseEntity.ok(backofficeCustomerService.unblockCustomer(id, request));
    }
}
