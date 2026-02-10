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
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

//@RestController
//@RequestMapping("/bo/auth")
@RestController
@RequestMapping({"/bo/auth", "/auth"})
@RequiredArgsConstructor
public class AuthController {

    private final BackofficeAuthService authService;
    private final JwtService jwtService;
    private final TotpService totpService;
    private final BoAdminUserRepository userRepo;
    private final CryptoBox cryptoBox;
    private final BoAdminRoleRepository roleRepo;

    private final BoMfaChallengeRepository mfaChallengeRepo;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        BoAdminUser user = authService.validatePasswordOrThrow(req.getEmail(), req.getPassword());

        boolean mfaProperlyEnabled
                = user.isMfaEnabled()
                && user.getTotpSecretEnc() != null
                && user.getTotpSecretIv() != null;

        // If DB was toggled wrongly: enabled=true but secret missing → force setup
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
            return ResponseEntity.ok(new TokenResponse(access, refresh, email, fullName, userRoleName));
        }

        String challengeId = authService.createMfaChallenge(user.getId());
        return ResponseEntity.ok(new LoginStep1Response("MFA_REQUIRED", challengeId));
    }

    //@PostMapping("/login")
    public ResponseEntity<?> loginOld(@RequestBody LoginRequest req) {
        BoAdminUser user = authService.validatePasswordOrThrow(req.getEmail(), req.getPassword());

        if (!user.isMfaEnabled()) {
            String access = jwtService.issueAccessToken(user);
            String refresh = authService.issueRefreshToken(user);
            //return ResponseEntity.ok(new TokenResponse(access, refresh));
        }

        String mfaToken = Base64.getEncoder().encodeToString(
                (user.getId() + ":" + Instant.now().getEpochSecond()).getBytes(StandardCharsets.UTF_8)
        );
        return ResponseEntity.ok(new LoginStep1Response("MFA_REQUIRED", mfaToken));
    }

    @PostMapping("/mfa/verify")
    public ResponseEntity<TokenResponse> verifyMfa(@RequestBody LoginStep2Request req) {

        BoAdminUser user = authService.verifyMfaOrThrow(req.getChallengeId(), req.getCode());

        String access = jwtService.issueAccessToken(user);
        String refresh = authService.issueRefreshToken(user);
        String email = user.getEmail();
        String fullName = user.getFullName();
        Optional<BoAdminRole> getRoleName = roleRepo.findById(user.getId());

        String userRoleName = getRoleName.get().getName();

        return ResponseEntity.ok(new TokenResponse(access, refresh, email, fullName, userRoleName));
    }

    // @PostMapping("/mfa/verify")
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

        return ResponseEntity.ok(new TokenResponse(access, refresh, email, fullName, userRoleName));
    }

    @PostMapping("/refresh-old")
    public ResponseEntity<TokenResponse> refreshOld(@RequestParam("refreshToken") String refreshToken) {
        BoAdminUser user = authService.validateRefreshOrThrow(refreshToken);
        String access = jwtService.issueAccessToken(user);
        String email = user.getEmail();
        String fullName = user.getFullName();
        Optional<BoAdminRole> getRoleName = roleRepo.findById(user.getId());

        String userRoleName = getRoleName.get().getName();

        return ResponseEntity.ok(new TokenResponse(access, refreshToken, email, fullName, userRoleName));
    }

    @Transactional(readOnly = true)
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestParam("refreshToken") String refreshToken) {
        BoAdminUser user = authService.validateRefreshOrThrow(refreshToken);

        String access = jwtService.issueAccessToken(user);

        // role name(s)
        String userRoleName = user.getRoles().stream()
                .findFirst()
                .map(BoAdminRole::getName)
                .orElse(null);

        return ResponseEntity.ok(new TokenResponse(
                access,
                refreshToken,
                user.getEmail(),
                user.getFullName(),
                userRoleName
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestParam("refreshToken") String refreshToken) {
        authService.revokeRefresh(refreshToken);
        return ResponseEntity.ok().build();
    }
}
