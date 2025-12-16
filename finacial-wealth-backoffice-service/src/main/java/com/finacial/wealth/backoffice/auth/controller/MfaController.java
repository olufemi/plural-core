package com.finacial.wealth.backoffice.auth.controller;

import com.finacial.wealth.backoffice.auth.dto.MfaConfirmRequest;
import com.finacial.wealth.backoffice.auth.entity.BoAdminUser;
import com.finacial.wealth.backoffice.auth.repo.BoAdminUserRepository;
import com.finacial.wealth.backoffice.auth.service.TotpService;
import com.finacial.wealth.backoffice.util.CryptoBox;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bo/auth/mfa")
@RequiredArgsConstructor
public class MfaController {

    private final BoAdminUserRepository userRepo;
    private final TotpService totpService;
    private final CryptoBox cryptoBox;

    @Value("${bo.jwt.issuer}")
    private String issuer;

    @PostMapping("/setup")
    public ResponseEntity<MfaSetupResponse> setup(@RequestAttribute("boAdminUserId") Long adminUserId) {

        BoAdminUser user = userRepo.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String secret = totpService.generateSecret();
        var enc = cryptoBox.encrypt(secret);

        user.setTotpSecretEnc(enc.cipherTextBase64());
        user.setTotpSecretIv(enc.ivBase64());
        user.setMfaEnabled(false);
        userRepo.save(user);

        String qrDataUri = totpService.buildQrDataUri(user.getEmail(), issuer, secret);

        // ✅ Frontend displays QR + email label; app automatically uses it
        return ResponseEntity.ok(new MfaSetupResponse(qrDataUri, user.getEmail(), issuer));
    }

    //@PostMapping("/setup")
    public ResponseEntity<MfaSetupResponse> setupOld(@RequestAttribute("boAdminUserId") Long adminUserId) {
        BoAdminUser user = userRepo.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String secret = totpService.generateSecret();
        var enc = cryptoBox.encrypt(secret);

        user.setTotpSecretEnc(enc.cipherTextBase64());
        user.setTotpSecretIv(enc.ivBase64());
        user.setMfaEnabled(false);
        userRepo.save(user);

        String qrDataUri = totpService.buildQrDataUri(user.getEmail(), issuer, secret);
        return ResponseEntity.ok(new MfaSetupResponse(qrDataUri, secret));
    }

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirm(
            @RequestAttribute("boAdminUserId") Long adminUserId,
            @RequestBody MfaConfirmRequest req
    ) {
        BoAdminUser user = userRepo.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getTotpSecretEnc() == null || user.getTotpSecretIv() == null) {
            throw new IllegalStateException("MFA setup not started");
        }

        String code = req.code();
        if (code == null || !code.matches("\\d{6}")) {
            throw new IllegalArgumentException("Invalid code format");
        }

        String secret = cryptoBox.decrypt(user.getTotpSecretEnc(), user.getTotpSecretIv());
        if (!totpService.verifyCode(secret, code)) {
            throw new IllegalArgumentException("Invalid TOTP");
        }

        user.setMfaEnabled(true);
        userRepo.save(user);

        return ResponseEntity.ok().build();
    }

    //@PostMapping("/confirm")
    public ResponseEntity<Void> confirmOld(@RequestAttribute("boAdminUserId") Long adminUserId,
            @RequestParam("code") String code) {
        BoAdminUser user = userRepo.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getTotpSecretEnc() == null || user.getTotpSecretIv() == null) {
            throw new IllegalStateException("MFA setup not started");
        }

        String secret = cryptoBox.decrypt(user.getTotpSecretEnc(), user.getTotpSecretIv());
        if (!totpService.verifyCode(secret, code)) {
            throw new IllegalArgumentException("Invalid TOTP");
        }

        user.setMfaEnabled(true);
        userRepo.save(user);
        return ResponseEntity.ok().build();
    }

    @Data
    @AllArgsConstructor
    public static class MfaSetupResponse {

        private String qrDataUri;
        private String manualKey; // helpful fallback

        private MfaSetupResponse(String qrDataUri, String email, String suer) {
            throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
        }
    }
}
