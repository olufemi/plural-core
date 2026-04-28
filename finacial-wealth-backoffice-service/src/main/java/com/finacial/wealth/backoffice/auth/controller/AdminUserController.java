package com.finacial.wealth.backoffice.auth.controller;

import com.finacial.wealth.backoffice.admin.dto.AdminUserResponse;
import com.finacial.wealth.backoffice.admin.dto.CreateAdminUserRequest;
import com.finacial.wealth.backoffice.admin.dto.UpdateAdminUserRequest;
import com.finacial.wealth.backoffice.auth.dto.AdminPasswordResetResponse;
import com.finacial.wealth.backoffice.auth.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/backoffice/admin-users", "/bo/admin-users", "/admin-users"})
@RequiredArgsConstructor
@Tag(name = "Admin Users", description = "Backoffice admin user management endpoints.")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<AdminUserResponse> create(
            @RequestAttribute("boAdminUserId") Long actorAdminId,
            @RequestBody CreateAdminUserRequest req,
            HttpServletRequest http
    ) {
        return ResponseEntity.ok(adminUserService.createAdmin(actorAdminId, req, ip(http), ua(http)));
    }

    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    @PatchMapping("/{adminId}")
    public ResponseEntity<AdminUserResponse> update(
            @RequestAttribute("boAdminUserId") Long actorAdminId,
            @PathVariable Long adminId,
            @RequestBody UpdateAdminUserRequest req,
            HttpServletRequest http
    ) {
        return ResponseEntity.ok(adminUserService.updateAdmin(actorAdminId, adminId, req, ip(http), ua(http)));
    }

    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    @PostMapping("/{adminId}/activate")
    public ResponseEntity<AdminUserResponse> activate(
            @RequestAttribute("boAdminUserId") Long actorAdminId,
            @PathVariable Long adminId,
            HttpServletRequest http
    ) {
        return ResponseEntity.ok(adminUserService.setStatus(actorAdminId, adminId, true, ip(http), ua(http)));
    }

    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    @Operation(
            summary = "Reset admin password",
            description = "Generates a new temporary password for an admin user, unlocks the account, and revokes existing refresh tokens.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/{adminId}/password-reset")
    public ResponseEntity<AdminPasswordResetResponse> resetPassword(
            @RequestAttribute("boAdminUserId") Long actorAdminId,
            @PathVariable Long adminId,
            HttpServletRequest http
    ) {
        return ResponseEntity.ok(adminUserService.triggerPasswordReset(actorAdminId, adminId, ip(http), ua(http)));
    }

    @PostMapping("/{adminId}/suspend")
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<AdminUserResponse> suspend(
            @RequestAttribute("boAdminUserId") Long actorAdminId,
            @PathVariable Long adminId,
            HttpServletRequest http
    ) {
        return ResponseEntity.ok(adminUserService.setStatus(actorAdminId, adminId, false, ip(http), ua(http)));
    }

    private String ip(HttpServletRequest r) {
        String xf = r.getHeader("X-Forwarded-For");
        return (xf != null && !xf.isBlank()) ? xf.split(",")[0].trim() : r.getRemoteAddr();
    }

    private String ua(HttpServletRequest r) {
        String ua = r.getHeader("User-Agent");
        return (ua == null) ? "" : ua;
    }

    @GetMapping("/admins/{adminId}")
    public AdminUserResponse getAdmin(@PathVariable Long adminId,
            @RequestAttribute("boAdminUserId") Long actorAdminId,
            HttpServletRequest request) {
        return adminUserService.getAdmin(actorAdminId, adminId, request.getRemoteAddr(), request.getHeader("User-Agent"));
    }

    @GetMapping("/admins")
    public Page<AdminUserResponse> getAdmins(@RequestAttribute("boAdminUserId") Long actorAdminId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String q,
            HttpServletRequest request) {
        return adminUserService.getAdmins(actorAdminId, page, size, q, request.getRemoteAddr(), request.getHeader("User-Agent"));
    }
}
