/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.financial.wealth.api.transactions.controllers;

import com.financial.wealth.api.transactions.breezepay.payout.NameLookUpInterBank;
import com.financial.wealth.api.transactions.breezepay.payout.NipBankService;
import com.financial.wealth.api.transactions.config.ApiClientException;
import com.financial.wealth.api.transactions.models.ApiResponseModel;
import com.financial.wealth.api.transactions.models.BaseResponse;
import com.financial.wealth.api.transactions.models.LocalTransferRequest;
import com.financial.wealth.api.transactions.models.OtherBankTransferRequest;
import com.financial.wealth.api.transactions.models.SaveBeneficiary;
import com.financial.wealth.api.transactions.models.WalletNoReq;
import com.financial.wealth.api.transactions.models.local.trans.NameLookUp;
import com.financial.wealth.api.transactions.services.LocalTransferService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author olufemioshin
 */
@RestController
@RequiredArgsConstructor
public class TransferServicesControllers {

    private final LocalTransferService localTransferService;
    private final NipBankService nipService;

    @GetMapping("/get-banks")
    public ApiResponseModel getAllBanks(@RequestHeader(value = "authorization", required = true) String auth) throws ApiClientException {
        return nipService.getAllBanks();
    }

    @PostMapping("/localtransfer/name-enquiry")
    public ResponseEntity<BaseResponse> nameLookUp(@RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid NameLookUp rq) {

        BaseResponse baseResponse = localTransferService.nameLookUp(rq, "", auth);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/interbank/name-enquiry")
    public ResponseEntity<BaseResponse> nameLookUpInterBank(@RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid NameLookUpInterBank rq) {

        BaseResponse baseResponse = nipService.nameLookUp(rq, "", auth);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/interbank/make-payment")
    public ResponseEntity<BaseResponse> makePayment(@RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid OtherBankTransferRequest rq) {

        BaseResponse baseResponse = nipService.processTransfer(rq, "", auth);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/localtransfer/transfer")
    public ResponseEntity<BaseResponse> processTransfer(@RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid LocalTransferRequest rq) {

        BaseResponse baseResponse = localTransferService.processTransfer(rq, "", auth);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/localtransfer/save-beneficiary")
    public ResponseEntity<BaseResponse> saveBeneficiary(@RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid SaveBeneficiary rq) {

        BaseResponse baseResponse = localTransferService.saveBeneficiary(rq, "", auth);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @GetMapping("/localtransfer/find-beneficiary")
    public ResponseEntity<ApiResponseModel> configuedMembersNumberData(@RequestHeader(value = "authorization", required = true) String auth) {

        ApiResponseModel baseResponse = localTransferService.findSavedBeneficiaries("", auth);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/get-transactions-history")
    public ResponseEntity<ApiResponseModel> walletToWalletUserTransactions(@RequestHeader(value = "authorization", required = true) String auth,
            // @RequestHeader(value = "channel", required = true) String channel, 
            @RequestBody @Valid WalletNoReq rq) {

        ApiResponseModel baseResponse = localTransferService.getUserTransactionsHistory(rq, "", auth);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

}
