/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.financial.wealth.api.transactions.proxies;

import com.financial.wealth.api.transactions.models.ApiResponseModel;
import com.financial.wealth.api.transactions.models.BaseResponse;
import com.financial.wealth.api.transactions.models.ValidateCountryCode;
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

   
    @RequestMapping(value = "/countries/validate/country-code", consumes = "application/json", method = RequestMethod.POST)
    public ApiResponseModel validateCountryCode(@RequestBody ValidateCountryCode rq, @RequestHeader("Authorization") String authorization);

}
