/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.controllers;

import com.financial.wealth.api.transactions.models.BaseResponse;
import com.financial.wealth.api.transactions.models.CreditWalletCaller;
import com.financial.wealth.api.transactions.models.DebitWalletCaller;
import com.financial.wealth.api.transactions.models.LeaveGroupRequest;
import com.financial.wealth.api.transactions.services.fx.p2.p2.wallet.ManageWalletService;
import com.financial.wealth.api.transactions.services.fx.p2.p2.wallet.WalletInfoValAcct;
import com.financial.wealth.api.transactions.services.fx.p2.p2.wallet.WalletInfoValiAcctBal;
import com.financial.wealth.api.transactions.utils.UttilityMethods;
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
    private final UttilityMethods utilMeth;

    @PostMapping("/create-offer-validate-account")
    public ResponseEntity<BaseResponse> createofferValidateAccount(
            @RequestBody @Valid WalletInfoValAcct rq) {

        BaseResponse baseResponse = manageWalletService.createofferValidateAccount(rq);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/validate-balance")
    public ResponseEntity<BaseResponse> validateAccountBalnce(
            @RequestBody @Valid WalletInfoValiAcctBal rq) {

        BaseResponse baseResponse = manageWalletService.validateAccountBalnce(rq);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/debit-customer-with-type")
    public ResponseEntity<BaseResponse> debitCustomerWithType(@RequestHeader(value = "user-type", required = true) String userType,
            @RequestBody @Valid DebitWalletCaller rq) {

        BaseResponse baseResponse = utilMeth.debitCustomerWithType(rq, userType, "");
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/credit-customer-with-type")
    public ResponseEntity<BaseResponse> creditCustomerWithType(@RequestHeader(value = "user-type", required = true) String userType,
            @RequestBody @Valid CreditWalletCaller rq) {

        BaseResponse baseResponse = utilMeth.creditCustomerWithType(rq, userType);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

}
