package com.finacial.wealth.backoffice.controller;

import com.finacial.wealth.backoffice.integrations.fxpeer.FxPeerExchangeClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/backoffice/fxpeer/services")
@RequiredArgsConstructor
public class BoFxPeerServicesController {

    private final FxPeerExchangeClient fxPeerClient;

    @GetMapping("/airtime-reversals/summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    public Map<String, Object> getAirtimeReversalSummary(HttpServletRequest req) {
        String auth = req.getHeader("Authorization");
        return fxPeerClient.getAirtimeReversalSummary(auth);
    }

    @GetMapping("/airtime-reversals")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    public Map<String, Object> getAirtimeReversalCases(
            HttpServletRequest req,
            @RequestParam(required = false) String status
    ) {
        String auth = req.getHeader("Authorization");
        return fxPeerClient.getAirtimeReversalCases(auth, status);
    }
}
