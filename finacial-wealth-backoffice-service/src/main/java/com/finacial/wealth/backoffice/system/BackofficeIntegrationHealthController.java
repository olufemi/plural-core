package com.finacial.wealth.backoffice.system;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/backoffice/system", "/bo/backoffice/system"})
@RequiredArgsConstructor
public class BackofficeIntegrationHealthController {

    private final BackofficeIntegrationHealthService service;

    @GetMapping("/integration-health")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    public Map<String, Object> integrationHealth() {
        return service.health();
    }
}
