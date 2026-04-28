package com.finacial.wealth.backoffice.auth.service;

import com.finacial.wealth.backoffice.PasswordPolicy;
import com.finacial.wealth.backoffice.auth.dto.PasswordRecoveryCompleteRequest;
import com.finacial.wealth.backoffice.auth.dto.PasswordRecoveryStartResponse;
import com.finacial.wealth.backoffice.auth.entity.BoAdminUser;
import com.finacial.wealth.backoffice.auth.entity.BoMfaChallenge;
import com.finacial.wealth.backoffice.auth.entity.BoRefreshToken;
import com.finacial.wealth.backoffice.auth.repo.BoAdminUserRepository;
import com.finacial.wealth.backoffice.auth.repo.BoMfaChallengeRepository;
import com.finacial.wealth.backoffice.auth.repo.BoRefreshTokenRepository;
import com.finacial.wealth.backoffice.model.BaseResponse;
import com.finacial.wealth.backoffice.util.CryptoBox;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BackofficeAuthService {

    private final BoAdminUserRepository userRepo;
    private final BoRefreshTokenRepository refreshRepo;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicy passwordPolicy;
    private final TotpService totpService;
    private final CryptoBox cryptoBox;
    private final BoMfaChallengeRepository mfaChallengeRepo;
    private final AdminAuditService auditService;

    @Value("${bo.security.max-failed-attempts}")
    private int maxFailed;

    @Value("${bo.security.lock-minutes}")
    private int lockMinutes;

    @Value("${bo.jwt.refresh-ttl-days}")
    private int refreshTtlDays;

    public BoAdminUser validatePasswordOrThrow(String email, String rawPassword) {
        BoAdminUser user = userRepo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (user.getStatus() != BoAdminUser.Status.ACTIVE) {
            throw new IllegalStateException("User is not active");
        }

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Account locked. Try later.");
        }

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            int next = user.getFailedAttempts() + 1;
            user.setFailedAttempts(next);

            if (next >= maxFailed) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(lockMinutes));
                user.setFailedAttempts(0);
            }
            userRepo.save(user);
            throw new IllegalArgumentException("Invalid credentials");
        }

        user.setFailedAttempts(0);
        user.setLastLoginAt(LocalDateTime.now());
        userRepo.save(user);
        return user;
    }

    @Transactional
    public void changePassword(Long adminUserId, String currentPassword, String newPassword, String confirmPassword, String ip, String ua) {
        BoAdminUser user = userRepo.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        validateNewPassword(newPassword, confirmPassword);

        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("New password must be different from current password");
        }

        applyNewPassword(user, newPassword);
        auditService.audit("PASSWORD_CHANGE", adminUserId, user.getId(), ip, ua, Map.of("email", user.getEmail()));
    }

    @Transactional
    public PasswordRecoveryStartResponse startPasswordRecovery(String email, String ip, String ua) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("email is required");
        }

        String normalizedEmail = email.trim().toLowerCase();
        BoAdminUser user = userRepo.findByEmailIgnoreCase(normalizedEmail).orElse(null);

        if (user == null || user.getStatus() != BoAdminUser.Status.ACTIVE) {
            auditService.audit("PASSWORD_RECOVERY_REQUEST", null, null, ip, ua, Map.of(
                    "email", normalizedEmail,
                    "outcome", "CONTACT_SUPER_ADMIN"
            ));
            return new PasswordRecoveryStartResponse(
                    "CONTACT_SUPER_ADMIN",
                    null,
                    maskEmail(normalizedEmail),
                    "Password recovery requires an active account with MFA enabled. Contact a super admin if you cannot continue."
            );
        }

        boolean mfaReady = user.isMfaEnabled()
                && user.getTotpSecretEnc() != null
                && user.getTotpSecretIv() != null;

        if (!mfaReady) {
            auditService.audit("PASSWORD_RECOVERY_REQUEST", null, user.getId(), ip, ua, Map.of(
                    "email", user.getEmail(),
                    "outcome", "CONTACT_SUPER_ADMIN"
            ));
            return new PasswordRecoveryStartResponse(
                    "CONTACT_SUPER_ADMIN",
                    null,
                    maskEmail(user.getEmail()),
                    "MFA recovery is not available for this account yet. Contact a super admin for a manual reset."
            );
        }

        String challengeId = createMfaChallenge(user.getId());
        auditService.audit("PASSWORD_RECOVERY_REQUEST", null, user.getId(), ip, ua, Map.of(
                "email", user.getEmail(),
                "outcome", "MFA_REQUIRED"
        ));

        return new PasswordRecoveryStartResponse(
                "MFA_REQUIRED",
                challengeId,
                maskEmail(user.getEmail()),
                "Enter the 6-digit code from your authenticator app to complete password recovery."
        );
    }

    @Transactional
    public void resetPasswordByAdmin(BoAdminUser user, String rawPassword) {
        validateNewPassword(rawPassword, rawPassword);
        applyNewPassword(user, rawPassword);
    }

    @Transactional
    public void completePasswordRecovery(PasswordRecoveryCompleteRequest request, String ip, String ua) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }

        validateNewPassword(request.newPassword(), request.confirmPassword());

        BoAdminUser user = verifyMfaOrThrow(request.challengeId(), request.code());

        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("New password must be different from current password");
        }

        applyNewPassword(user, request.newPassword());
        auditService.audit("PASSWORD_RECOVERY_COMPLETE", null, user.getId(), ip, ua, Map.of("email", user.getEmail()));
    }

    public String issueRefreshToken(BoAdminUser user) {
        String raw = UUID.randomUUID().toString() + "." + UUID.randomUUID();
        String hash = sha256Base64(raw);

        BoRefreshToken rt = BoRefreshToken.builder()
                .adminUser(user)
                .tokenHash(hash)
                .expiresAt(LocalDateTime.now().plusDays(refreshTtlDays))
                .revoked(false)
                .build();
        refreshRepo.save(rt);
        return raw;
    }

    public BoAdminUser validateRefreshOrThrow(String rawRefresh) {
        String hash = sha256Base64(rawRefresh);
        BoRefreshToken rt = refreshRepo.findByTokenHashAndRevokedFalse(hash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (rt.getExpiresAt().isBefore(LocalDateTime.now())) {
            rt.setRevoked(true);
            refreshRepo.save(rt);
            throw new IllegalArgumentException("Refresh token expired");
        }
        return rt.getAdminUser();
    }

    public void revokeRefresh(String rawRefresh) {
        String hash = sha256Base64(rawRefresh);
        refreshRepo.findByTokenHashAndRevokedFalse(hash).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshRepo.save(rt);
        });
    }

    public String hashPassword(String raw) {
        return passwordEncoder.encode(raw);
    }

    private static String sha256Base64(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(s.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(dig);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private String generateChallengeId() {
        byte[] b = new byte[32];
        new java.security.SecureRandom().nextBytes(b);
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte x : b) {
            sb.append(String.format("%02x", x));
        }
        return sb.toString();
    }

    public String createMfaChallenge(Long userId) {
        BoMfaChallenge c = new BoMfaChallenge();
        c.setId(generateChallengeId());
        c.setUserId(userId);
        c.setCreatedAt(Instant.now());
        c.setExpiresAt(Instant.now().plusSeconds(300));
        c.setUsed(false);
        c.setAttempts(0);
        c.setMaxAttempts(5);

        mfaChallengeRepo.save(c);
        return c.getId();
    }

    @Transactional
    public BoAdminUser verifyMfaOrThrow(String challengeId, String code) {

        BoMfaChallenge ch = mfaChallengeRepo.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid challenge"));

        if (ch.isUsed() || Instant.now().isAfter(ch.getExpiresAt())) {
            throw new IllegalArgumentException("Challenge expired");
        }

        BoAdminUser user = userRepo.findById(ch.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String secret = cryptoBox.decrypt(user.getTotpSecretEnc(), user.getTotpSecretIv());
        if (!totpService.verifyCode(secret, code)) {
            ch.setAttempts(ch.getAttempts() + 1);
            if (ch.getAttempts() >= ch.getMaxAttempts()) {
                ch.setUsed(true);
            }
            mfaChallengeRepo.save(ch);
            throw new IllegalArgumentException("Invalid TOTP");
        }

        ch.setUsed(true);
        mfaChallengeRepo.save(ch);

        return user;
    }

    public BoAdminUser verifyMfaOrThrowNew(String challengeId, String code) {

        if (challengeId == null || challengeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing challengeId");
        }
        if (code == null || !code.matches("\\d{6}")) {
            throw new IllegalArgumentException("Invalid code format");
        }

        BoMfaChallenge c = mfaChallengeRepo.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Invalid challenge"));

        if (c.isUsed()) {
            throw new RuntimeException("Challenge already used");
        }
        if (c.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Challenge expired");
        }
        if (c.getAttempts() >= c.getMaxAttempts()) {
            c.setUsed(true);
            mfaChallengeRepo.save(c);
            throw new RuntimeException("Too many attempts");
        }

        BoAdminUser user = userRepo.findById(c.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getTotpSecretEnc() == null || user.getTotpSecretIv() == null) {
            throw new IllegalStateException("MFA secret not configured");
        }

        if (!user.isMfaEnabled() || user.getMfaSecret() == null || user.getMfaSecret().trim().isEmpty()) {
            throw new RuntimeException("MFA not configured");
        }

        boolean ok = totpService.verifyCode(user.getMfaSecret(), code);

        if (!ok) {
            c.setAttempts(c.getAttempts() + 1);
            mfaChallengeRepo.save(c);
            throw new RuntimeException("Invalid MFA code");
        }

        c.setUsed(true);
        mfaChallengeRepo.save(c);

        return user;
    }

    private void validateNewPassword(String newPassword, String confirmPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("newPassword is required");
        }
        if (confirmPassword == null || confirmPassword.isBlank()) {
            throw new IllegalArgumentException("confirmPassword is required");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Confirm password not same as password!");
        }

        BaseResponse policy = passwordPolicy.validate(newPassword);
        if (policy.getStatusCode() != 200) {
            throw new IllegalArgumentException(policy.getDescription());
        }
    }

    private void applyNewPassword(BoAdminUser user, String rawPassword) {
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        userRepo.save(user);
        revokeAllRefreshTokens(user.getId());
    }

    private void revokeAllRefreshTokens(Long adminUserId) {
        List<BoRefreshToken> tokens = refreshRepo.findAllByAdminUserIdAndRevokedFalse(adminUserId);
        if (tokens.isEmpty()) {
            return;
        }
        tokens.forEach(token -> token.setRevoked(true));
        refreshRepo.saveAll(tokens);
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        int at = email.indexOf('@');
        if (at <= 1) {
            return email;
        }
        return email.charAt(0) + "***" + email.substring(at - 1);
    }
}
