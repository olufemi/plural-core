package com.finacial.wealth.backoffice.reports;

import com.finacial.wealth.backoffice.audit.AuditAspect.Audited;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/bo/reports")
public class ReportController {

  @GetMapping(value = "/sample-transactions.csv", produces = "text/csv")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FINANCE')")
  @Audited(action = "EXPORT_CSV", entityType = "REPORT", entityId = "sample-transactions")
  public void sample(HttpServletResponse response,
                     @RequestParam(required = false) String walletNo) throws Exception {

    response.setHeader("Content-Disposition", "attachment; filename=\"sample-transactions.csv\"");

    List<String> headers = List.of("txId", "walletNo", "amount", "status", "date");
    List<List<Object>> rows = List.of(
        List.of("TX001", walletNo != null ? walletNo : "08800000001", "2000.00", "SUCCESS", LocalDate.now()),
        List.of("TX002", walletNo != null ? walletNo : "08800000002", "5000.00", "FAILED", LocalDate.now())
    );

    try (var writer = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {
      CsvWriter.write(writer, headers, rows);
    }
  }
}
