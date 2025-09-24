/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.finacial.wealth.api.profiling.controllers;

import com.finacial.wealth.api.profiling.models.GetActBalPhoneNumber;
import com.finacial.wealth.api.profiling.models.GetActBalReq;
import com.finacial.wealth.api.profiling.response.BaseResponse;
import com.finacial.wealth.api.profiling.services.WalletSystemProxyService;
import javax.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author olufemioshin
 */
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
@Validated
public class InternalWalletController {

    private final WalletSystemProxyService walletSystemProxyService;

    @PostMapping("/get-account-bal")
    public ResponseEntity<BaseResponse> getActBal(
            // @RequestHeader(value = "channel", required = true) String channel,
            @RequestBody @Valid GetActBalPhoneNumber rq) {

        BaseResponse baseResponse = walletSystemProxyService.getAccountBalanceCaller(rq.getPhoneNumber());
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/get-account-bal-phone")
    public ResponseEntity<BaseResponse> getActBalPhn(
            @RequestBody @Valid GetActBalPhoneNumber rq) {

        BaseResponse baseResponse = walletSystemProxyService.getAccountBalanceCallerPhoneNumber(rq.getPhoneNumber());
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }
}
