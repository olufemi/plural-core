package com.finacial.wealth.api.profiling.proxies;

import com.finacial.wealth.api.profiling.breezpay.virt.create.acct.GenerateVirtualAccountNumResponse;
import com.finacial.wealth.api.profiling.breezpay.virt.create.acct.GenerateVirtualAccountNumberReq;
import com.finacial.wealth.api.profiling.breezpay.virt.acct.details.GetCusmerDetailsRequest;
import com.finacial.wealth.api.profiling.breezpay.virt.acct.details.GetCusmerDetailsResponse;
import com.finacial.wealth.api.profiling.breezpay.virt.get.acct.list.CustomerResponse;
import com.finacial.wealth.api.profiling.breezpay.virt.get.acct.list.GetAcctListReq;
import com.finacial.wealth.api.profiling.breezpay.virt.get.bvn.GetSingleBvnResponse;
import com.finacial.wealth.api.profiling.breezpay.virt.get.bvn.ValidateSingleBvnReq;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
/**
 *
 * @author olufemioshin
 */
@FeignClient(name = "breezePayVirtApiDevAcctProxy", url = "${fin.wealth.breeze.api.dev.url}")
public interface BreezePayVirtApiDevAcctProxy {

    @RequestMapping(value = "/GenerateVirtualAccountNumber", consumes = "application/json", method = RequestMethod.POST)
    public GenerateVirtualAccountNumResponse generateVirtualAccount(@RequestBody GenerateVirtualAccountNumberReq rq,
            @RequestHeader("Authorization") String auth, @RequestHeader("Ocp-Apim-Subscription-Key") String subKey);

    @RequestMapping(value = "/FetchCustomerDetails", consumes = "application/json", method = RequestMethod.POST)
    public GetCusmerDetailsResponse getCusDetails(@RequestBody GetCusmerDetailsRequest rq,
            @RequestHeader("Authorization") String auth, @RequestHeader("Ocp-Apim-Subscription-Key") String subKey);

    @RequestMapping(value = "/GetCustomers", consumes = "application/json", method = RequestMethod.POST)
    public CustomerResponse getCustomers(@RequestBody GetAcctListReq rq,
            @RequestHeader("Authorization") String auth, @RequestHeader("Ocp-Apim-Subscription-Key") String subKey);

    @RequestMapping(value = "/bvn/v1/VerifySingleBVN", consumes = "application/json", method = RequestMethod.POST)
    public GetSingleBvnResponse VerifySingleBVN(@RequestBody ValidateSingleBvnReq rq,
            @RequestHeader("Authorization") String auth, @RequestHeader("Ocp-Apim-Subscription-Key") String subKey);

}
