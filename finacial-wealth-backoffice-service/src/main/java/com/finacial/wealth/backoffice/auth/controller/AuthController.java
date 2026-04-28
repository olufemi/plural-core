package com.finacial.wealth.backoffice.auth.controller;

import com.finacial.wealth.backoffice.auth.dto.*;
import com.finacial.wealth.backoffice.auth.entity.BoAdminRole;
import com.finacial.wealth.backoffice.auth.entity.BoAdminUser;
import com.finacial.wealth.backoffice.auth.repo.BoAdminRoleRepository;
import com.finacial.wealth.backoffice.auth.repo.BoAdminUserRepository;
import com.finacial.wealth.backoffice.auth.repo.BoMfaChallengeRepository;
import com.finacial.wealth.backoffice.auth.service.BackofficeAuthService;
import com.finacial.wealth.backoffice.auth.service.JwtService;
import com.finacial.wealth.backoffice.auth.service.TotpService;
import com.finacial.wealth.backoffice.util.CryptoBox;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@RestController
@RequestMapping({"/bo/auth", "/auth"})
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Backoffice login, MFA, token refresh, and password recovery endpoints.")
public class AuthController {

    private final BackofficeAuthService authService;
    private final JwtService jwtService;
    private final TotpService totpService;
    private final BoAdminUserRepository userRepo;
    private final CryptoBox cryptoBox;
    private final BoAdminRoleRepository roleRepo;

    private final BoMfaChallengeRepository mfaChallengeRepo;

    @Operation(summary = "Login admin user")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        BoAdminUser user = authService.validatePasswordOrThrow(req.getEmail(), req.getPassword());

        boolean mfaProperlyEnabled
                = user.isMfaEnabled()
                && user.getTotpSecretEnc() != null
                && user.getTotpSecretIv() != null;

        if (user.isMfaEnabled() && !mfaProperlyEnabled) {
            return ResponseEntity.ok(new LoginStep1Response("MFA_SETUP_REQUIRED", null));
        }

        if (!mfaProperlyEnabled) {
            String access = jwtService.issueAccessToken(user);
            String refresh = authService.issueRefreshToken(user);
            String email = user.getEmail();
            String fullName = user.getFullName();
            Optional<BoAdminRole> getRoleName = roleRepo.findById(user.getId());

            String userRoleName = getRoleName.get().getName();
            return ResponseEntity.ok(new TokenResponse(access, refresh, email, fullName, userRoleName, user.getId()));
        }

        String challengeId = authService.createMfaChallenge(user.getId());
        return ResponseEntity.ok(new LoginStep1Response("MFA_REQUIRED", challengeId));
    }

    public ResponseEntity<?> loginOld(@RequestBody LoginRequest req) {
        BoAdminUser user = authService.validatePasswordOrThrow(req.getEmail(), req.getPassword());

        if (!user.isMfaEnabled()) {
            String access = jwtService.issueAccessToken(user);
            String refresh = authService.issueRefreshToken(user);
        }

        String mfaToken = Base64.getEncoder().encodeToString(
                (user.getId() + ":" + Instant.now().getEpochSecond()).getBytes(StandardCharsets.UTF_8)
        );
        return ResponseEntity.ok(new LoginStep1Response("MFA_REQUIRED", mfaToken));
    }

    @Operation(summary = "Verify MFA code")
    @PostMapping("/mfa/verify")
    public ResponseEntity<TokenResponse> verifyMfa(@RequestBody LoginStep2Request req) {

        BoAdminUser user = authService.verifyMfaOrThrow(req.getChallengeId(), req.getCode());

        String access = jwtService.issueAccessToken(user);
        String refresh = authService.issueRefreshToken(user);
        String email = user.getEmail();
        String fullName = user.getFullName();
        Optional<BoAdminRole> getRoleName = roleRepo.findById(user.getId());

        String userRoleName = getRoleName.get().getName();

        return ResponseEntity.ok(new TokenResponse(access, refresh, email, fullName, userRoleName, user.getId()));
    }

    public ResponseEntity<TokenResponse> verifyMfaOld(@RequestBody MfaVerifyRequest req) {
        String decoded = new String(Base64.getDecoder().decode(req.getMfaToken()), StandardCharsets.UTF_8);
        Long userId = Long.valueOf(decoded.split(":")[0]);

        BoAdminUser user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid MFA token"));

        if (!user.isMfaEnabled() || user.getTotpSecretEnc() == null || user.getTotpSecretIv() == null) {
            throw new IllegalStateException("MFA not enrolled");
        }

        String secret = cryptoBox.decrypt(user.getTotpSecretEnc(), user.getTotpSecretIv());
        if (!totpService.verifyCode(secret, req.getTotpCode())) {
            throw new IllegalArgumentException("Invalid TOTP");
        }

        String access = jwtService.issueAccessToken(user);
        String refresh = authService.issueRefreshToken(user);
        String email = user.getEmail();
        String fullName = user.getFullName();
        Optional<BoAdminRole> getRoleName = roleRepo.findById(user.getId());

        String userRoleName = getRoleName.get().getName();

        return ResponseEntity.ok(new TokenResponse(access, refresh, email, fullName, userRoleName, user.getId()));
    }

    @Operation(summary = "Refresh access token")
    @PostMapping("/refresh-old")
    public ResponseEntity<TokenResponse> refreshOld(@RequestParam("refreshToken") String refreshToken) {
        BoAdminUser user = authService.validateRefreshOrThrow(refreshToken);
        String access = jwtService.issueAccessToken(user);
        String email = user.getEmail();
        String fullName = user.getFullName();
        Optional<BoAdminRole> getRoleName = roleRepo.findById(user.getId());

        String userRoleName = getRoleName.get().getName();

        return ResponseEntity.ok(new TokenResponse(access, refreshToken, email, fullName, userRoleName, user.getId()));
    }

    @Transactional(readOnly = true)
    @Operation(summary = "Refresh access token")
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestParam("refreshToken") String refreshToken) {
        BoAdminUser user = authService.validateRefreshOrThrow(refreshToken);

        String access = jwtService.issueAccessToken(user);

        String userRoleName = user.getRoles().stream()
                .findFirst()
                .map(BoAdminRole::getName)
                .orElse(null);

        return ResponseEntity.ok(new TokenResponse(
                access,
                refreshToken,
                user.getEmail(),
                user.getFullName(),
                userRoleName, user.getId()
        ));
    }

    @Operation(summary = "Logout admin user")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestParam("refreshToken") String refreshToken) {
        authService.revokeRefresh(refreshToken);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Change password",
            description = "Allows a signed-in admin user to change their password using their current password.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/password/change")
    public ResponseEntity<Void> changePassword(
            @RequestAttribute("boAdminUserId") Long adminUserId,
            @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest
    ) {
        authService.changePassword(adminUserId, request.currentPassword(), request.newPassword(), request.confirmPassword(), ip(httpRequest), ua(httpRequest));
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Start password recovery",
            description = "Starts backoffice password recovery. If the account has MFA enrolled, the response includes a one-time challenge id for the next recovery step."
    )
    @PostMapping("/password/recovery/start")
    public ResponseEntity<PasswordRecoveryStartResponse> startPasswordRecovery(
            @RequestBody PasswordRecoveryStartRequest request,
            HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(authService.startPasswordRecovery(request.email(), ip(httpRequest), ua(httpRequest)));
    }

    @Operation(
            summary = "Complete password recovery",
            description = "Completes password recovery using the challenge id from the start step, a valid TOTP code, and the new password."
    )
    @PostMapping("/password/recovery/complete")
    public ResponseEntity<Void> completePasswordRecovery(
            @RequestBody PasswordRecoveryCompleteRequest request,
            HttpServletRequest httpRequest
    ) {
        authService.completePasswordRecovery(request, ip(httpRequest), ua(httpRequest));
        return ResponseEntity.ok().build();
    }

    private String ip(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        return (xf != null && !xf.isBlank()) ? xf.split(",")[0].trim() : request.getRemoteAddr();
    }

    private String ua(HttpServletRequest request) {
        String value = request.getHeader("User-Agent");
        return value == null ? "" : value;
    }
}
