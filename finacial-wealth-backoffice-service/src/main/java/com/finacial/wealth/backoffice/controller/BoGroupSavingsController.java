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
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/groups")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    @Operation(
            summary = "List group savings groups",
            description = "Returns paginated group savings records for the Group Management screen.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public Map<String, Object> listGroups(
            @Parameter(description = "Optional normalized status filter: ALL, INITIATED, CREATED, ACTIVE, IN_PROGRESS, COMPLETED, CLOSED.")
            @RequestParam(required = false) String status,
            @Parameter(description = "Optional search across group name, invite code, owner email, phone, wallet id, and transaction references.")
            @RequestParam(required = false) String search,
            @Parameter(description = "Zero-based page number.")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size.")
            @RequestParam(defaultValue = "20") Integer size
    ) {
        return transactionsClient.listGroupSavingsGroups(status, search, page, size);
    }

    @GetMapping("/groups/{groupId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    @Operation(
            summary = "Get group savings group detail",
            description = "Returns group detail, members, and cycle schedule for the Group Management detail view.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public Map<String, Object> getGroup(@PathVariable Long groupId) {
        return transactionsClient.getGroupSavingsGroup(groupId);
    }

    @PostMapping("/groups/{groupId}/close")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    @Audited(action = "CLOSE_GROUP_SAVING", entityType = "GROUP_SAVINGS")
    @Operation(
            summary = "Close a group savings group",
            description = "Soft-closes a group savings record for administrative cleanup. Pause and resume need a real transaction-service paused state before exposure.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public Map<String, Object> closeGroup(@PathVariable Long groupId) {
        return transactionsClient.closeGroupSavingsGroup(groupId);
    }

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
