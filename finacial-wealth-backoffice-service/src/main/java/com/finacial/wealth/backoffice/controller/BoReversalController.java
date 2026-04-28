package com.finacial.wealth.backoffice.controller;

import com.finacial.wealth.backoffice.reversal.dto.ManualReversalRequest;
import com.finacial.wealth.backoffice.reversal.service.ReversalExceptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/backoffice/reversals")
@RequiredArgsConstructor
@Tag(name = "Reversals", description = "Reversal exception monitoring and manual reversal request endpoints.")
public class BoReversalController {

    private final ReversalExceptionService reversalExceptionService;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyAuthority('reversal.exception.view','ROLE_SUPER_ADMIN')")
    @Operation(summary = "Get reversal exception summary", security = @SecurityRequirement(name = "bearerAuth"))
    public Map<String, Object> getSummary(HttpServletRequest request) {
        return reversalExceptionService.getSummary(request);
    }

    @GetMapping("/cases")
    @PreAuthorize("hasAnyAuthority('reversal.exception.view','ROLE_SUPER_ADMIN')")
    @Operation(summary = "List reversal exception cases", security = @SecurityRequirement(name = "bearerAuth"))
    public Map<String, Object> listCases(
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            HttpServletRequest request
    ) {
        return reversalExceptionService.listCases(source, status, page, size, request);
    }

    @PostMapping("/cases/{source}/{caseRef}/manual-request")
    @PreAuthorize("hasAnyAuthority('reversal.manual.request','ROLE_SUPER_ADMIN')")
    @Operation(summary = "Submit a manual reversal request for maker-checker approval", security = @SecurityRequirement(name = "bearerAuth"))
    public Map<String, Object> requestManualReversal(
            @PathVariable String source,
            @PathVariable String caseRef,
            @RequestAttribute("boAdminUserId") Long actorAdminId,
            @RequestBody(required = false) ManualReversalRequest body,
            HttpServletRequest request
    ) {
        String notes = body == null ? null : body.notes();
        return reversalExceptionService.requestManualReversal(source, caseRef, notes, actorAdminId, request);
    }
}
