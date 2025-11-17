/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.controllers;

import com.finacial.wealth.api.fxpeer.exchange.model.ApiResponseModel;
import com.finacial.wealth.api.fxpeer.exchange.model.BaseResponse;

import com.finacial.wealth.api.fxpeer.exchange.inter.airtime.security.ProcSochitelServices;
import com.finacial.wealth.api.fxpeer.exchange.inter.airtime.security.ProcessTrnsactionReq;
import com.finacial.wealth.api.fxpeer.exchange.inter.airtime.security.ValidatePhoneNumber;
import com.finacial.wealth.api.fxpeer.exchange.model.GetProducts;
import com.finacial.wealth.api.fxpeer.exchange.model.GetProductsByCatId;
import com.finacial.wealth.api.fxpeer.exchange.model.ValidateAccount;
import jakarta.validation.Valid;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@RequestMapping("/fxothers")
public class FxOtherServicesController {

    private final ProcSochitelServices procSochitelServices;

    public FxOtherServicesController(ProcSochitelServices procSochitelServices) {
        this.procSochitelServices = procSochitelServices;

    }

    /**
     * POST /fxothers/airtime-get-products Expects: Authorization header + JSON
     * body (GetProducts) Produces: application/json (ApiResponseModel)
     */
    @PostMapping(
            path = "/get-all-products",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponseModel> getProducts(
            @RequestHeader(name = "authorization", required = true) String auth,
            @RequestBody @Valid GetProducts rq
    ) {
        return procSochitelServices.getProdocts(rq, auth);
    }

    @PostMapping(
            path = "/int-utilities-get-products",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponseModel> getProducts(
            @RequestHeader(name = "authorization", required = true) String auth,
            @RequestBody @Valid GetProductsByCatId rq
    ) {
        return procSochitelServices.getProdoctsByCategory(rq, auth);
    }

    /**
     * Simple probe to confirm you’re hitting THIS build & service. curl -s
     * http://127.0.0.1:7007/fxothers/__ping
     */
    @GetMapping(path = "/__ping", produces = MediaType.TEXT_PLAIN_VALUE)
    public String ping() {
        return "fxothers:ok";
    }

    @GetMapping(
            path = "/int-utilities-get-categories",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponseModel> validatePhoneNumber(
            @RequestHeader(value = "authorization", required = true) String auth) {

        ApiResponseModel baseResponse = procSochitelServices.getCategories(auth);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping(
            path = "/airtime-validate-phonenumber",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<BaseResponse> validatePhoneNumber(
            @RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid ValidatePhoneNumber rq) {

        BaseResponse baseResponse = procSochitelServices.validatePhoneNumber(rq, auth);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping(
            path = "/int-utilities-fulfilment",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponseModel> processTrnsaction(
            @RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid ProcessTrnsactionReq rq) throws IOException {

        ApiResponseModel baseResponse = procSochitelServices.processTrnsaction(rq, auth);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

    @PostMapping(
            path = "/int-utilities-validate-accountid",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponseModel> getAccountLookup(
            @RequestHeader(name = "authorization", required = true) String auth,
            @RequestBody @Valid ValidateAccount rq
    ) {
        return procSochitelServices.getAccountLookup(rq, auth);
    }

}
