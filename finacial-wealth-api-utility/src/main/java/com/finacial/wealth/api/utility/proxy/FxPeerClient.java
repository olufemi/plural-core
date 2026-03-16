/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.sessionmanager.proxy;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.sessionmanager.request.DeviceBindingResponse;
import com.finacial.wealth.api.sessionmanager.request.DeviceLoginKeyRequest;
import com.finacial.wealth.api.sessionmanager.request.UserDeviceRequest;
import com.finacial.wealth.api.sessionmanager.response.ReceiptKeysResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "fxpeer-exchange-service", path = "/canonical/crypto")
public interface FxPeerClient {

    @GetMapping("/keys")
    ReceiptKeysResponse getReceiptKeys();

    @RequestMapping(value = "/login-key", consumes = "application/json", method = RequestMethod.POST)
    public DeviceBindingResponse loginKey(@RequestBody DeviceLoginKeyRequest rq);

}
