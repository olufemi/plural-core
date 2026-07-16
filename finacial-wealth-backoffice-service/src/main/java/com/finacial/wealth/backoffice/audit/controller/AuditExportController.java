package com.finacial.wealth.backoffice.audit.controller;

import com.finacial.wealth.backoffice.audit.AuditAspect.Audited;
import jakarta.servlet.http.HttpServletResponse;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/bo/backoffice/audit/export-jobs", "/backoffice/audit/export-jobs"})
public class AuditExportController {

    private final Map<String, Map<String, Object>> jobs = new ConcurrentHashMap<>();

    @PostMapping
    @PreAuthorize("hasAnyAuthority('audit.view','ROLE_SUPER_ADMIN')")
    @Audited(action = "CREATE_AUDIT_EXPORT_JOB", entityType = "AUDIT")
    public Map<String, Object> create(@RequestBody(required = false) Map<String, Object> request) {
        Map<String, Object> body = request == null ? Map.of() : request;
        String jobId = "bo-audit-export-" + UUID.randomUUID().toString().substring(0, 12);
        Map<String, Object> job = new LinkedHashMap<>();
        job.put("jobId", jobId);
        job.put("status", "READY");
        job.put("format", body.getOrDefault("format", "CSV"));
        job.put("filters", body.getOrDefault("filters", Map.of()));
        job.put("requestedAt", Instant.now());
        job.put("completedAt", Instant.now());
        job.put("downloadUrl", "/bo/backoffice/audit/export-jobs/" + jobId + "/download");
        jobs.put(jobId, job);
        return job;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('audit.view','ROLE_SUPER_ADMIN')")
    public Map<String, Object> list() {
        return Map.of("content", new ArrayList<>(jobs.values()), "totalElements", jobs.size());
    }

    @GetMapping("/{jobId}")
    @PreAuthorize("hasAnyAuthority('audit.view','ROLE_SUPER_ADMIN')")
    public Map<String, Object> get(@PathVariable String jobId) {
        Map<String, Object> job = jobs.get(jobId);
        if (job == null) {
            throw new IllegalArgumentException("Audit export job not found");
        }
        return job;
    }

    @GetMapping(value = "/{jobId}/download", produces = "text/csv")
    @PreAuthorize("hasAnyAuthority('audit.view','ROLE_SUPER_ADMIN')")
    public void download(@PathVariable String jobId, HttpServletResponse response) throws Exception {
        Map<String, Object> job = get(jobId);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + jobId + ".csv\"");
        try (var writer = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {
            com.finacial.wealth.backoffice.reports.CsvWriter.write(writer,
                    List.of("jobId", "status", "requestedAt", "completedAt"),
                    List.of(List.of(job.get("jobId"), job.get("status"), job.get("requestedAt"), job.get("completedAt"))));
        }
    }
}
