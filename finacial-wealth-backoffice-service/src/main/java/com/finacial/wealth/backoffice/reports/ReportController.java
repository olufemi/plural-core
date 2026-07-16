package com.finacial.wealth.backoffice.reports;

import com.finacial.wealth.backoffice.audit.AuditAspect.Audited;
import com.finacial.wealth.backoffice.notification.entity.BackofficeNotificationSeverity;
import com.finacial.wealth.backoffice.notification.service.BackofficeNotificationService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/bo/reports", "/bo/backoffice/reports", "/backoffice/reports"})
@RequiredArgsConstructor
public class ReportController {

  private final BackofficeNotificationService notificationService;
  private final Map<String, Map<String, Object>> jobs = new ConcurrentHashMap<>();
  private final Map<String, Map<String, Object>> schedules = new ConcurrentHashMap<>();

  @GetMapping("/catalog")
  @PreAuthorize("hasAnyAuthority('audit.view','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_FINANCE')")
  public Map<String, Object> catalog() {
    List<Map<String, Object>> reports = List.of(
        report("sample-transactions", "Sample Transactions", "CSV", List.of("walletNo"), "Pilot smoke-test export"),
        report("investment-products", "Investment Products", "CSV", List.of("status", "assetClass"), "Investment product configuration export"),
        report("audit-log", "Audit Log", "CSV", List.of("fromDate", "toDate", "actorAdminId", "action"), "Backoffice audit activity export")
    );
    return Map.of("content", reports, "totalElements", reports.size());
  }

  @PostMapping("/jobs")
  @PreAuthorize("hasAnyAuthority('audit.view','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_FINANCE')")
  @Audited(action = "CREATE_REPORT_JOB", entityType = "REPORT")
  public Map<String, Object> createJob(
      @RequestAttribute("boAdminUserId") Long adminUserId,
      @RequestBody(required = false) Map<String, Object> request
  ) {
    Map<String, Object> body = request == null ? Map.of() : request;
    String reportCode = stringValue(body.getOrDefault("reportCode", "sample-transactions"));
    String jobId = "bo-report-" + UUID.randomUUID().toString().substring(0, 12);
    Map<String, Object> job = new LinkedHashMap<>();
    job.put("jobId", jobId);
    job.put("reportCode", reportCode);
    job.put("status", "READY");
    job.put("format", stringValue(body.getOrDefault("format", "CSV")));
    job.put("requestedByAdminId", adminUserId);
    job.put("requestedAt", Instant.now());
    job.put("completedAt", Instant.now());
    job.put("filters", body.getOrDefault("filters", Map.of()));
    job.put("downloadUrl", "/bo/backoffice/reports/jobs/" + jobId + "/download");
    jobs.put(jobId, job);

    notificationService.createForAdmin(
        adminUserId,
        "REPORT",
        BackofficeNotificationSeverity.INFO,
        "Report ready",
        "Report " + reportCode + " is ready for download.",
        "REPORT",
        jobId,
        job
    );
    return job;
  }

  @GetMapping("/jobs")
  @PreAuthorize("hasAnyAuthority('audit.view','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_FINANCE')")
  public Map<String, Object> listJobs(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    List<Map<String, Object>> all = new ArrayList<>(jobs.values());
    all.sort((a, b) -> String.valueOf(b.get("requestedAt")).compareTo(String.valueOf(a.get("requestedAt"))));
    int safePage = Math.max(page, 0);
    int safeSize = Math.min(Math.max(size, 1), 100);
    int from = Math.min(safePage * safeSize, all.size());
    int to = Math.min(from + safeSize, all.size());
    return Map.of(
        "content", all.subList(from, to),
        "page", safePage,
        "size", safeSize,
        "totalElements", all.size(),
        "totalPages", all.isEmpty() ? 0 : (int) Math.ceil((double) all.size() / safeSize)
    );
  }

  @GetMapping("/jobs/{jobId}")
  @PreAuthorize("hasAnyAuthority('audit.view','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_FINANCE')")
  public Map<String, Object> getJob(@PathVariable String jobId) {
    Map<String, Object> job = jobs.get(jobId);
    if (job == null) {
      throw new IllegalArgumentException("Report job not found");
    }
    return job;
  }

  @GetMapping(value = "/jobs/{jobId}/download", produces = "text/csv")
  @PreAuthorize("hasAnyAuthority('audit.view','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_FINANCE')")
  @Audited(action = "DOWNLOAD_REPORT_JOB", entityType = "REPORT")
  public void downloadJob(@PathVariable String jobId, HttpServletResponse response) throws Exception {
    Map<String, Object> job = getJob(jobId);
    response.setHeader("Content-Disposition", "attachment; filename=\"" + job.get("reportCode") + "-" + jobId + ".csv\"");
    try (var writer = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {
      CsvWriter.write(writer,
          List.of("jobId", "reportCode", "status", "requestedAt", "completedAt"),
          List.of(List.of(job.get("jobId"), job.get("reportCode"), job.get("status"), job.get("requestedAt"), job.get("completedAt")))
      );
    }
  }

  @GetMapping("/schedules")
  @PreAuthorize("hasAnyAuthority('audit.view','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_FINANCE')")
  public Map<String, Object> listSchedules() {
    return Map.of("content", new ArrayList<>(schedules.values()), "totalElements", schedules.size());
  }

  @PostMapping("/schedules")
  @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_ADMIN')")
  @Audited(action = "CREATE_REPORT_SCHEDULE", entityType = "REPORT")
  public Map<String, Object> createSchedule(@RequestBody Map<String, Object> request) {
    Map<String, Object> body = request == null ? Map.of() : request;
    String scheduleId = "bo-report-schedule-" + UUID.randomUUID().toString().substring(0, 10);
    Map<String, Object> schedule = new LinkedHashMap<>();
    schedule.put("scheduleId", scheduleId);
    schedule.put("reportCode", stringValue(body.getOrDefault("reportCode", "sample-transactions")));
    schedule.put("frequency", stringValue(body.getOrDefault("frequency", "MANUAL")));
    schedule.put("recipients", body.getOrDefault("recipients", List.of()));
    schedule.put("active", body.getOrDefault("active", Boolean.TRUE));
    schedule.put("createdAt", Instant.now());
    schedules.put(scheduleId, schedule);
    return schedule;
  }

  @GetMapping(value = "/sample-transactions.csv", produces = "text/csv")
  @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','FINANCE')")
  @Audited(action = "EXPORT_CSV", entityType = "REPORT", entityId = "sample-transactions")
  public void sample(HttpServletResponse response,
                     @RequestAttribute("boAdminUserId") Long adminUserId,
                     @RequestParam(required = false) String walletNo) throws Exception {

    response.setHeader("Content-Disposition", "attachment; filename=\"sample-transactions.csv\"");

    List<String> headers = List.of("txId", "walletNo", "amount", "currency", "status", "date");
    List<List<Object>> rows = List.of(
        List.of("TX001", walletNo != null ? walletNo : "08800000001", "2000.00", "CAD", "SUCCESS", LocalDate.now()),
        List.of("TX002", walletNo != null ? walletNo : "08800000002", "5000.00", "CAD", "FAILED", LocalDate.now())
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

  private Map<String, Object> report(String code, String name, String format, List<String> filters, String description) {
    Map<String, Object> item = new LinkedHashMap<>();
    item.put("code", code);
    item.put("name", name);
    item.put("format", format);
    item.put("filters", filters);
    item.put("description", description);
    item.put("async", true);
    return item;
  }

  private String stringValue(Object value) {
    return value == null ? null : String.valueOf(value).trim();
  }
}
