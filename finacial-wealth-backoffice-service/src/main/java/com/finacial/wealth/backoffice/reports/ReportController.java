package com.finacial.wealth.backoffice.reports;

import com.finacial.wealth.backoffice.audit.AuditAspect.Audited;
import com.finacial.wealth.backoffice.notification.entity.BackofficeNotificationSeverity;
import com.finacial.wealth.backoffice.notification.service.BackofficeNotificationService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bo/reports")
@RequiredArgsConstructor
public class ReportController {

  private final BackofficeNotificationService notificationService;

  @GetMapping(value = "/sample-transactions.csv", produces = "text/csv")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FINANCE')")
  @Audited(action = "EXPORT_CSV", entityType = "REPORT", entityId = "sample-transactions")
  public void sample(HttpServletResponse response,
                     @RequestAttribute("boAdminUserId") Long adminUserId,
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

    notificationService.createForAdmin(
        adminUserId,
        "REPORT",
        BackofficeNotificationSeverity.INFO,
        "Report ready",
        "Sample transactions CSV export is ready.",
        "REPORT",
        "sample-transactions.csv",
        Map.of("report", "sample-transactions.csv", "walletNo", walletNo == null ? "" : walletNo)
    );
  }
}
