/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.feign;

import com.finacial.wealth.api.fxpeer.exchange.model.BaseResponse;
import com.finacial.wealth.api.fxpeer.exchange.model.CreditWalletCaller;
import com.finacial.wealth.api.fxpeer.exchange.model.DebitWalletCaller;
import com.finacial.wealth.api.fxpeer.exchange.model.WalletNo;
import com.finacial.wealth.api.fxpeer.exchange.offer.WalletInfoValAcct;
import com.finacial.wealth.api.fxpeer.exchange.order.WalletInfoValiAcctBal;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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

    @RequestMapping(value = "/peer-to-peer/validate-balance", consumes = "application/json", method = RequestMethod.POST)
    public BaseResponse validateAccountBalnce(@RequestBody WalletInfoValiAcctBal rq);

    @RequestMapping(value = "/peer-to-peer/debit-customer-with-type", consumes = "application/json", method = RequestMethod.POST)
    public BaseResponse debitCustomerWithType(@RequestBody DebitWalletCaller rq, @RequestHeader("user-type") String userType);

    @RequestMapping(value = "/peer-to-peer/credit-customer-with-type", consumes = "application/json", method = RequestMethod.POST)
    public BaseResponse creditCustomerWithType(@RequestBody CreditWalletCaller rq, @RequestHeader("user-type") String userType);

    //, @RequestHeader("Authorization") String auth
}
