/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.utility.controllers;

import com.finacial.wealth.api.utility.models.AuthUserRequestCustomerUuid;
import com.finacial.wealth.api.utility.models.CreditWalletCaller;
import com.finacial.wealth.api.utility.models.DebitWalletCaller;
import com.finacial.wealth.api.utility.models.GetActBalPhoneNumber;
import com.finacial.wealth.api.utility.models.GetBalanceReq;
import com.finacial.wealth.api.utility.response.BaseResponse;
import com.finacial.wealth.api.utility.services.UtilityService;
import com.finacial.wealth.api.utility.services.WalletSystemProxyService;
import java.net.MalformedURLException;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author olufemioshin
 */
@RestController
@RequiredArgsConstructor
public class UtilitiesControllers {

    private final UtilityService generalServices;
    private final WalletSystemProxyService walletSystemProxyService;

    @PostMapping("/walletmgt/user/uuid")
    public ResponseEntity<BaseResponse> authUserUuid(
            @RequestHeader(value = "channel", required = true) String channel, @RequestBody @Valid AuthUserRequestCustomerUuid rq) {

        BaseResponse baseResponse = generalServices.authenticateUserCustomerUuid(rq, channel);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/walletmgt/account/balance")
    public ResponseEntity<BaseResponse> getAccountBalanceCaller(
            @RequestBody @Valid GetBalanceReq rq) {

        BaseResponse baseResponse = walletSystemProxyService.getAccountBalanceCaller(rq.getAuth());
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/walletmgt/account/debit-Wallet")
    public ResponseEntity<BaseResponse> debitWalletPayOutCaller(@RequestBody @Valid DebitWalletCaller rq) throws MalformedURLException {

        BaseResponse baseResponse = walletSystemProxyService.debitWalletPayOutCaller(rq);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/walletmgt/account/debit-Wallet-phone")
    public ResponseEntity<BaseResponse> debitWalletPayOutCallerPhone(@RequestBody @Valid DebitWalletCaller rq) throws MalformedURLException {

        BaseResponse baseResponse = walletSystemProxyService.debitWalletPayOutCallerPhone(rq);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/walletmgt/account/credit-Wallet")
    public ResponseEntity<BaseResponse> creditWalletCaller(@RequestBody @Valid CreditWalletCaller rq) throws MalformedURLException {

        BaseResponse baseResponse = walletSystemProxyService.creditWalletCaller(rq);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/walletmgt/account/credit-Wallet-phone")
    public ResponseEntity<BaseResponse> creditWalletCallerPhone(@RequestBody @Valid CreditWalletCaller rq) throws MalformedURLException {

        BaseResponse baseResponse = walletSystemProxyService.creditWalletCallerPhn(rq);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/get-account-bal-phone")
    public ResponseEntity<BaseResponse> getActBal(
            @RequestBody @Valid GetActBalPhoneNumber rq) {

        BaseResponse baseResponse = walletSystemProxyService.getAccountBalanceCallerPhoneNumber(rq.getPhoneNumber());
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

}
