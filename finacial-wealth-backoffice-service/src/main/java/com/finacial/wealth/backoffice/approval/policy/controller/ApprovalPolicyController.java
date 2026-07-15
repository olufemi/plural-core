package com.finacial.wealth.backoffice.approval.policy.controller;

import com.finacial.wealth.backoffice.approval.policy.dto.ApprovalPolicyRequest;
import com.finacial.wealth.backoffice.approval.policy.dto.ApprovalPolicyResponse;
import com.finacial.wealth.backoffice.approval.policy.service.ApprovalPolicyService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/backoffice/approval-policies")
@RequiredArgsConstructor
public class ApprovalPolicyController {

    private final ApprovalPolicyService service;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('approval.policy.view','approval.policy.manage','ROLE_SUPER_ADMIN')")
    public List<ApprovalPolicyResponse> list() {
        return service.listActivePolicies();
    }

    @GetMapping("/{actionCode}")
    @PreAuthorize("hasAnyAuthority('approval.policy.view','approval.policy.manage','ROLE_SUPER_ADMIN')")
    public ApprovalPolicyResponse get(@PathVariable String actionCode) {
        return service.getPolicy(actionCode);
    }

    @PutMapping("/{actionCode}")
    @PreAuthorize("hasAnyAuthority('approval.policy.manage','ROLE_SUPER_ADMIN')")
    public ApprovalPolicyResponse update(@PathVariable String actionCode,
            @RequestBody ApprovalPolicyRequest request,
            @RequestAttribute("boAdminUserId") Long actorAdminId,
            HttpServletRequest httpRequest) {
        return service.updatePolicy(actionCode, request, actorAdminId, httpRequest);
    }
}
