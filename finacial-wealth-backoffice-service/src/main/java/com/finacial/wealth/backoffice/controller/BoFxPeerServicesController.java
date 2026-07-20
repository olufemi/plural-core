package com.finacial.wealth.backoffice.controller;

import com.finacial.wealth.backoffice.audit.AuditAspect.Audited;
import com.finacial.wealth.backoffice.integrations.fxpeer.FxPeerExchangeClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/backoffice/fxpeer/services")
@RequiredArgsConstructor
public class BoFxPeerServicesController {

    private final FxPeerExchangeClient fxPeerClient;

    @GetMapping("/featured")
    @PreAuthorize("hasAnyAuthority('vas.view','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_OPERATIONS','ROLE_FINANCE')")
    public Map<String, Object> getFeaturedServices(HttpServletRequest req) {
        return fxPeerClient.getFeaturedServices(auth(req));
    }

    @GetMapping("/categories")
    @PreAuthorize("hasAnyAuthority('vas.view','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_OPERATIONS','ROLE_FINANCE')")
    public Map<String, Object> getCategories(HttpServletRequest req) {
        return fxPeerClient.getVasCategories(auth(req));
    }

    @PostMapping("/products")
    @PreAuthorize("hasAnyAuthority('vas.view','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_OPERATIONS','ROLE_FINANCE')")
    public Map<String, Object> getProducts(HttpServletRequest req, @RequestBody Map<String, Object> body) {
        return fxPeerClient.getVasProducts(auth(req), body);
    }

    @PostMapping("/products/by-category")
    @PreAuthorize("hasAnyAuthority('vas.view','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_OPERATIONS','ROLE_FINANCE')")
    public Map<String, Object> getProductsByCategory(HttpServletRequest req, @RequestBody Map<String, Object> body) {
        return fxPeerClient.getVasProductsByCategory(auth(req), body);
    }

    @PostMapping("/products/by-country")
    @PreAuthorize("hasAnyAuthority('vas.view','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_OPERATIONS','ROLE_FINANCE')")
    public Map<String, Object> getProductsByCountry(HttpServletRequest req, @RequestBody Map<String, Object> body) {
        return fxPeerClient.getVasProductsByCountry(auth(req), body);
    }

    @GetMapping("/airtime-reversals/summary")
    @PreAuthorize("hasAnyAuthority('vas.reversal.view','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_OPERATIONS','ROLE_FINANCE')")
    public Map<String, Object> getAirtimeReversalSummary(HttpServletRequest req) {
        return fxPeerClient.getAirtimeReversalSummary(auth(req));
    }

    @GetMapping("/airtime-reversals")
    @PreAuthorize("hasAnyAuthority('vas.reversal.view','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_OPERATIONS','ROLE_FINANCE')")
    public Map<String, Object> getAirtimeReversalCases(
            HttpServletRequest req,
            @RequestParam(required = false) String status
    ) {
        return fxPeerClient.getAirtimeReversalCases(auth(req), status);
    }

    @PostMapping("/airtime-reversals/{processId}/retry")
    @PreAuthorize("hasAnyAuthority('vas.reversal.remediate','ROLE_SUPER_ADMIN','ROLE_ADMIN','ROLE_OPERATIONS','ROLE_FINANCE')")
    @Audited(action = "RETRY_VAS_AIRTIME_REVERSAL", entityType = "VAS_AIRTIME_REVERSAL")
    public Map<String, Object> retryAirtimeReversal(
            HttpServletRequest req,
            @org.springframework.web.bind.annotation.PathVariable String processId,
            @RequestParam String reason
    ) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Direct VAS reversal retry is disabled. Submit /backoffice/reversals/cases/FXPEER_AIRTIME/{processId}/manual-request for maker-checker approval.");
    }

    private String auth(HttpServletRequest req) {
        return req.getHeader("Authorization");
    }
}
