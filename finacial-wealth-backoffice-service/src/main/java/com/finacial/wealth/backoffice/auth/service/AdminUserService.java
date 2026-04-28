package com.finacial.wealth.backoffice.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finacial.wealth.backoffice.PasswordPolicy;
import com.finacial.wealth.backoffice.admin.dto.AdminUserResponse;
import com.finacial.wealth.backoffice.admin.dto.CreateAdminUserRequest;
import com.finacial.wealth.backoffice.admin.dto.UpdateAdminUserRequest;
import com.finacial.wealth.backoffice.auth.dto.AdminPasswordResetResponse;
import com.finacial.wealth.backoffice.auth.dto.AdminRoleDto;
import com.finacial.wealth.backoffice.auth.entity.BoAdminUser;
import com.finacial.wealth.backoffice.auth.repo.BoAdminRoleRepository;
import com.finacial.wealth.backoffice.auth.repo.BoAdminUserRepository;
import com.finacial.wealth.backoffice.model.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final BoAdminUserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AdminAuditService auditService;
    private final ObjectMapper objectMapper;
    private final BoAdminRoleRepository roleRepo;
    private final PasswordPolicy passwordPolicy;
    private final AdminRolePermissionService adminRolePermissionService;
    private final BackofficeAuthService authService;

    public List<AdminRoleDto> listRoles() {
        return adminRolePermissionService.listRoles();
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

        BoAdminUser u = new BoAdminUser();
        u.setEmail(req.email().trim().toLowerCase());
        u.setFullName(req.fullName().trim());
        u.setLockedUntil(null);
        u.setMfaEnabled(false);

        if (!req.password().equals(req.confirmPassword())) {
            throw new IllegalArgumentException("Confirm password not same as password!");
        }

        BaseResponse getPol = passwordPolicy.validate(req.password());

        if (getPol.getStatusCode() != 200) {
            throw new IllegalArgumentException(getPol.getDescription());
        }

        u.setPasswordHash(passwordEncoder.encode(req.password()));
        u.setRoles(new HashSet<>(req.roles()));

        u = userRepo.save(u);

        auditService.audit("ADMIN_CREATE", actorAdminId, u.getId(), ip, ua,
                Map.of("email", u.getEmail(), "roles", u.getRoles()));

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
    public AdminPasswordResetResponse triggerPasswordReset(Long actorAdminId, Long adminId, String ip, String ua) {
        BoAdminUser u = userRepo.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin user not found"));

        String tempPassword = PasswordPolicy.generateTempPassword(14);
        authService.resetPasswordByAdmin(u, tempPassword);

        auditService.audit("ADMIN_PASSWORD_RESET", actorAdminId, u.getId(), ip, ua,
                Map.of("email", u.getEmail()));

        return new AdminPasswordResetResponse(
                u.getId(),
                u.getEmail(),
                tempPassword,
                "Temporary password generated successfully. Share it with the admin user over a secure channel and require them to change it immediately after login."
        );
    }

    private AdminUserResponse toResponse(BoAdminUser u) {
        return new AdminUserResponse(u.getId(), u.getEmail(), u.getFullName(), u.getStatus(), u.isMfaEnabled(), u.getRoles());
    }

    @Transactional(readOnly = true)
    public AdminUserResponse getAdmin(Long actorAdminId, Long adminId, String ip, String ua) {

        BoAdminUser u = userRepo.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("Admin user not found"));

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

        int p = Math.max(page, 0);
        int s = Math.min(Math.max(size, 1), 100);

        PageRequest pr = PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "id"));

        String query = (q == null ? null : q.trim());
        boolean hasQ = (query != null && !query.isEmpty());

        Page<BoAdminUser> result = hasQ
                ? userRepo.findByEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(query, query, pr)
                : userRepo.findAll(pr);

        Map<String, Object> meta = new HashMap<>();
        meta.put("page", p);
        meta.put("size", s);
        if (hasQ) {
            meta.put("q", query);
        }

        safeAuditAdminList(actorAdminId, ip, ua, meta);

        return result.map(this::toResponse);
    }

    private void safeAuditAdminList(Long actorAdminId, String ip, String ua, Map<String, Object> meta) {
        try {
            auditService.audit("ADMIN_LIST", actorAdminId, null, ip, ua, meta);
        } catch (Exception e) {
        }
    }
}
