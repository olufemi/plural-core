package com.financial.wealth.api.transactions.proxies;

import com.financial.wealth.api.transactions.breezepay.payout.NameEnquiryReq;
import com.financial.wealth.api.transactions.breezepay.payout.NameEnquiryResponse;
import com.financial.wealth.api.transactions.breezepay.payout.NipCreditAccountTransferRequest;
import com.financial.wealth.api.transactions.breezepay.payout.NipCreditTransferResponse;
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
@FeignClient(name = "breezePayVirtAcctProxy", url = "${fin.wealth.breeze.wallet.pay.base.url}")
public interface BreezePayVirtAcctProxy {

    @RequestMapping(value = "/virtualpayapi/NameEnquiry", consumes = "application/json", method = RequestMethod.POST)
    public NameEnquiryResponse nameEnquiry(@RequestBody NameEnquiryReq rq,
            @RequestHeader("Authorization") String auth, @RequestHeader("Ocp-Apim-Subscription-Key") String subKey);

    @RequestMapping(value = "/virtualpayapi/MakePayment", consumes = "application/json", method = RequestMethod.POST)
    public NipCreditTransferResponse makePayment(@RequestBody NipCreditAccountTransferRequest rq,
            @RequestHeader("Authorization") String auth, @RequestHeader("Ocp-Apim-Subscription-Key") String subKey);

}
