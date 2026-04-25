package com.finacial.wealth.backoffice.controller;

import com.finacial.wealth.backoffice.audit.AuditAspect.Audited;
import com.finacial.wealth.backoffice.integrations.transactions.TransactionsClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/backoffice/group-savings")
@RequiredArgsConstructor
@Tag(name = "Group Savings", description = "Backoffice monitoring and operational endpoints for contribution, payout, and slot tracking.")
public class BoGroupSavingsController {

    private final TransactionsClient transactionsClient;

    @GetMapping("/contribution-payout-monitoring")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    @Operation(
            summary = "Get contribution and payout monitoring dashboard",
            description = "Returns group-savings inflow/outflow totals, chart points, and operational alerts for the contribution and payout monitoring screen.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public Map<String, Object> getContributionPayoutMonitoring(
            @Parameter(description = "Aggregation period. Supported values: DAILY, WEEKLY, MONTHLY.")
            @RequestParam(required = false) String period,
            @Parameter(description = "Optional start date in YYYY-MM-DD.")
            @RequestParam(required = false) LocalDate fromDate,
            @Parameter(description = "Optional end date in YYYY-MM-DD.")
            @RequestParam(required = false) LocalDate toDate,
            @Parameter(description = "Optional group id filter.")
            @RequestParam(required = false) Long groupId
    ) {
        return transactionsClient.getContributionPayoutMonitoring(
                period,
                fromDate != null ? fromDate.toString() : null,
                toDate != null ? toDate.toString() : null,
                groupId
        );
    }

    @GetMapping("/slot-assignment-tracking")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    @Operation(
            summary = "Get slot assignment and payout tracking dashboard",
            description = "Returns slot schedule rows, payout history, and group-savings alerts for the slot assignment and tracking screen.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public Map<String, Object> getSlotAssignmentTracking(
            @Parameter(description = "Optional group id filter.")
            @RequestParam(required = false) Long groupId,
            @Parameter(description = "Optional slot status filter. Supported values include UPCOMING, IN_PROGRESS, MISSED, COMPLETED.")
            @RequestParam(required = false) String status
    ) {
        return transactionsClient.getSlotAssignmentTracking(groupId, status);
    }

    @PostMapping("/delete")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Audited(action = "DELETE_GROUP_SAVING", entityType = "GROUP_SAVINGS")
    @Operation(
            summary = "Delete a group savings configuration",
            description = "Bridges the existing delete-group-saving transaction endpoint for administrative cleanup.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public Map<String, Object> deleteGroupSaving(@RequestBody Map<String, Object> request) {
        return transactionsClient.deleteGroupSaving(request);
    }
}
