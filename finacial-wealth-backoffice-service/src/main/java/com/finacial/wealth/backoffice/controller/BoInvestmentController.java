package com.finacial.wealth.backoffice.controller;

import com.finacial.wealth.backoffice.integrations.fxpeer.FxPeerExchangeClient;
import com.finacial.wealth.backoffice.integrations.fxpeer.model.FeaturedServicesConfigRequest;
import com.finacial.wealth.backoffice.integrations.fxpeer.model.InvestmentProductUpsertRequest;
import com.finacial.wealth.backoffice.integrations.fxpeer.model.LiquidationApprovalRequest;
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
import java.time.YearMonth;
import java.util.*;

@RestController
@RequestMapping({"/backoffice/investments", "/bo/backoffice/investments"})
@RequiredArgsConstructor
@Tag(name = "Investments", description = "Backoffice investment product, transaction, and liquidation queue endpoints.")
public class BoInvestmentController {

    private final FxPeerExchangeClient fxPeerClient;

    @GetMapping("/products")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    @Operation(
            summary = "List investment products",
            description = "Returns the investment products configured in the exchange service. Requires one of the investment operations roles.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public Map<String, Object> getProducts(HttpServletRequest req) {
        String auth = req.getHeader("Authorization"); // preserve case
        return fxPeerClient.getInvestmentProducts(auth);
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
        String auth = req.getHeader("Authorization");
        Map<String, Object> data = fxPeerClient.getInvestmentProducts(auth);

        return extractItems(data).stream()
                .filter(item -> productCode.equalsIgnoreCase(String.valueOf(item.get("productCode"))))
                .findFirst()
                .map(this::toSingleProductResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(notFoundProductResponse(productCode)));
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
        return fxPeerClient.saveFeaturedServicesConfig(auth, request);
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
    public Map<String, Object> createProduct(@Valid @RequestBody InvestmentProductUpsertRequest req) {
        return fxPeerClient.createInvestmentProduct(req);
    }

    @PostMapping("/approve-liquidation-request")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    @Operation(
            summary = "Approve a liquidation directly",
            description = "Direct liquidation approval bridge to the exchange service. Prefer the approval inbox for maker-checker flows.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public Map<String, Object> approveLiquidation(@RequestBody LiquidationApprovalRequest req) {
        return fxPeerClient.approveLiquidation(req);
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
        return fxPeerClient.cancelLiquidation(req);
    }

    @PutMapping("/products/{productCode}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    @Operation(
            summary = "Update an investment product",
            description = "Updates an investment product. The product code in the path takes precedence over any body value.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public Map<String, Object> updateProduct(
            @PathVariable String productCode,
            @Valid @RequestBody InvestmentProductUpsertRequest req
    ) {
        req.setProductCode(productCode); // path wins
        return fxPeerClient.updateInvestmentProduct(productCode, req);
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
        String auth = req.getHeader("Authorization");
        response.setHeader("Content-Disposition", "attachment; filename=\"investment-products.csv\"");
        Map<String, Object> data = fxPeerClient.getInvestmentProducts(auth);
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
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractItems(Map<String, Object> data) {
        Object d = data.get("data");
        if (d instanceof Map<?, ?> m) {
            Object items = m.get("items");
            if (items instanceof List<?> l) {
                return (List<Map<String, Object>>) l;
            }
            Object items2 = m.get("data");
            if (items2 instanceof List<?> l2) {
                return (List<Map<String, Object>>) l2;
            }
        }
        Object items3 = data.get("items");
        if (items3 instanceof List<?> l3) {
            return (List<Map<String, Object>>) l3;
        }
        return Collections.emptyList();
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

    private Map<String, Object> notFoundProductResponse(String productCode) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("statusCode", 404);
        response.put("description", "Investment product not found");
        response.put("data", null);
        response.put("productCode", productCode);
        return response;
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
