/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.controllers;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.service.canonical.DeviceKeyService;
import com.finacial.wealth.api.fxpeer.exchange.util.UttilityMethods;
import lombok.RequiredArgsConstructor;
import om.finacial.wealth.api.fxpeer.exchange.service.canonical.model.DeviceBindingResponse;
import om.finacial.wealth.api.fxpeer.exchange.service.canonical.model.DeviceConfirmOtp;
import om.finacial.wealth.api.fxpeer.exchange.service.canonical.model.DeviceLoginKeyRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/canonical/crypto")
public class DeviceBindingController {

    private final DeviceKeyService deviceKeyService;
    private final UttilityMethods utilService;

    // Call this immediately after login (or embed in login flow later)
    @PostMapping("/login-key")
    public DeviceBindingResponse upsertDeviceKey(@RequestBody DeviceLoginKeyRequest req) {
       //String userId = utilService.getClaimFromJwt(req.auth(), "emailAddress"); // preferred if your JWT has sellerId
       
       String userId = req.emailAddress();

        var r = deviceKeyService.upsertOnLogin(userId, req.deviceId(), req.devicePublicSpkiB64());
        return new DeviceBindingResponse(r.deviceId(), r.status(), r.activeKid());
    }

    // OTP confirm endpoint (OTP validation should happen before calling confirmOtpActivate)
    @PostMapping("/bind/confirm-otp")
    public DeviceBindingResponse confirmOtp(@RequestBody DeviceConfirmOtp req) {
        // Long userId = UserContext.requireUserId(); // replace
        String userId = req.emailAddress(); // preferred if your JWT has sellerId

        var r = deviceKeyService.confirmOtpActivate(userId, req.deviceId());
        return new DeviceBindingResponse(r.deviceId(), r.status(), r.activeKid());
    }
}
