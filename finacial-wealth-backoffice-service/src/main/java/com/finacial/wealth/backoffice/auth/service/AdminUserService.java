/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.backoffice.auth.service;

/**
 *
 * @author olufemioshin
 */
import com.fasterxml.jackson.databind.ObjectMapper;

import com.finacial.wealth.backoffice.admin.dto.*;
import com.finacial.wealth.backoffice.audit.entity.AdminAuditLog;

import com.finacial.wealth.backoffice.auth.entity.BoAdminUser;
import com.finacial.wealth.backoffice.auth.repo.BoAdminUserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final BoAdminUserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AdminAuditService auditService;
    private final ObjectMapper objectMapper;

    @Transactional
    public AdminUserResponse createAdmin(Long actorAdminId, CreateAdminUserRequest req, String ip, String ua) {
        if (req.email() == null || req.email().trim().isEmpty()) {
            throw new IllegalArgumentException("email is required");
        }
        if (req.fullName() == null || req.fullName().trim().isEmpty()) {
            throw new IllegalArgumentException("fullName is required");
        }
        if (req.roles() == null || req.roles().isEmpty()) {
            throw new IllegalArgumentException("roles is required");
        }

        userRepo.findByEmailIgnoreCase(req.email().trim())
                .ifPresent(u -> {
                    throw new IllegalArgumentException("email already exists");
                });

        // Generate temporary password and force reset flow
        String tempPassword = generateTempPassword(14);

        BoAdminUser u = new BoAdminUser();
        u.setEmail(req.email().trim().toLowerCase());
        u.setFullName(req.fullName().trim());
       // u.setActive(true);
       // u.setFailedLoginAttempts(0);
        u.setLockedUntil(null);
        u.setMfaEnabled(false); // MUST enroll MFA on first login per requirement :contentReference[oaicite:7]{index=7}
        u.setPasswordHash(passwordEncoder.encode(tempPassword));
        u.setRoles(new HashSet<>(req.roles()));

        u = userRepo.save(u);

        audit("ADMIN_CREATE", actorAdminId, u.getId(), ip, ua,
                Map.of("email", u.getEmail(), "roles", u.getRoles()));

        // IMPORTANT: return temp password ONLY if your policy allows it (often you email it instead).
        // Here we do NOT return it to keep API safe.
        return toResponse(u);
    }

    @Transactional
    public AdminUserResponse updateAdmin(Long actorAdminId, Long adminId, UpdateAdminUserRequest req, String ip, String ua) {
        BoAdminUser u = userRepo.findById(adminId).orElseThrow(() -> new IllegalArgumentException("Admin user not found"));

        if (req.fullName() != null && !req.fullName().trim().isEmpty()) {
            u.setFullName(req.fullName().trim());
        }
        if (req.roles() != null && !req.roles().isEmpty()) {
            u.setRoles(new HashSet<>(req.roles()));
        }

        u = userRepo.save(u);

        audit("ADMIN_UPDATE", actorAdminId, u.getId(), ip, ua,
                Map.of("roles", u.getRoles(), "fullName", u.getFullName()));

        return toResponse(u);
    }

    @Transactional
    public AdminUserResponse setStatus(
            Long actorAdminId,
            Long adminId,
            boolean activate,
            String ip,
            String ua
    ) {
        BoAdminUser u = userRepo.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin user not found"));

        u.setStatus(activate ? BoAdminUser.Status.ACTIVE : BoAdminUser.Status.SUSPENDED);
        u = userRepo.save(u);

        audit(
                activate ? "ADMIN_ACTIVATE" : "ADMIN_SUSPEND",
                actorAdminId,
                u.getId(),
                ip,
                ua,
                Map.of("newStatus", u.getStatus().name())
        );

        return toResponse(u);
    }

    @Transactional
    public void triggerPasswordReset(Long actorAdminId, Long adminId, String ip, String ua) {
        BoAdminUser u = userRepo.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin user not found"));

        String tempPassword = generateTempPassword(14);

        u.setPasswordHash(passwordEncoder.encode(tempPassword));

        // reset lockout state (your fields)
        u.setFailedAttempts(0);
        u.setLockedUntil(null);

        userRepo.save(u);

        audit("ADMIN_PASSWORD_RESET", actorAdminId, u.getId(), ip, ua,
                Map.of("email", u.getEmail()));

        // TODO: send tempPassword via email/SMS/secure channel.
    }

    private void audit(String action, Long actorId, Long targetId, String ip, String ua, Map<String, Object> meta) {
        try {
            auditService.log(AdminAuditLog.builder()
                    .actorAdminId(actorId)
                    .action(action)
                    .targetType("BoAdminUser")
                    .targetId(targetId)
                    .ip(ip)
                    .userAgent(ua)
                    .metadataJson(objectMapper.writeValueAsString(meta))
                    .build());
        } catch (Exception ignored) {
            // last resort: don't break business flow because audit serialization failed
        }
    }

    private AdminUserResponse toResponse(BoAdminUser u) {
        return new AdminUserResponse(u.getId(), u.getEmail(), u.getFullName(), u.getStatus(), u.isMfaEnabled(), u.getRoles());
    }

    private String generateTempPassword(int len) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%";
        Random r = new Random();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(r.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
