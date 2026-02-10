package com.finacial.wealth.backoffice.controller;

import com.finacial.wealth.backoffice.integrations.fxpeer.FxPeerExchangeClient;
import com.finacial.wealth.backoffice.integrations.fxpeer.model.InvestmentProductUpsertRequest;
import com.finacial.wealth.backoffice.reports.CsvWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/backoffice/investments")
@RequiredArgsConstructor
public class BoInvestmentController {

    private final FxPeerExchangeClient fxPeerClient;

    @GetMapping("/products")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    public Map<String, Object> getProducts(HttpServletRequest req) {
        String auth = req.getHeader("Authorization"); // preserve case
        return fxPeerClient.getInvestmentProducts(auth);
    }

    @PostMapping("/products")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    public Map<String, Object> createProduct(@RequestBody InvestmentProductUpsertRequest req) {
        return fxPeerClient.createInvestmentProduct(req);
    }

    @PutMapping("/products/{productCode}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    public Map<String, Object> updateProduct(
            @PathVariable String productCode,
            @RequestBody InvestmentProductUpsertRequest req
    ) {
        req.setProductCode(productCode); // path wins
        return fxPeerClient.updateInvestmentProduct(productCode, req);
    }

    @GetMapping(value = "/products/export.csv", produces = "text/csv")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FINANCE')")
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
