package com.finacial.wealth.backoffice.auth.service;

import com.finacial.wealth.backoffice.auth.entity.BoAdminUser;
import com.finacial.wealth.backoffice.auth.entity.BoMfaChallenge;
import com.finacial.wealth.backoffice.auth.entity.BoRefreshToken;
import com.finacial.wealth.backoffice.auth.repo.BoAdminUserRepository;
import com.finacial.wealth.backoffice.auth.repo.BoMfaChallengeRepository;
import com.finacial.wealth.backoffice.auth.repo.BoRefreshTokenRepository;
import com.finacial.wealth.backoffice.util.CryptoBox;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BackofficeAuthService {

    private final BoAdminUserRepository userRepo;
    private final BoRefreshTokenRepository refreshRepo;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Value("${bo.security.max-failed-attempts}")
    private int maxFailed;

    @Value("${bo.security.lock-minutes}")
    private int lockMinutes;

    @Value("${bo.jwt.refresh-ttl-days}")
    private int refreshTtlDays;

    private final TotpService totpService;

    private final CryptoBox cryptoBox;

    private final BoMfaChallengeRepository mfaChallengeRepo;

    public BoAdminUser validatePasswordOrThrow(String email, String rawPassword) {
        BoAdminUser user = userRepo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (user.getStatus() != BoAdminUser.Status.ACTIVE) {
            throw new IllegalStateException("User is not active");
        }

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Account locked. Try later.");
        }

        if (!encoder.matches(rawPassword, user.getPasswordHash())) {
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
        return encoder.encode(raw);
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
        // 32 bytes => 64 hex chars
        byte[] b = new byte[32];
        new java.security.SecureRandom().nextBytes(b);
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (byte x : b) {
            sb.append(String.format("%02x", x));
        }
        return sb.toString();
    }

    public String createMfaChallenge(Long userId) {
        // invalidate old ones for same user (optional but helps prevent clutter/replay)
        // simplest: leave them; stronger: mark them used via query if you add one.

        BoMfaChallenge c = new BoMfaChallenge();
        c.setId(generateChallengeId());
        c.setUserId(userId);
        c.setCreatedAt(Instant.now());
        c.setExpiresAt(Instant.now().plusSeconds(300)); // 5 mins
        c.setUsed(false);
        c.setAttempts(0);
        c.setMaxAttempts(5);

        mfaChallengeRepo.save(c);
        return c.getId();
    }

    /*
    Rules:

challenge must exist

not expired

not used

attempts < maxAttempts

user must have MFA enabled + secret present

verify TOTP

if success: mark used=true (one-time)

if failure: attempts++ and throw 401; if exceeded, mark used=true or lock
     */
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

        if (!user.isMfaEnabled() || user.getMfaSecret() == null || user.getMfaSecret().trim().isEmpty()) {
            throw new RuntimeException("MFA not configured");
        }

        boolean ok = totpService.verifyCode(user.getMfaSecret(), code);

        if (!ok) {
            c.setAttempts(c.getAttempts() + 1);
            mfaChallengeRepo.save(c);
            throw new RuntimeException("Invalid MFA code");
        }

        // success: one-time use
        c.setUsed(true);
        mfaChallengeRepo.save(c);

        return user;
    }
}
