package com.finacial.wealth.backoffice.controller;

import com.finacial.wealth.backoffice.integrations.fxpeer.FxPeerExchangeClient;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/backoffice/investments")
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
}
