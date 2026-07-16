package com.finacial.wealth.backoffice.savedview.controller;

import com.finacial.wealth.backoffice.savedview.dto.SavedViewRequest;
import com.finacial.wealth.backoffice.savedview.dto.SavedViewResponse;
import com.finacial.wealth.backoffice.savedview.service.BoSavedViewService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/backoffice/saved-views", "/bo/backoffice/saved-views"})
@RequiredArgsConstructor
public class BoSavedViewController {

    private final BoSavedViewService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    public List<SavedViewResponse> list(
            @RequestAttribute("boAdminUserId") Long adminUserId,
            @RequestParam String moduleKey
    ) {
        return service.list(adminUserId, moduleKey);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    public SavedViewResponse save(
            @RequestAttribute("boAdminUserId") Long adminUserId,
            @Valid @RequestBody SavedViewRequest request
    ) {
        return service.save(adminUserId, request);
    }

    @PostMapping("/{viewId}/default")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    public SavedViewResponse setDefault(
            @RequestAttribute("boAdminUserId") Long adminUserId,
            @PathVariable Long viewId
    ) {
        return service.setDefault(adminUserId, viewId);
    }

    @DeleteMapping("/{viewId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','OPERATIONS','FINANCE')")
    public Map<String, Object> delete(
            @RequestAttribute("boAdminUserId") Long adminUserId,
            @PathVariable Long viewId
    ) {
        return service.delete(adminUserId, viewId);
    }
}
