package com.finacial.wealth.backoffice.auth.controller;

import com.finacial.wealth.backoffice.auth.dto.AdminPermissionDto;
import com.finacial.wealth.backoffice.auth.service.AdminRolePermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/permissions")
@RequiredArgsConstructor
@Tag(name = "Permissions", description = "Permission catalog endpoints used for role and approval setup.")
public class AdminPermissionController {

    private final AdminRolePermissionService adminRolePermissionService;

    @PreAuthorize("hasAnyAuthority('role.manage','ROLE_SUPER_ADMIN')")
    @GetMapping
    @Operation(
            summary = "List permission catalog",
            description = "Returns the available permission codes that can be assigned to admin roles.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<List<AdminPermissionDto>> list() {
        return ResponseEntity.ok(adminRolePermissionService.listPermissions());
    }
}
