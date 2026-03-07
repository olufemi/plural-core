/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.controllers;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.fxpeer.exchange.model.BaseResponse;
import com.finacial.wealth.api.fxpeer.exchange.service.canonical.ConsentVerifierService;
import lombok.RequiredArgsConstructor;
import om.finacial.wealth.api.fxpeer.exchange.service.canonical.model.ConsentVerifyRequest;
import om.finacial.wealth.api.fxpeer.exchange.service.canonical.model.ConsentVerifyResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/canonical/crypto")
public class ConsentVerifyController {

    private final ConsentVerifierService verifier;

 
    @PostMapping("/consent/verify")
    public ResponseEntity<BaseResponse> verify(@RequestBody ConsentVerifyRequest body,
            @RequestHeader("X-Device-Id") String deviceId,
            @RequestHeader("X-Device-Kid") String kid,
            @RequestHeader(value = "X-Consent-Ts", required = false) Long ts,
            @RequestHeader("X-Consent-Nonce") String nonce,
            @RequestHeader("X-Consent-Sig") String sigB64) {

        BaseResponse res = verifier.verifyOrThrow(body, deviceId, kid, ts, nonce, sigB64);
        return ResponseEntity.status(res.getStatusCode()).body(res);
    }
}
