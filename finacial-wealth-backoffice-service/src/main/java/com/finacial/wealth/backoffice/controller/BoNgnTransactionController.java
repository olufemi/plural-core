package com.finacial.wealth.backoffice.controller;

import com.finacial.wealth.backoffice.integrations.fxpeer.FxPeerExchangeClient;
import com.finacial.wealth.backoffice.integrations.transactions.TransactionsClient;
import com.finacial.wealth.backoffice.reports.CsvWriter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping({"/backoffice/transactions", "/bo/backoffice/transactions"})
public class BoNgnTransactionController {

    private final TransactionsClient transactionsClient;
    private final FxPeerExchangeClient fxPeerExchangeClient;
    private static final List<String> EXPORT_HEADERS = List.of(
            "sourceService", "transactionId", "walletNo", "customerReference", "emailAddress",
            "transactionType", "paymentType", "requestType", "status", "amount", "fees",
            "sender", "senderName", "receiver", "receiverName", "receiverBankName",
            "receiverBankCode", "narration", "currencyCode", "createdDate", "lastModifiedDate"
    );

    @Value("${bo.downstream.internal-token:}")
    private String downstreamInternalToken;

    public BoNgnTransactionController(TransactionsClient transactionsClient, FxPeerExchangeClient fxPeerExchangeClient) {
        this.transactionsClient = transactionsClient;
        this.fxPeerExchangeClient = fxPeerExchangeClient;
    }

    @GetMapping("/ngn")
    @PreAuthorize("hasAnyAuthority('transactions.view','transactions.filter','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_OPERATIONS','ROLE_FINANCE')")
    public Map<String, Object> getNgnTransactions(
            @RequestParam(defaultValue = "ALL") String source,
            @RequestParam(required = false) String customer,
            @RequestParam(required = false) String walletId,
            @RequestParam(required = false) String transactionId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(defaultValue = "true") Boolean includeLegacyNullCurrency,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        Map<String, Object> data = ngnTransactionData(source, customer, walletId, transactionId, type, status, q,
                fromDate, toDate, includeLegacyNullCurrency, page, size, 200);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("statusCode", 200);
        response.put("description", "NGN transactions pulled successfully.");
        response.put("data", data);
        return response;
    }

    @GetMapping(value = "/ngn/export.csv", produces = "text/csv")
    @PreAuthorize("hasAnyAuthority('reports.export','transactions.view','transactions.filter','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_OPERATIONS','ROLE_FINANCE')")
    public void exportNgnTransactionsCsv(
            @RequestParam(defaultValue = "ALL") String source,
            @RequestParam(required = false) String customer,
            @RequestParam(required = false) String walletId,
            @RequestParam(required = false) String transactionId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(defaultValue = "true") Boolean includeLegacyNullCurrency,
            @RequestParam(defaultValue = "1000") Integer size,
            HttpServletResponse response) throws Exception {

        Map<String, Object> data = ngnTransactionData(source, customer, walletId, transactionId, type, status, q,
                fromDate, toDate, includeLegacyNullCurrency, 0, size, 5000);
        response.setHeader("Content-Disposition", "attachment; filename=\"ngn-transactions.csv\"");
        try (var writer = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {
            CsvWriter.write(writer, EXPORT_HEADERS, exportRows(content(data)));
        }
    }

    @GetMapping(value = "/ngn/export.pdf", produces = "application/pdf")
    @PreAuthorize("hasAnyAuthority('reports.export','transactions.view','transactions.filter','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_OPERATIONS','ROLE_FINANCE')")
    public void exportNgnTransactionsPdf(
            @RequestParam(defaultValue = "ALL") String source,
            @RequestParam(required = false) String customer,
            @RequestParam(required = false) String walletId,
            @RequestParam(required = false) String transactionId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(defaultValue = "true") Boolean includeLegacyNullCurrency,
            @RequestParam(defaultValue = "200") Integer size,
            HttpServletResponse response) throws Exception {

        Map<String, Object> data = ngnTransactionData(source, customer, walletId, transactionId, type, status, q,
                fromDate, toDate, includeLegacyNullCurrency, 0, size, 500);
        response.setHeader("Content-Disposition", "attachment; filename=\"ngn-transactions.pdf\"");
        byte[] pdf = simplePdf(content(data));
        response.getOutputStream().write(pdf);
    }

    private Map<String, Object> ngnTransactionData(
            String source,
            String customer,
            String walletId,
            String transactionId,
            String type,
            String status,
            String q,
            String fromDate,
            String toDate,
            Boolean includeLegacyNullCurrency,
            Integer page,
            Integer size,
            int maxSize) {
        requireInternalTokenConfigured();
        int safePage = Math.max(page == null ? 0 : page, 0);
        int safeSize = Math.min(Math.max(size == null ? 20 : size, 1), maxSize);
        String normalizedSource = normalizeSource(source);
        int fetchSize = normalizedSource.equals("ALL") ? Math.min(Math.max(safeSize * (safePage + 2), safeSize), maxSize) : safeSize;

        List<Map<String, Object>> rows = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        Map<String, Object> sourceTotals = new LinkedHashMap<>();

        if (normalizedSource.equals("ALL") || normalizedSource.equals("TRANSACTIONS")) {
            fetchSource("TRANSACTIONS", normalizedSource, warnings, sourceTotals, rows,
                    () -> transactionsClient.getNgnTransactions(downstreamInternalToken, customer, walletId, transactionId,
                            type, status, q, fromDate, toDate, includeLegacyNullCurrency, normalizedSource.equals("ALL") ? 0 : safePage, fetchSize));
        }
        if (normalizedSource.equals("ALL") || normalizedSource.equals("FXPEER")) {
            fetchSource("FXPEER", normalizedSource, warnings, sourceTotals, rows,
                    () -> fxPeerExchangeClient.getNgnTransactions(downstreamInternalToken, customer, walletId, transactionId,
                            type, status, q, fromDate, toDate, includeLegacyNullCurrency, normalizedSource.equals("ALL") ? 0 : safePage, fetchSize));
        }

        rows.sort(Comparator.comparing(this::createdInstant).reversed());
        List<Map<String, Object>> pageRows = normalizedSource.equals("ALL") ? page(rows, safePage, safeSize) : rows;
        long totalElements = totalElements(sourceTotals);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("content", pageRows);
        data.put("page", safePage);
        data.put("size", safeSize);
        data.put("totalElements", totalElements);
        data.put("totalPages", (totalElements + safeSize - 1) / safeSize);
        data.put("currencyCode", "NGN");
        data.put("source", normalizedSource);
        data.put("sourceTotals", sourceTotals);
        data.put("warnings", warnings);
        return data;
    }

    @GetMapping("/ngn/{transactionId}")
    @PreAuthorize("hasAnyAuthority('transactions.view','transactions.filter','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_OPERATIONS','ROLE_FINANCE')")
    public Map<String, Object> getNgnTransactionDetails(
            @PathVariable String transactionId,
            @RequestParam(defaultValue = "ALL") String source,
            @RequestParam(defaultValue = "true") Boolean includeLegacyNullCurrency) {

        requireInternalTokenConfigured();
        if (transactionId == null || transactionId.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "transactionId is required");
        }

        String normalizedSource = normalizeSource(source);
        Map<String, Object> sourceDetails = new LinkedHashMap<>();
        List<Map<String, Object>> entries = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (normalizedSource.equals("ALL") || normalizedSource.equals("TRANSACTIONS")) {
            fetchDetailSource("TRANSACTIONS", normalizedSource, warnings, sourceDetails, entries,
                    () -> transactionsClient.getNgnTransactionDetails(downstreamInternalToken, transactionId,
                            includeLegacyNullCurrency));
        }
        if (normalizedSource.equals("ALL") || normalizedSource.equals("FXPEER")) {
            fetchDetailSource("FXPEER", normalizedSource, warnings, sourceDetails, entries,
                    () -> fxPeerExchangeClient.getNgnTransactionDetails(downstreamInternalToken, transactionId,
                            includeLegacyNullCurrency));
        }

        if (entries.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "NGN transaction not found");
        }

        entries.sort(Comparator.comparing(this::createdInstant).reversed());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("transactionId", transactionId.trim());
        data.put("source", normalizedSource);
        data.put("currencyCode", "NGN");
        data.put("primary", entries.get(0));
        data.put("entryCount", entries.size());
        data.put("entries", entries);
        data.put("sources", sourceDetails);
        data.put("warnings", warnings);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("statusCode", 200);
        response.put("description", "NGN transaction details pulled successfully.");
        response.put("data", data);
        return response;
    }

    private void fetchSource(String sourceName, String requestedSource, List<String> warnings,
                             Map<String, Object> sourceTotals, List<Map<String, Object>> rows,
                             SourceCall sourceCall) {
        try {
            Map<String, Object> response = sourceCall.fetch();
            Map<String, Object> data = map(response.get("data"));
            rows.addAll(content(data));
            sourceTotals.put(sourceName, numberValue(data.get("totalElements")));
        } catch (Exception ex) {
            if (!"ALL".equals(requestedSource)) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        sourceName + " transaction source is unavailable", ex);
            }
            sourceTotals.put(sourceName, 0L);
            warnings.add(sourceName + " transaction source is unavailable: " + ex.getClass().getSimpleName());
        }
    }

    private void fetchDetailSource(String sourceName, String requestedSource, List<String> warnings,
                                   Map<String, Object> sourceDetails, List<Map<String, Object>> entries,
                                   SourceCall sourceCall) {
        try {
            Map<String, Object> response = sourceCall.fetch();
            Map<String, Object> data = map(response.get("data"));
            List<Map<String, Object>> sourceEntries = detailEntries(data);
            if (sourceEntries.isEmpty()) {
                sourceDetails.put(sourceName, data);
                if (!"ALL".equals(requestedSource)) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, sourceName + " transaction not found");
                }
                return;
            }
            entries.addAll(sourceEntries);
            sourceDetails.put(sourceName, data);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            if (!"ALL".equals(requestedSource)) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                        sourceName + " transaction source is unavailable", ex);
            }
            warnings.add(sourceName + " transaction source is unavailable: " + ex.getClass().getSimpleName());
        }
    }

    private void requireInternalTokenConfigured() {
        if (downstreamInternalToken == null || downstreamInternalToken.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "bo.downstream.internal-token is required for backoffice transaction aggregation");
        }
    }

    private String normalizeSource(String source) {
        String value = source == null ? "ALL" : source.trim().toUpperCase(Locale.ROOT);
        if ("ALL".equals(value) || "TRANSACTIONS".equals(value) || "FXPEER".equals(value)) {
            return value;
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "source must be ALL, TRANSACTIONS, or FXPEER");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object value) {
        if (value instanceof Map<?, ?> raw) {
            return (Map<String, Object>) raw;
        }
        return new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> content(Map<String, Object> data) {
        Object content = data.get("content");
        if (content instanceof List<?> rawRows) {
            List<Map<String, Object>> rows = new ArrayList<>();
            for (Object rawRow : rawRows) {
                if (rawRow instanceof Map<?, ?> rawMap) {
                    rows.add((Map<String, Object>) rawMap);
                }
            }
            return rows;
        }
        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> detailEntries(Map<String, Object> data) {
        Object entries = data.get("entries");
        if (entries instanceof List<?> rawRows) {
            List<Map<String, Object>> rows = new ArrayList<>();
            for (Object rawRow : rawRows) {
                if (rawRow instanceof Map<?, ?> rawMap) {
                    rows.add((Map<String, Object>) rawMap);
                }
            }
            return rows;
        }
        return new ArrayList<>();
    }

    private List<Map<String, Object>> page(List<Map<String, Object>> rows, int page, int size) {
        int from = Math.min(page * size, rows.size());
        int to = Math.min(from + size, rows.size());
        return new ArrayList<>(rows.subList(from, to));
    }

    private long totalElements(Map<String, Object> sourceTotals) {
        long total = 0L;
        for (Object value : sourceTotals.values()) {
            total += numberValue(value);
        }
        return total;
    }

    private long numberValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }

    private Instant createdInstant(Map<String, Object> row) {
        Object value = row.get("createdDate");
        if (value == null) {
            return Instant.EPOCH;
        }
        try {
            return Instant.parse(String.valueOf(value));
        } catch (Exception ignored) {
            return Instant.EPOCH;
        }
    }

    private List<List<Object>> exportRows(List<Map<String, Object>> rows) {
        List<List<Object>> exportRows = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            List<Object> exportRow = new ArrayList<>();
            for (String header : EXPORT_HEADERS) {
                exportRow.add(row.get(header));
            }
            exportRows.add(exportRow);
        }
        return exportRows;
    }

    private byte[] simplePdf(List<Map<String, Object>> rows) {
        List<String> lines = new ArrayList<>();
        lines.add("NGN Customer Transactions");
        lines.add("Generated: " + Instant.now());
        lines.add("Rows: " + rows.size());
        lines.add("");
        for (Map<String, Object> row : rows) {
            lines.add(String.format("%s | %s | %s | %s | %s | %s",
                    safe(row.get("createdDate")),
                    safe(row.get("sourceService")),
                    safe(row.get("transactionId")),
                    safe(row.get("walletNo")),
                    safe(row.get("amount")),
                    safe(row.get("status"))));
        }

        StringBuilder stream = new StringBuilder();
        stream.append("BT\n/F1 10 Tf\n50 790 Td\n");
        int lineCount = 0;
        for (String line : lines) {
            if (lineCount >= 70) {
                stream.append("(").append(escapePdf("Output truncated. Use CSV export for complete data.")).append(") Tj\n");
                break;
            }
            stream.append("(").append(escapePdf(line)).append(") Tj\n0 -11 Td\n");
            lineCount++;
        }
        stream.append("ET");

        byte[] content = stream.toString().getBytes(StandardCharsets.US_ASCII);
        List<byte[]> objects = List.of(
                "<< /Type /Catalog /Pages 2 0 R >>".getBytes(StandardCharsets.US_ASCII),
                "<< /Type /Pages /Kids [3 0 R] /Count 1 >>".getBytes(StandardCharsets.US_ASCII),
                "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>".getBytes(StandardCharsets.US_ASCII),
                "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>".getBytes(StandardCharsets.US_ASCII),
                ("<< /Length " + content.length + " >>\nstream\n" + stream + "\nendstream").getBytes(StandardCharsets.US_ASCII)
        );

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeAscii(out, "%PDF-1.4\n");
        List<Integer> offsets = new ArrayList<>();
        for (int i = 0; i < objects.size(); i++) {
            offsets.add(out.size());
            writeAscii(out, (i + 1) + " 0 obj\n");
            out.writeBytes(objects.get(i));
            writeAscii(out, "\nendobj\n");
        }
        int xref = out.size();
        writeAscii(out, "xref\n0 " + (objects.size() + 1) + "\n");
        writeAscii(out, "0000000000 65535 f \n");
        for (Integer offset : offsets) {
            writeAscii(out, String.format("%010d 00000 n \n", offset));
        }
        writeAscii(out, "trailer\n<< /Size " + (objects.size() + 1) + " /Root 1 0 R >>\nstartxref\n" + xref + "\n%%EOF\n");
        return out.toByteArray();
    }

    private void writeAscii(ByteArrayOutputStream out, String value) {
        out.writeBytes(value.getBytes(StandardCharsets.US_ASCII));
    }

    private String safe(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String escapePdf(String value) {
        return value.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    private interface SourceCall {
        Map<String, Object> fetch();
    }
}
