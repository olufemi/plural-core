package com.finacial.wealth.backoffice.approval.controller;

import com.finacial.wealth.backoffice.approval.dto.ApprovalDecisionRequest;
import com.finacial.wealth.backoffice.approval.dto.ApprovalResubmitRequest;
import com.finacial.wealth.backoffice.approval.service.ApprovalService;
import jakarta.servlet.http.HttpServletRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
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
@RequestMapping("/backoffice/approvals")
@RequiredArgsConstructor
@Tag(name = "Approvals", description = "Maker-checker approval inbox and decision endpoints.")
public class ApprovalController {

    private final ApprovalService approvalService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('approval.inbox.view','ROLE_SUPER_ADMIN')")
    @Operation(
            summary = "List approval requests",
            description = "Returns the backoffice approval inbox for liquidation and reversal workflows. Requires `approval.inbox.view` or `ROLE_SUPER_ADMIN`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public Map<String, Object> list(
            @Parameter(description = "Optional approval status filter such as PENDING, IN_REMEDIATION, or RESUBMITTED")
            @RequestParam(required = false) String status,
            @Parameter(description = "Zero-based page number")
            @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") Integer size
    ) {
        return approvalService.listApprovals(status, page, size);
    }

    @GetMapping("/{approvalId}")
    @PreAuthorize("hasAnyAuthority('approval.inbox.view','ROLE_SUPER_ADMIN')")
    @Operation(
            summary = "Get approval request details",
            description = "Returns a single approval request with audit-style events for workflow inspection.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public Map<String, Object> get(@PathVariable Long approvalId) {
        return approvalService.getApproval(approvalId);
    }

    @PostMapping("/{approvalId}/approve")
    @PreAuthorize("hasAnyAuthority('investment.liquidation.approve','reversal.manual.approve','ROLE_SUPER_ADMIN')")
    @Operation(
            summary = "Approve an approval request",
            description = "Approves a liquidation or manual reversal approval item and forwards execution to the owning service. "
                    + "Requires `investment.liquidation.approve`, `reversal.manual.approve`, or `ROLE_SUPER_ADMIN`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Approval processed"),
            @ApiResponse(responseCode = "400", description = "Request is not in an approvable state"),
            @ApiResponse(responseCode = "403", description = "Maker-checker violation or missing authority")
    })
    public Map<String, Object> approve(
            @PathVariable Long approvalId,
            @RequestAttribute("boAdminUserId") Long actorAdminId,
            HttpServletRequest request
    ) {
        return approvalService.approve(approvalId, actorAdminId, request);
    }

    @PostMapping("/{approvalId}/reject")
    @PreAuthorize("hasAnyAuthority('investment.liquidation.approve','reversal.manual.approve','ROLE_SUPER_ADMIN')")
    @Operation(
            summary = "Reject an approval request",
            description = "Moves an approval item into remediation with an optional rejection reason. "
                    + "Requires `investment.liquidation.approve`, `reversal.manual.approve`, or `ROLE_SUPER_ADMIN`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(@ApiResponse(
            responseCode = "200",
            description = "Request moved to remediation",
            content = @Content(examples = @ExampleObject(
                    name = "reject-body",
                    value = "{\"reason\":\"Duplicate reversal risk identified during checker review\"}"
            ))
    ))
    public Map<String, Object> reject(
            @PathVariable Long approvalId,
            @RequestAttribute("boAdminUserId") Long actorAdminId,
            @RequestBody(required = false) ApprovalDecisionRequest decision,
            HttpServletRequest request
    ) {
        ApprovalDecisionRequest safeDecision = decision == null ? new ApprovalDecisionRequest(null) : decision;
        return approvalService.reject(approvalId, actorAdminId, safeDecision, request);
    }

    @PostMapping("/{approvalId}/resubmit")
    @PreAuthorize("hasAnyAuthority('investment.liquidation.remediate','reversal.manual.remediate','ROLE_SUPER_ADMIN')")
    @Operation(
            summary = "Resubmit an approval request",
            description = "Resubmits a remediated item back into the approval queue. "
                    + "Requires `investment.liquidation.remediate`, `reversal.manual.remediate`, or `ROLE_SUPER_ADMIN`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(@ApiResponse(
            responseCode = "200",
            description = "Request resubmitted",
            content = @Content(examples = @ExampleObject(
                    name = "resubmit-body",
                    value = "{\"notes\":\"Duplicate check cleared and original auto-reversal failure confirmed\"}"
            ))
    ))
    public Map<String, Object> resubmit(
            @PathVariable Long approvalId,
            @RequestAttribute("boAdminUserId") Long actorAdminId,
            @RequestBody(required = false) ApprovalResubmitRequest body,
            HttpServletRequest request
    ) {
        ApprovalResubmitRequest safeBody = body == null ? new ApprovalResubmitRequest(null) : body;
        return approvalService.resubmit(approvalId, actorAdminId, safeBody, request);
    }
}
