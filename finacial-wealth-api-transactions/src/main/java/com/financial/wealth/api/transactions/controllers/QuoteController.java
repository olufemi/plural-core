/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.controllers;

import com.financial.wealth.api.transactions.models.AcceptQuoteFE;

import com.financial.wealth.api.transactions.models.BaseResponse;
import com.financial.wealth.api.transactions.models.WalletNo;

import com.financial.wealth.api.transactions.models.tranfaar.inflow.CreateQuoteFE;
import com.financial.wealth.api.transactions.models.tranfaar.inflow.GetPendingQuotes;
import com.financial.wealth.api.transactions.models.tranfaar.outflow.CreateQuoteWithdrawalFE;

import com.financial.wealth.api.transactions.tranfaar.services.CreateQuoteClient;
import com.financial.wealth.api.transactions.tranfaar.services.QuoteLookupService;
import java.io.UnsupportedEncodingException;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author olufemioshin
 */
@RestController
@RequestMapping("/interbank")
public class QuoteController {

    @Autowired
    private CreateQuoteClient quoteService;
    @Autowired
    private QuoteLookupService qservice;

    @PostMapping("/validate-pin")
    public ResponseEntity<BaseResponse> validatePin(@RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid WalletNo rq) {

        BaseResponse baseResponse = quoteService.validatePin(rq, auth);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @GetMapping("/pending-accepted")
    public ResponseEntity<BaseResponse> getPendingAcceptedForUser(
            @RequestHeader(value = "authorization", required = true) String auth,
            // @RequestParam("email") String email,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) throws UnsupportedEncodingException {
        BaseResponse res = qservice.findAllPendingAcceptedForUserByEmail(auth, page, size);
        return ResponseEntity.status(res.getStatusCode()).body(res);
    }

    @PostMapping("/pending-accepted-quoteid")
    public ResponseEntity<BaseResponse> getPendingAccepted(@RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid GetPendingQuotes rq) {
        BaseResponse res = qservice.findPendingAcceptedQuote(rq.getQuoteId());
        return ResponseEntity.status(res.getStatusCode() == 200 ? 200 : res.getStatusCode()).body(res);
    }

    @PostMapping("/create-quote")
    public ResponseEntity<BaseResponse> createQuote(@RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid CreateQuoteFE rq) throws Exception {

        BaseResponse baseResponse = quoteService.createQuote(rq, auth);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/accept-quote")
    public ResponseEntity<BaseResponse> acceptQuote(@RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid AcceptQuoteFE rq) throws Exception {

        BaseResponse baseResponse = quoteService.acceptQuote(rq, auth);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }
    
     @PostMapping("/create-quote-withdrawal")
    public ResponseEntity<BaseResponse> createQuoteWithdrawal(@RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid CreateQuoteWithdrawalFE rq) throws Exception {

        BaseResponse baseResponse = quoteService.createQuoteWithdrawal(rq, auth);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping("/accept-quote-withdrawal")
    public ResponseEntity<BaseResponse> acceptQuoteWithdrawal(@RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid AcceptQuoteFE rq) throws Exception {

        BaseResponse baseResponse = quoteService.acceptQuoteWithdrawal(rq, auth);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

}
