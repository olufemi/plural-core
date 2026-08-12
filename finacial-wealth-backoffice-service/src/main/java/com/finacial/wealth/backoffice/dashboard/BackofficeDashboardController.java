package com.finacial.wealth.backoffice.dashboard;

import java.time.LocalDate;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/backoffice/dashboard", "/bo/backoffice/dashboard"})
@RequiredArgsConstructor
public class BackofficeDashboardController {

    private final BackofficeDashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    public Map<String, Object> dashboard(
            @RequestAttribute(name = "boAdminUserId", required = false) Long adminUserId,
            @RequestParam(required = false, defaultValue = "TODAY") String range,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return dashboardService.dashboard(adminUserId, range, fromDate, toDate);
    }
}
