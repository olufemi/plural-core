/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.controllers;

import com.financial.wealth.api.transactions.models.BaseResponse;
import com.financial.wealth.api.transactions.models.LeaveGroupRequest;
import com.financial.wealth.api.transactions.services.fx.p2.p2.wallet.ManageWalletService;
import com.financial.wealth.api.transactions.services.fx.p2.p2.wallet.WalletInfoValAcct;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/peer-to-peer")
@RequiredArgsConstructor
public class FXPeerToPeerController {

    private final ManageWalletService manageWalletService;

    @PostMapping("/create-offer-validate-account")
    public ResponseEntity<BaseResponse> createofferValidateAccount(
            @RequestBody @Valid WalletInfoValAcct rq) {

        BaseResponse baseResponse = manageWalletService.createofferValidateAccount(rq);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

}
