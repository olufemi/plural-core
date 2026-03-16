/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.utility.proxy;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.utility.models.DeviceBindingResponse;
import com.finacial.wealth.api.utility.models.DeviceLoginKeyRequest;
import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "fxpeer-exchange-service", path = "/canonical/crypto")
public interface FxPeerClient {

    @RequestMapping(value = "/login-key", consumes = "application/json", method = RequestMethod.POST)
    public DeviceBindingResponse loginKey(@RequestBody DeviceLoginKeyRequest rq);

}
