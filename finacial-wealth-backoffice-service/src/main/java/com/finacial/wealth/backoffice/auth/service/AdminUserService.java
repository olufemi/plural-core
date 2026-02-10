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
import com.finacial.wealth.backoffice.PasswordPolicy;

import com.finacial.wealth.backoffice.admin.dto.*;
import com.finacial.wealth.backoffice.audit.entity.AdminAuditLog;
import com.finacial.wealth.backoffice.auth.dto.AdminRoleDto;

import com.finacial.wealth.backoffice.auth.entity.BoAdminUser;
import com.finacial.wealth.backoffice.auth.repo.BoAdminRoleRepository;
import com.finacial.wealth.backoffice.auth.repo.BoAdminUserRepository;
import com.finacial.wealth.backoffice.model.BaseResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final BoAdminUserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AdminAuditService auditService;
    private final ObjectMapper objectMapper;
    private final BoAdminRoleRepository roleRepo;
    private final PasswordPolicy passwordPolicy;

    public List<AdminRoleDto> listRoles() {
        return roleRepo.findAllByOrderByNameAsc()
                .stream()
                .map(r -> new AdminRoleDto(r.getId(), r.getName()))
                .toList();
    }

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

        if (!req.password().equals(req.confirmPassword())) {
            throw new IllegalArgumentException("Confirm password not same as password!");
        }

        BaseResponse getPol = passwordPolicy.validate(req.password());

        if (getPol.getStatusCode() != 200) {
            throw new IllegalArgumentException(getPol.getDescription());
        }

        String encoded = passwordEncoder.encode(req.password());
        u.setPassword(encoded);
        // u.setPasswordHash(passwordEncoder.encode(req.password()));
        u.setRoles(new HashSet<>(req.roles()));

        u = userRepo.save(u);

        auditService.audit("ADMIN_CREATE", actorAdminId, u.getId(), ip, ua,
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

        auditService.audit("ADMIN_UPDATE", actorAdminId, u.getId(), ip, ua,
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

        auditService.audit(
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

        auditService.audit("ADMIN_PASSWORD_RESET", actorAdminId, u.getId(), ip, ua,
                Map.of("email", u.getEmail()));

        // TODO: send tempPassword via email/SMS/secure channel.
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

    @Transactional(readOnly = true)
    public AdminUserResponse getAdmin(Long actorAdminId, Long adminId, String ip, String ua) {

        BoAdminUser u = userRepo.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin user not found"));

        // optional audit (only if your org audits reads)
        auditService.audit("ADMIN_VIEW", actorAdminId, u.getId(), ip, ua,
                Map.of("adminId", adminId));

        return toResponse(u);
    }

    @Transactional(readOnly = true)
    public Page<AdminUserResponse> getAdmins(Long actorAdminId,
            int page,
            int size,
            String q,
            String ip,
            String ua) {

        // guard page/size
        int p = Math.max(page, 0);
        int s = Math.min(Math.max(size, 1), 100);

        PageRequest pr = PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "id"));

        String query = (q == null ? null : q.trim());
        boolean hasQ = (query != null && !query.isEmpty());

        Page<BoAdminUser> result = hasQ
                ? userRepo.findByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(query, query, pr)
                : userRepo.findAll(pr);

        // audit metadata (no null values)
        Map<String, Object> meta = new HashMap<>();
        meta.put("page", p);
        meta.put("size", s);
        if (hasQ) {
            meta.put("q", query);
        }

        // IMPORTANT: do audit in a separate transaction so read-only tx is not poisoned
        safeAuditAdminList(actorAdminId, ip, ua, meta);

        return result.map(this::toResponse);
    }

    private void safeAuditAdminList(Long actorAdminId, String ip, String ua, Map<String, Object> meta) {
        try {
            auditService.audit("ADMIN_LIST", actorAdminId, null, ip, ua, meta);
        } catch (Exception e) {
            // don't break list reads if audit fails
           // log.warn("Audit failed for ADMIN_LIST actorAdminId={} ip={}", actorAdminId, ip, e);
        }
    }

}
