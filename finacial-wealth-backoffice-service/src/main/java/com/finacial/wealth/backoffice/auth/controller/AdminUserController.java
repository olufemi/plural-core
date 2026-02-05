/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.backoffice.auth.controller;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.backoffice.admin.dto.*;
import com.finacial.wealth.backoffice.auth.dto.AdminRoleDto;
import com.finacial.wealth.backoffice.auth.service.AdminUserService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin-users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    // You already use @RequestAttribute("boAdminUserId") in MFA confirm; reuse that pattern
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

    /*@PostMapping("/{adminId}/activate")
@PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
public ResponseEntity<AdminUserResponse> activate(
        @RequestAttribute("boAdminUserId") Long actorAdminId,
        @PathVariable Long adminId,
        HttpServletRequest http
) {
    return ResponseEntity.ok(service.setStatus(actorAdminId, adminId, true, ip(http), ua(http)));
}*/
    @PreAuthorize("hasAuthority('ROLE_SUPER_ADMIN')")
    @PostMapping("/{adminId}/password-reset")
    public ResponseEntity<Void> resetPassword(
            @RequestAttribute("boAdminUserId") Long actorAdminId,
            @PathVariable Long adminId,
            HttpServletRequest http
    ) {
        adminUserService.triggerPasswordReset(actorAdminId, adminId, ip(http), ua(http));
        return ResponseEntity.ok().build();
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
            @RequestHeader("boAdminUserId") Long actorAdminId,
            HttpServletRequest request) {
        return adminUserService.getAdmin(actorAdminId, adminId, request.getRemoteAddr(), request.getHeader("User-Agent"));
    }

    @GetMapping("/admins")
    public Page<AdminUserResponse> getAdmins(@RequestHeader("boAdminUserId") Long actorAdminId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String q,
            HttpServletRequest request) {
        return adminUserService.getAdmins(actorAdminId, page, size, q, request.getRemoteAddr(), request.getHeader("User-Agent"));
    }
}
