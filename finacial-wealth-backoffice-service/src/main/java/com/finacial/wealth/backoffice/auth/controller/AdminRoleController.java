/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.backoffice.auth.controller;

import com.finacial.wealth.backoffice.auth.dto.AdminRoleDto;
import com.finacial.wealth.backoffice.auth.dto.CreateRoleRequest;
import com.finacial.wealth.backoffice.auth.dto.UpdateRolePermissionsRequest;
import com.finacial.wealth.backoffice.auth.service.AdminRolePermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author olufemioshin
 */
@RestController
@RequestMapping({"/admin/roles", "/backoffice/admin/roles", "/bo/admin/roles", "/api/admin/roles"})
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Role management and permission assignment endpoints.")
public class AdminRoleController {

    private final AdminRolePermissionService adminRolePermissionService;

    @PreAuthorize("hasAnyAuthority('role.manage','ROLE_SUPER_ADMIN')")
    @GetMapping
    @Operation(
            summary = "List admin roles",
            description = "Returns all admin roles with their assigned permission codes. Requires `role.manage` or `ROLE_SUPER_ADMIN`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<List<AdminRoleDto>> list() {
        return ResponseEntity.ok(adminRolePermissionService.listRoles());
    }

    @PreAuthorize("hasAnyAuthority('role.manage','ROLE_SUPER_ADMIN')")
    @PostMapping
    @Operation(
            summary = "Create an admin role",
            description = "Creates a role and optionally assigns permission codes at creation time.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<AdminRoleDto> create(@RequestBody CreateRoleRequest request) {
        return ResponseEntity.ok(adminRolePermissionService.createRole(request));
    }

    @PreAuthorize("hasAnyAuthority('role.manage','ROLE_SUPER_ADMIN')")
    @PutMapping("/{roleId}/permissions")
    @Operation(
            summary = "Replace role permissions",
            description = "Replaces the permission set assigned to a role. Use permission codes from `/backoffice/admin/permissions`.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<AdminRoleDto> updatePermissions(
            @PathVariable Long roleId,
            @RequestBody UpdateRolePermissionsRequest request
    ) {
        return ResponseEntity.ok(adminRolePermissionService.updatePermissions(roleId, request));
    }
}
