package com.finacial.wealth.backoffice.search;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/backoffice/search", "/bo/backoffice/search"})
@RequiredArgsConstructor
public class BackofficeGlobalSearchController {

    private final BackofficeGlobalSearchService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    public Map<String, Object> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) List<String> types,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return service.search(q, types, page, size);
    }
}
