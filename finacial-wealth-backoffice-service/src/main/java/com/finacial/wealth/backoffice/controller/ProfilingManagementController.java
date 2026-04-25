/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.backoffice.controller;

import com.finacial.wealth.backoffice.integrations.profiling.BackofficeCustomerService;
import com.finacial.wealth.backoffice.model.ApiResponse;
import com.finacial.wealth.backoffice.model.BlockUserRequest;
import com.finacial.wealth.backoffice.model.RegWalletInfoBackofficeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    @Operation(
            summary = "Block a customer",
            description = "Blocks a customer account in profiling. Intended for support and risk workflows.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ApiResponse<RegWalletInfoBackofficeResponse> blockCustomer(
            @PathVariable("id") Long id,
            @RequestBody BlockUserRequest request
    ) {
        return backofficeCustomerService.blockCustomer(id, request);
    }

    @PatchMapping("/{id}/unblock")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    @Operation(
            summary = "Unblock a customer",
            description = "Reverses a prior customer block in profiling.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ApiResponse<RegWalletInfoBackofficeResponse> unblockCustomer(
            @PathVariable("id") Long id,
            @RequestBody BlockUserRequest request
    ) {
        return backofficeCustomerService.unblockCustomer(id, request);
    }
}
