/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.profiling.proxies;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.profiling.models.DeviceBindingResponse;
import com.finacial.wealth.api.profiling.models.DeviceConfirmOtp;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "fxpeer-exchange-service", path = "/canonical/crypto")
public interface FxPeerClient {


    @RequestMapping(value = "/bind/confirm-otp", consumes = "application/json", method = RequestMethod.POST)
    public DeviceBindingResponse checkIfDeviceBelongsToUser(@RequestBody DeviceConfirmOtp rq);

}
