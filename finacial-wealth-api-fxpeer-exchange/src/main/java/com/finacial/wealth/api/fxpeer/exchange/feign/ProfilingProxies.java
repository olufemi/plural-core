/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.feign;

import com.finacial.wealth.api.fxpeer.exchange.model.AddAccountObj;
import com.finacial.wealth.api.fxpeer.exchange.model.ApiResponseModel;
import com.finacial.wealth.api.fxpeer.exchange.model.BaseResponse;
import com.finacial.wealth.api.fxpeer.exchange.model.ValidateCountryCode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author olufemioshin
 */
@FeignClient(name = "profiling-service")
public interface ProfilingProxies {

    @RequestMapping(value = "/walletmgt/add-other-currency-account", consumes = "application/json", method = RequestMethod.POST)
    public BaseResponse addOtherAccount(@RequestBody AddAccountObj rq, @RequestHeader("Authorization") String authorization);

    @RequestMapping(value = "/countries/validate/country-code", consumes = "application/json", method = RequestMethod.POST)
    public BaseResponse validateCountryCode(@RequestBody ValidateCountryCode rq, @RequestHeader("Authorization") String authorization);

}
