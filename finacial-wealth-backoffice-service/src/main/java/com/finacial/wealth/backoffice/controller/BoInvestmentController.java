package com.finacial.wealth.backoffice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finacial.wealth.backoffice.approval.policy.service.ApprovalPolicyService;
import com.finacial.wealth.backoffice.approval.service.ApprovalService;
import com.finacial.wealth.backoffice.integrations.fxpeer.FxPeerExchangeClient;
import com.finacial.wealth.backoffice.integrations.fxpeer.model.FeaturedServicesConfigRequest;
import com.finacial.wealth.backoffice.integrations.fxpeer.model.InterestAccrueType;
import com.finacial.wealth.backoffice.integrations.fxpeer.model.InterestCapitalization;
import com.finacial.wealth.backoffice.integrations.fxpeer.model.InvestmentType;
import com.finacial.wealth.backoffice.integrations.fxpeer.model.InvestmentProductUpsertRequest;
import com.finacial.wealth.backoffice.integrations.fxpeer.model.LiquidationApprovalRequest;
import com.finacial.wealth.backoffice.integrations.fxpeer.model.LiquidationFeeAppliedTo;
import com.finacial.wealth.backoffice.integrations.fxpeer.model.LiquidationFeeType;
import com.finacial.wealth.backoffice.integrations.fxpeer.model.ScheduleMode;
import com.finacial.wealth.backoffice.integrations.fxpeer.model.ValuationMethod;
import com.finacial.wealth.backoffice.notification.entity.BackofficeNotificationSeverity;
import com.finacial.wealth.backoffice.notification.service.BackofficeNotificationService;
import com.finacial.wealth.backoffice.reports.CsvWriter;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping({"/backoffice/investments", "/bo/backoffice/investments"})
@RequiredArgsConstructor
@Tag(name = "Investments", description = "Backoffice investment product, transaction, and liquidation queue endpoints.")
public class BoInvestmentController {

    private final FxPeerExchangeClient fxPeerClient;
    private final BackofficeNotificationService notificationService;
    private final ApprovalPolicyService approvalPolicyService;
    private final ApprovalService approvalService;
    private final ObjectMapper objectMapper;

    @GetMapping("/products")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    @Operation(
            summary = "List investment products",
            description = "Returns the investment products configured in the exchange service. Requires one of the investment operations roles.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public Map<String, Object> getProducts(HttpServletRequest req) {
        return fxPeerClient.getInvestmentProducts();
    }

    @GetMapping("/products/{productCode}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    @Operation(
            summary = "Fetch an investment product",
            description = "Returns a single investment product from the backoffice catalog using the product code.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Map<String, Object>> getProduct(
            @PathVariable String productCode,
            HttpServletRequest req
    ) {
        return toStatusResponse(fxPeerClient.getInvestmentProduct(productCode));
    }

    @GetMapping("/products/{productCode}/history")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    @Operation(
            summary = "Fetch investment product history",
            description = "Returns lifecycle and current configuration metadata for a single investment product. "
                    + "Detailed before/after configuration audit will be populated from maker-checker audit when enabled.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Map<String, Object>> getProductHistory(
            @PathVariable String productCode,
            HttpServletRequest req
    ) {
        Map<String, Object> response = fxPeerClient.getInvestmentProductHistory(productCode);
        attachApprovalHistory(response, productCode);
        return toStatusResponse(response);
    }

    @GetMapping("/featured-services")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    @Operation(
            summary = "List featured service cards",
            description = "Returns the mobile featured service cards resolved by the exchange service.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public Map<String, Object> getFeaturedServices(HttpServletRequest req) {
        String auth = req.getHeader("Authorization");
        return fxPeerClient.getFeaturedServices(auth);
    }

    @GetMapping("/featured-services/config")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    @Operation(
            summary = "Fetch featured services config",
            description = "Returns the stored featured-card configuration managed by backoffice.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public Map<String, Object> getFeaturedServicesConfig(HttpServletRequest req) {
        String auth = req.getHeader("Authorization");
        return fxPeerClient.getFeaturedServicesConfig(auth);
    }

    @PostMapping("/featured-services/config")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    @Operation(
            summary = "Save featured services config",
            description = "Creates or updates the featured-card configuration used by the mobile services screen.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public Map<String, Object> saveFeaturedServicesConfig(
            HttpServletRequest req,
            @RequestBody FeaturedServicesConfigRequest request
    ) {
        String auth = req.getHeader("Authorization");
        try {
            return fxPeerClient.saveFeaturedServicesConfig(auth, request);
        } catch (RuntimeException ex) {
            notifyIntegrationFailure("Featured services config save failed", "FXPEER_FEATURED_SERVICES_CONFIG", null, ex);
            throw ex;
        }
    }

    @PostMapping("/products")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    @Operation(
            summary = "Create an investment product",
            description = "Creates a product in the exchange service using the backoffice upsert payload.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product created"),
            @ApiResponse(responseCode = "400", description = "Invalid product payload"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "403", description = "Caller lacks backoffice role")
    })
    public ResponseEntity<Map<String, Object>> createProduct(@Valid @RequestBody InvestmentProductUpsertRequest req,
            @RequestAttribute("boAdminUserId") Long adminUserId,
            HttpServletRequest httpRequest) {
        if (approvalPolicyService.requiresApproval("INVESTMENT_PRODUCT_CREATE")) {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(approvalService.submitInvestmentProductCreate(req, adminUserId, httpRequest));
        }
        try {
            return toStatusResponse(fxPeerClient.createInvestmentProduct(req));
        } catch (RuntimeException ex) {
            notifyIntegrationFailure("Investment product creation failed", "INVESTMENT_PRODUCT", req.getProductCode(), ex);
            throw ex;
        }
    }

    @PostMapping("/approve-liquidation-request")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    @Operation(
            summary = "Approve a liquidation directly",
            description = "Direct liquidation approval bridge to the exchange service. Prefer the approval inbox for maker-checker flows.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public Map<String, Object> approveLiquidation(@RequestBody LiquidationApprovalRequest req) {
        try {
            return fxPeerClient.approveLiquidation(req);
        } catch (RuntimeException ex) {
            notifyIntegrationFailure("Liquidation approval failed", "LIQUIDATION", req == null ? null : req.getOrderRef(), ex);
            throw ex;
        }
    }

    @GetMapping("/liquidations")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    @Operation(
            summary = "List active liquidation requests",
            description = "Returns liquidation requests for the operational queue. Useful for the liquidation table screen. "
                    + "Supports filtering by status, product, and request date range.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liquidation queue returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "403", description = "Caller lacks one of the investment operations roles")
    })
    public Map<String, Object> getLiquidations(
            @Parameter(description = "Comma-separated liquidation statuses such as LIQUIDATION_PENDING_APPROVAL or LIQUIDATION_PROCESSING")
            @RequestParam(required = false) String status,
            @Parameter(description = "Investment product code filter, for example MMF003")
            @RequestParam(required = false) String productCode,
            @Parameter(description = "Inclusive lower request date in YYYY-MM-DD")
            @RequestParam(required = false) LocalDate fromDate,
            @Parameter(description = "Inclusive upper request date in YYYY-MM-DD")
            @RequestParam(required = false) LocalDate toDate,
            @Parameter(description = "Zero-based page number")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") Integer size
    ) {
        return fxPeerClient.getAdminLiquidations(status, productCode, fromDate, toDate, page, size);
    }

    @GetMapping("/liquidations/history")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    @Operation(
            summary = "List historical liquidation requests",
            description = "Returns closed or historical liquidation items for the liquidation history table.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public Map<String, Object> getLiquidationHistory(
            @Parameter(description = "Optional comma-separated status filter")
            @RequestParam(required = false) String status,
            @Parameter(description = "Investment product code filter")
            @RequestParam(required = false) String productCode,
            @Parameter(description = "Inclusive lower request date in YYYY-MM-DD")
            @RequestParam(required = false) LocalDate fromDate,
            @Parameter(description = "Inclusive upper request date in YYYY-MM-DD")
            @RequestParam(required = false) LocalDate toDate,
            @Parameter(description = "Zero-based page number")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") Integer size
    ) {
        return fxPeerClient.getAdminLiquidationHistory(status, productCode, fromDate, toDate, page, size);
    }

    @GetMapping("/orders")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    @Operation(
            summary = "List investment and topup requests",
            description = "Returns subscription and topup requests for transaction monitoring. "
                    + "The `cutoffBucket` filter supports queue views such as BEFORE_CUTOFF and AFTER_CUTOFF_NEXT_DAY.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public Map<String, Object> getOrders(
            @Parameter(description = "Order type filter such as SUBSCRIPTION or TOPUP")
            @RequestParam(required = false) String type,
            @Parameter(description = "Comma-separated order statuses")
            @RequestParam(required = false) String status,
            @Parameter(description = "Investment product code filter")
            @RequestParam(required = false) String productCode,
            @Parameter(description = "Cutoff bucket filter such as BEFORE_CUTOFF or AFTER_CUTOFF_NEXT_DAY")
            @RequestParam(required = false) String cutoffBucket,
            @Parameter(description = "Inclusive lower request date in YYYY-MM-DD")
            @RequestParam(required = false) LocalDate fromDate,
            @Parameter(description = "Inclusive upper request date in YYYY-MM-DD")
            @RequestParam(required = false) LocalDate toDate,
            @Parameter(description = "Zero-based page number")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") Integer size
    ) {
        return fxPeerClient.getAdminOrders(type, status, productCode, cutoffBucket, fromDate, toDate, page, size);
    }

    @GetMapping("/performance")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    @Operation(
            summary = "Get investment performance dashboard",
            description = "Returns the investment performance dashboard payload with product filter options, date-range filtered AUM trend, product snapshots, and recent activity.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public Map<String, Object> getPerformanceDashboard(
            @Parameter(description = "Optional investment product code filter such as MMF003")
            @RequestParam(required = false) String productCode,
            @Parameter(description = "Inclusive lower valuation date in YYYY-MM-DD")
            @RequestParam(required = false) LocalDate fromDate,
            @Parameter(description = "Inclusive upper valuation date in YYYY-MM-DD")
            @RequestParam(required = false) LocalDate toDate
    ) {
        return fxPeerClient.getAdminPerformance(productCode, fromDate, toDate);
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    @Operation(
            summary = "Get investment dashboard",
            description = "Returns the frontend dashboard contract for summary metrics, AUM trend, product returns, recent activity, and filter metadata.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public Map<String, Object> getDashboard(
            @Parameter(description = "Dashboard range such as 1M, 3M, 6M, or 12M. Defaults to 3M.")
            @RequestParam(defaultValue = "3M") String range,
            @Parameter(description = "Optional investment product code filter such as MMF003")
            @RequestParam(required = false) String productCode,
            @Parameter(description = "Optional inclusive lower valuation date in YYYY-MM-DD. Overrides range start when supplied.")
            @RequestParam(required = false) LocalDate fromDate,
            @Parameter(description = "Optional inclusive upper valuation date in YYYY-MM-DD. Overrides range end when supplied.")
            @RequestParam(required = false) LocalDate toDate
    ) {
        DashboardRange dashboardRange = resolveDashboardRange(range, fromDate, toDate);
        Map<String, Object> performance = fxPeerClient.getAdminPerformance(
                productCode,
                dashboardRange.fromDate(),
                dashboardRange.toDate()
        );
        return toDashboardResponse(performance, dashboardRange);
    }


    @GetMapping("/oversight")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    @Operation(
            summary = "Get investment oversight dashboard",
            description = "Returns the oversight summary cards and normalized investment action feed for the backoffice oversight screen.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public Map<String, Object> getOversightDashboard(
            @Parameter(description = "Optional product code filter.")
            @RequestParam(value = "productCode", required = false) String productCode,
            @Parameter(description = "Optional start date in ISO format, for example 2026-04-01.")
            @RequestParam(value = "fromDate", required = false) LocalDate fromDate,
            @Parameter(description = "Optional end date in ISO format, for example 2026-04-25.")
            @RequestParam(value = "toDate", required = false) LocalDate toDate,
            @Parameter(description = "Optional action type filter. Supported values: ALLOCATION, TOPUP, LIQUIDATION.")
            @RequestParam(value = "actionType", required = false) String actionType,
            @Parameter(description = "Optional normalized status filter. Supported values: EXECUTED, PENDING, FAILED, CANCELLED.")
            @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "Optional maximum number of action rows to return. Defaults to 20 and caps at 200.")
            @RequestParam(value = "size", required = false) Integer size
    ) {
        return fxPeerClient.getAdminOversight(productCode, fromDate, toDate, actionType, status, size);
    }

    @PostMapping("/deny-customer-liquidation-request")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    @Operation(
            summary = "Cancel or deny a liquidation directly",
            description = "Direct rejection bridge to the exchange service. Prefer the approval inbox for audited maker-checker processing.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public Map<String, Object> cancelLiquidation(@RequestBody LiquidationApprovalRequest req) {
        try {
            return fxPeerClient.cancelLiquidation(req);
        } catch (RuntimeException ex) {
            notifyIntegrationFailure("Liquidation cancellation failed", "LIQUIDATION", req == null ? null : req.getOrderRef(), ex);
            throw ex;
        }
    }

    @PutMapping("/products/{productCode}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    @Operation(
            summary = "Update an investment product",
            description = "Updates an investment product. The product code in the path takes precedence over any body value.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<Map<String, Object>> updateProduct(
            @PathVariable String productCode,
            @RequestBody InvestmentProductUpsertRequest req,
            @RequestAttribute("boAdminUserId") Long adminUserId,
            HttpServletRequest httpRequest
    ) {
        req = mergeProductUpdate(productCode, req);
        if (approvalPolicyService.requiresApproval("INVESTMENT_PRODUCT_UPDATE")) {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(approvalService.submitInvestmentProductUpdate(productCode, req, adminUserId, httpRequest));
        }
        try {
            return toStatusResponse(fxPeerClient.updateInvestmentProduct(productCode, req));
        } catch (RuntimeException ex) {
            notifyIntegrationFailure("Investment product update failed", "INVESTMENT_PRODUCT", productCode, ex);
            throw ex;
        }
    }

    private InvestmentProductUpsertRequest mergeProductUpdate(String productCode, InvestmentProductUpsertRequest patch) {
        if (patch == null) {
            patch = new InvestmentProductUpsertRequest();
        }

        Map<String, Object> response = fxPeerClient.getInvestmentProduct(productCode);
        Integer statusCode = intValue(response == null ? null : response.get("statusCode"));
        if (statusCode != null && statusCode >= 400) {
            HttpStatus status = HttpStatus.resolve(statusCode);
            throw new ResponseStatusException(status == null ? HttpStatus.BAD_GATEWAY : status,
                    stringValue(response.getOrDefault("description", "Unable to fetch current investment product")));
        }

        Map<String, Object> current = objectMap(response == null ? null : response.get("data"));
        if (current.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + productCode);
        }

        InvestmentProductUpsertRequest merged = fromCurrentProduct(productCode, current);
        overlayProductPatch(merged, patch);
        merged.setProductCode(productCode);
        return merged;
    }

    private InvestmentProductUpsertRequest fromCurrentProduct(String productCode, Map<String, Object> current) {
        InvestmentProductUpsertRequest req = new InvestmentProductUpsertRequest();
        req.setProductCode(firstString(current, "productCode", productCode));
        req.setName(firstString(current, "name", null));
        req.setType(enumValue(InvestmentType.class, firstObject(current, "type", "investmentType", "InvestmentType")));
        req.setCurrency(firstString(current, "currency", null));
        req.setMinimumInvestmentAmount(decimalValue(current.get("minimumInvestmentAmount")));
        req.setValuationMethod(enumValue(ValuationMethod.class, current.get("valuationMethod")));
        req.setUnitPrice(decimalValue(current.get("unitPrice")));
        req.setYieldPa(decimalValue(current.get("yieldPa")));
        req.setYieldYtd(decimalValue(current.get("yieldYtd")));
        req.setTenorDays(intValue(current.get("tenorDays")));
        req.setActive(booleanValue(current.get("active")));
        req.setLiquidationFeeAppliedTo(enumValue(LiquidationFeeAppliedTo.class, current.get("liquidationFeeAppliedTo")));
        req.setLiquidationFeeType(enumValue(LiquidationFeeType.class, current.get("liquidationFeeType")));
        req.setLiquidationFeeRate(decimalValue(current.get("liquidationFeeRate")));
        req.setMinLiquidationFee(decimalValue(current.get("minLiquidationFee")));
        req.setLiquidationFeeCap(decimalValue(current.get("liquidationFeeCap")));
        req.setLockEnabled(booleanValue(current.get("lockEnabled")));
        req.setLockDays(intValue(current.get("lockDays")));
        req.setEarlyLiquidationFeeAppliedTo(enumValue(LiquidationFeeAppliedTo.class, current.get("earlyLiquidationFeeAppliedTo")));
        req.setEarlyLiquidationFeeType(enumValue(LiquidationFeeType.class, current.get("earlyLiquidationFeeType")));
        req.setEarlyLiquidationFeeRate(decimalValue(current.get("earlyLiquidationFeeRate")));
        req.setEarlyLiquidationFeeCap(decimalValue(current.get("earlyLiquidationFeeCap")));
        req.setPartnerProductCode(firstString(current, "partnerProductCode", null));
        req.setProspectusUrl(firstString(current, "prospectusUrl", null));
        req.setMetaJson(firstString(current, "metaJson", null));
        req.setEnableProduct(firstString(current, "enableProduct", null));
        req.setPercentageCurrValue(decimalValue(current.get("percentageCurrValue")));
        req.setScheduleMode(enumValue(ScheduleMode.class, current.get("scheduleMode")));
        req.setInterestAccrueType(enumValue(InterestAccrueType.class, current.get("interestAccrueType")));
        req.setInterestCapitalization(enumValue(InterestCapitalization.class, current.get("interestCapitalization")));
        req.setSettlementDelayMinutes(longValue(current.get("settlementDelayMinutes")));
        req.setTenorMinutes(longValue(current.get("tenorMinutes")));
        req.setMaturityAtEndOfDay(booleanValue(current.get("maturityAtEndOfDay")));
        req.setSettlementAt(instantValue(current.get("settlementAt")));
        req.setMaturityAt(instantValue(current.get("maturityAt")));
        req.setSubscriptionCutOffTime(localTimeValue(current.get("subscriptionCutOffTime")));
        return req;
    }

    private void overlayProductPatch(InvestmentProductUpsertRequest target, InvestmentProductUpsertRequest patch) {
        if (patch.getName() != null) target.setName(patch.getName());
        if (patch.getType() != null) target.setType(patch.getType());
        if (patch.getCurrency() != null) target.setCurrency(patch.getCurrency());
        if (patch.getMinimumInvestmentAmount() != null) target.setMinimumInvestmentAmount(patch.getMinimumInvestmentAmount());
        if (patch.getValuationMethod() != null) target.setValuationMethod(patch.getValuationMethod());
        if (patch.getUnitPrice() != null) target.setUnitPrice(patch.getUnitPrice());
        if (patch.getYieldPa() != null) target.setYieldPa(patch.getYieldPa());
        if (patch.getYieldYtd() != null) target.setYieldYtd(patch.getYieldYtd());
        if (patch.getTenorDays() != null) target.setTenorDays(patch.getTenorDays());
        if (patch.getActive() != null) target.setActive(patch.getActive());
        if (patch.getLiquidationFeeAppliedTo() != null) target.setLiquidationFeeAppliedTo(patch.getLiquidationFeeAppliedTo());
        if (patch.getLiquidationFeeType() != null) target.setLiquidationFeeType(patch.getLiquidationFeeType());
        if (patch.getLiquidationFeeRate() != null) target.setLiquidationFeeRate(patch.getLiquidationFeeRate());
        if (patch.getMinLiquidationFee() != null) target.setMinLiquidationFee(patch.getMinLiquidationFee());
        if (patch.getLiquidationFeeCap() != null) target.setLiquidationFeeCap(patch.getLiquidationFeeCap());
        if (patch.getLockEnabled() != null) target.setLockEnabled(patch.getLockEnabled());
        if (patch.getLockDays() != null) target.setLockDays(patch.getLockDays());
        if (patch.getEarlyLiquidationFeeAppliedTo() != null) target.setEarlyLiquidationFeeAppliedTo(patch.getEarlyLiquidationFeeAppliedTo());
        if (patch.getEarlyLiquidationFeeType() != null) target.setEarlyLiquidationFeeType(patch.getEarlyLiquidationFeeType());
        if (patch.getEarlyLiquidationFeeRate() != null) target.setEarlyLiquidationFeeRate(patch.getEarlyLiquidationFeeRate());
        if (patch.getEarlyLiquidationFeeCap() != null) target.setEarlyLiquidationFeeCap(patch.getEarlyLiquidationFeeCap());
        if (patch.getPartnerProductCode() != null) target.setPartnerProductCode(patch.getPartnerProductCode());
        if (patch.getProspectusUrl() != null) target.setProspectusUrl(patch.getProspectusUrl());
        if (patch.getMetaJson() != null) target.setMetaJson(patch.getMetaJson());
        if (patch.getEnableProduct() != null) target.setEnableProduct(patch.getEnableProduct());
        if (patch.getPercentageCurrValue() != null) target.setPercentageCurrValue(patch.getPercentageCurrValue());
        if (patch.getScheduleMode() != null) target.setScheduleMode(patch.getScheduleMode());
        if (patch.getInterestAccrueType() != null) target.setInterestAccrueType(patch.getInterestAccrueType());
        if (patch.getInterestCapitalization() != null) target.setInterestCapitalization(patch.getInterestCapitalization());
        if (patch.getSettlementDelayMinutes() != null) target.setSettlementDelayMinutes(patch.getSettlementDelayMinutes());
        if (patch.getTenorMinutes() != null) target.setTenorMinutes(patch.getTenorMinutes());
        if (patch.getMaturityAtEndOfDay() != null) target.setMaturityAtEndOfDay(patch.getMaturityAtEndOfDay());
        if (patch.getSettlementAt() != null) target.setSettlementAt(patch.getSettlementAt());
        if (patch.getMaturityAt() != null) target.setMaturityAt(patch.getMaturityAt());
        if (patch.getSubscriptionCutOffTime() != null) target.setSubscriptionCutOffTime(patch.getSubscriptionCutOffTime());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> objectMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        if (value == null) {
            return Collections.emptyMap();
        }
        return objectMapper.convertValue(value, Map.class);
    }

    private Object firstObject(Map<String, Object> source, String... keys) {
        for (String key : keys) {
            if (source.containsKey(key) && source.get(key) != null) {
                return source.get(key);
            }
        }
        return null;
    }

    private String firstString(Map<String, Object> source, String key, String fallback) {
        String value = stringValue(source.get(key));
        return value == null || value.isBlank() ? fallback : value;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private BigDecimal decimalValue(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        if (value instanceof String text && !text.isBlank()) {
            return new BigDecimal(text.trim());
        }
        return null;
    }

    private Integer intValue(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Integer.valueOf(text.trim());
        }
        return null;
    }

    private Long longValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Long.valueOf(text.trim());
        }
        return null;
    }

    private Boolean booleanValue(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof String text && !text.isBlank()) {
            return Boolean.valueOf(text.trim());
        }
        return null;
    }

    private Instant instantValue(Object value) {
        if (value instanceof Instant instant) {
            return instant;
        }
        if (value instanceof String text && !text.isBlank()) {
            return Instant.parse(text.trim());
        }
        return null;
    }

    private LocalTime localTimeValue(Object value) {
        if (value instanceof LocalTime localTime) {
            return localTime;
        }
        if (value instanceof String text && !text.isBlank()) {
            return LocalTime.parse(text.trim());
        }
        return null;
    }

    private <E extends Enum<E>> E enumValue(Class<E> type, Object value) {
        String text = stringValue(value);
        if (text == null || text.isBlank()) {
            return null;
        }
        return Enum.valueOf(type, text.trim().toUpperCase(Locale.ROOT));
    }

    @GetMapping(value = "/products/export.csv", produces = "text/csv")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FINANCE')")
    @Operation(
            summary = "Export investment products as CSV",
            description = "Exports the current product catalog in CSV format for finance or operations users.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(@ApiResponse(
            responseCode = "200",
            description = "CSV export stream",
            content = @Content(mediaType = "text/csv", examples = @ExampleObject(
                    name = "product-export",
                    value = "productCode,name,currency,minimumInvestmentAmount,status\nMMF003,Prime Money Market Fund,NGN,5000,ACTIVE"
            ))
    ))
    public void exportProducts(HttpServletResponse response, HttpServletRequest req) throws Exception {
        Long adminUserId = (Long) req.getAttribute("boAdminUserId");
        response.setHeader("Content-Disposition", "attachment; filename=\"investment-products.csv\"");
        Map<String, Object> data = fxPeerClient.getInvestmentProducts();
        List<Map<String, Object>> items = extractItems(data);

        List<String> headers = List.of("productCode", "name", "currency", "minimumInvestmentAmount", "status");
        List<List<Object>> rows = new ArrayList<>();

        for (Map<String, Object> it : items) {
            rows.add(List.of(
                    safe(it.get("productCode")),
                    safe(it.get("name")),
                    safe(it.get("currency")),
                    safe(it.get("minimumInvestmentAmount")),
                    safe(it.get("status"))
            ));
        }

        try (var writer = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {
            CsvWriter.write(writer, headers, rows);
        }

        notificationService.createForAdmin(
                adminUserId,
                "REPORT",
                BackofficeNotificationSeverity.INFO,
                "Report ready",
                "Investment product CSV export is ready.",
                "REPORT",
                "investment-products.csv",
                Map.of("report", "investment-products.csv", "rowCount", rows.size())
        );
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractItems(Map<String, Object> data) {
        if (data == null) {
            return Collections.emptyList();
        }
        Object d = data.get("data");
        if (d instanceof List<?> rootDataList) {
            return (List<Map<String, Object>>) rootDataList;
        }
        if (d instanceof Map<?, ?> m) {
            Object items = m.get("items");
            if (items instanceof List<?> l) {
                return (List<Map<String, Object>>) l;
            }
            Object items2 = m.get("data");
            if (items2 instanceof List<?> l2) {
                return (List<Map<String, Object>>) l2;
            }
            Object products = m.get("products");
            if (products instanceof List<?> l3) {
                return (List<Map<String, Object>>) l3;
            }
            Object content = m.get("content");
            if (content instanceof List<?> l4) {
                return (List<Map<String, Object>>) l4;
            }
        }
        Object items = data.get("items");
        if (items instanceof List<?> l5) {
            return (List<Map<String, Object>>) l5;
        }
        Object products = data.get("products");
        if (products instanceof List<?> l6) {
            return (List<Map<String, Object>>) l6;
        }
        Object content = data.get("content");
        if (content instanceof List<?> l7) {
            return (List<Map<String, Object>>) l7;
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private void attachApprovalHistory(Map<String, Object> response, String productCode) {
        if (response == null) {
            return;
        }

        Map<String, Object> approvalHistory = approvalService.getInvestmentProductApprovalHistory(productCode);
        Object rawData = response.get("data");
        if (rawData instanceof Map<?, ?> rawMap) {
            Map<String, Object> data = (Map<String, Object>) rawMap;
            data.put("configurationAuditAvailable", approvalHistory.get("configurationAuditAvailable"));
            data.put("approvalHistory", approvalHistory.get("items"));
            if (Boolean.TRUE.equals(approvalHistory.get("configurationAuditAvailable"))) {
                data.put("configurationAuditMessage", "Product configuration changes are available from maker-checker approval history.");
            }
            return;
        }

        response.put("approvalHistory", approvalHistory);
    }

    private boolean matchesProductCode(Map<String, Object> item, String productCode) {
        if (item == null || productCode == null) {
            return false;
        }
        return productCode.equalsIgnoreCase(safe(item.get("productCode")))
                || productCode.equalsIgnoreCase(safe(item.get("code")))
                || productCode.equalsIgnoreCase(safe(item.get("productId")))
                || productCode.equalsIgnoreCase(safe(item.get("id")));
    }

    private String safe(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private Map<String, Object> toSingleProductResponse(Map<String, Object> product) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("statusCode", 200);
        response.put("description", "Investment product fetched successfully");
        response.put("data", product);
        return response;
    }

    private ResponseEntity<Map<String, Object>> toStatusResponse(Map<String, Object> response) {
        return ResponseEntity.status(resolveStatus(response)).body(response);
    }

    private HttpStatus resolveStatus(Map<String, Object> response) {
        Object statusCode = response == null ? null : response.get("statusCode");
        if (statusCode instanceof Number number) {
            return httpStatusOrOk(number.intValue());
        }
        if (statusCode instanceof String value) {
            try {
                return httpStatusOrOk(Integer.parseInt(value));
            } catch (NumberFormatException ignored) {
                return HttpStatus.OK;
            }
        }
        return HttpStatus.OK;
    }

    private HttpStatus httpStatusOrOk(int statusCode) {
        HttpStatus status = HttpStatus.resolve(statusCode);
        return status == null ? HttpStatus.OK : status;
    }

    private Map<String, Object> notFoundProductResponse(String productCode) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("statusCode", 404);
        response.put("description", "Investment product not found");
        response.put("data", null);
        response.put("productCode", productCode);
        return response;
    }

    private void notifyIntegrationFailure(String title, String entityType, String entityRef, RuntimeException ex) {
        try {
            notificationService.notifyUsersWithAnyPermission(
                    List.of("investment.liquidation.view", "investment.order.view", "approval.inbox.view"),
                    "INTEGRATION",
                    BackofficeNotificationSeverity.CRITICAL,
                    title,
                    "A downstream service call failed while processing a backoffice operation.",
                    entityType,
                    entityRef,
                    Map.of(
                            "errorType", ex.getClass().getSimpleName(),
                            "errorMessage", ex.getMessage() == null ? "" : ex.getMessage()
                    )
            );
        } catch (Exception ignored) {
            // Keep the original integration failure as the response cause.
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toDashboardResponse(Map<String, Object> performance, DashboardRange dashboardRange) {
        Map<String, Object> sourceData = Map.of();
        Object rawData = performance == null ? null : performance.get("data");
        if (rawData instanceof Map<?, ?> rawMap) {
            sourceData = (Map<String, Object>) rawMap;
        }

        List<Map<String, Object>> aumTrend = listOfMaps(sourceData.get("aumTrend"));
        List<Map<String, Object>> productSnapshots = listOfMaps(sourceData.get("productSnapshots"));
        List<Map<String, Object>> recentActivity = listOfMaps(sourceData.get("recentActivity"));
        List<Map<String, Object>> products = listOfMaps(sourceData.get("products")).stream()
                .map(this::toDashboardProduct)
                .toList();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("filters", buildDashboardFilters(dashboardRange));
        data.put("summaryMetrics", buildDashboardSummary(sourceData, aumTrend));
        data.put("aumTrend", aumTrend);
        data.put("productReturns", productSnapshots.stream().map(this::toProductReturn).toList());
        data.put("recentActivity", toDashboardRecentActivity(recentActivity));
        data.put("products", products);
        data.put("lastUpdatedAt", Instant.now().toString());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("statusCode", 200);
        response.put("description", "Dashboard data retrieved successfully");
        response.put("data", data);
        return response;
    }

    private Map<String, Object> buildDashboardFilters(DashboardRange dashboardRange) {
        Map<String, Object> filters = new LinkedHashMap<>();
        filters.put("range", dashboardRange.range());
        filters.put("fromDate", dashboardRange.fromDate());
        filters.put("toDate", dashboardRange.toDate());
        filters.put("refreshIntervalMs", 60000);
        return filters;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> buildDashboardSummary(Map<String, Object> sourceData, List<Map<String, Object>> aumTrend) {
        Map<String, Object> summary = Map.of();
        Object rawSummary = sourceData.get("summary");
        if (rawSummary instanceof Map<?, ?> rawMap) {
            summary = (Map<String, Object>) rawMap;
        }

        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("totalAum", decimalOrZero(summary.get("aum")).compareTo(BigDecimal.ZERO) == 0
                ? latestAum(aumTrend)
                : decimalOrZero(summary.get("aum")));
        metrics.put("activeCustomers", 0);
        metrics.put("pendingTransactions", 0);
        metrics.put("todaysTransactionVolume", 0);
        return metrics;
    }

    private Map<String, Object> toProductReturn(Map<String, Object> snapshot) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("productCode", snapshot.get("productCode"));
        row.put("productName", snapshot.get("productName"));
        row.put("returnPct", firstDecimal(snapshot.get("yieldYtd"), snapshot.get("yieldPa"), snapshot.get("netChangePct")));
        return row;
    }

    private Map<String, Object> toDashboardProduct(Map<String, Object> product) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("productCode", product.get("productCode"));
        row.put("productName", product.get("productName"));
        return row;
    }

    private List<Map<String, Object>> toDashboardRecentActivity(List<Map<String, Object>> activityRows) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (int i = 0; i < activityRows.size(); i++) {
            Map<String, Object> source = activityRows.get(i);
            String activityType = Objects.toString(source.get("activityType"), "ACTIVITY");
            String createdAt = Objects.toString(source.get("createdAt"), "");

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", source.get("id") == null ? activityType + "-" + createdAt + "-" + i : source.get("id"));
            row.put("activityType", source.get("activityType"));
            row.put("productCode", source.get("productCode"));
            row.put("productName", source.get("productName"));
            row.put("description", source.get("description"));
            row.put("amount", source.get("amount") == null ? 0 : source.get("amount"));
            row.put("createdAt", source.get("createdAt"));
            rows.add(row);
        }
        return rows;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> listOfMaps(Object value) {
        if (value instanceof List<?> list) {
            return (List<Map<String, Object>>) list;
        }
        return List.of();
    }

    private DashboardRange resolveDashboardRange(String requestedRange, LocalDate requestedFromDate, LocalDate requestedToDate) {
        String normalizedRange = normalizeRange(requestedRange);
        int months = Integer.parseInt(normalizedRange.substring(0, normalizedRange.length() - 1));
        LocalDate defaultToDate = YearMonth.now().minusMonths(1).atDay(1);
        LocalDate toDate = requestedToDate == null ? defaultToDate : requestedToDate;
        LocalDate fromDate = requestedFromDate == null ? toDate.minusMonths(months - 1L) : requestedFromDate;
        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("fromDate cannot be after toDate");
        }
        return new DashboardRange(normalizedRange, fromDate, toDate);
    }

    private String normalizeRange(String value) {
        if (value == null || value.isBlank()) {
            return "3M";
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (!normalized.matches("\\d+M")) {
            throw new IllegalArgumentException("range must be in month format, for example 1M, 3M, 6M, or 12M");
        }
        int months = Integer.parseInt(normalized.substring(0, normalized.length() - 1));
        if (months <= 0 || months > 24) {
            throw new IllegalArgumentException("range must be between 1M and 24M");
        }
        return normalized;
    }

    private BigDecimal latestAum(List<Map<String, Object>> aumTrend) {
        if (aumTrend.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return decimalOrZero(aumTrend.get(aumTrend.size() - 1).get("aum"));
    }

    private BigDecimal firstDecimal(Object... values) {
        for (Object value : values) {
            BigDecimal decimal = decimalOrZero(value);
            if (decimal.compareTo(BigDecimal.ZERO) != 0) {
                return decimal;
            }
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal decimalOrZero(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal.setScale(2, RoundingMode.HALF_UP);
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue()).setScale(2, RoundingMode.HALF_UP);
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return new BigDecimal(text).setScale(2, RoundingMode.HALF_UP);
            } catch (NumberFormatException ignored) {
                return BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }

    private record DashboardRange(String range, LocalDate fromDate, LocalDate toDate) {
    }
}
