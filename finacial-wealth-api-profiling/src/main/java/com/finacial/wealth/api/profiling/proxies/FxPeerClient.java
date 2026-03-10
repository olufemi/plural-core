/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.profiling.proxies;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.profiling.models.ConsentVerifyRequest;
import com.finacial.wealth.api.profiling.models.DeviceBindingResponse;
import com.finacial.wealth.api.profiling.models.DeviceConfirmOtp;
import com.finacial.wealth.api.profiling.response.BaseResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "fxpeer-exchange-service")
public interface FxPeerClient {

    @RequestMapping(value = "/canonical/crypto/bind/confirm-otp", consumes = "application/json", method = RequestMethod.POST)
    public DeviceBindingResponse bindConfirmOtp(@RequestBody DeviceConfirmOtp rq);

    @PostMapping(
            value = "/canonical/crypto/consent/verify",
            consumes = "application/json"
    )
    BaseResponse verify(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("X-Device-Id") String deviceId,
            @RequestHeader("X-Device-Kid") String deviceKid,
            @RequestHeader("X-Consent-Ts") Long ts,
            @RequestHeader("X-Consent-Nonce") String nonce,
            @RequestHeader("X-Consent-Sig") String sigB64,
            @RequestBody ConsentVerifyRequest body
    );

}
