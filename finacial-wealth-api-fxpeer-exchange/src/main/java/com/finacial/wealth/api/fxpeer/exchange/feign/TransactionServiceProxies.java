/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.feign;

import com.finacial.wealth.api.fxpeer.exchange.model.BaseResponse;
import com.finacial.wealth.api.fxpeer.exchange.model.WalletNo;
import com.finacial.wealth.api.fxpeer.exchange.offer.WalletInfoValAcct;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * @author olufemioshin
 */
@FeignClient(name = "transactions-service")
public interface TransactionServiceProxies {

    @RequestMapping(value = "/interbank/validate-pin", consumes = "application/json", method = RequestMethod.POST)
    public BaseResponse validatePin(@RequestBody WalletNo rq);

    @RequestMapping(value = "/peer-to-peer/create-offer-validate-account", consumes = "application/json", method = RequestMethod.POST)
    public BaseResponse createOffervalidateAccount(@RequestBody WalletInfoValAcct rq);

}
