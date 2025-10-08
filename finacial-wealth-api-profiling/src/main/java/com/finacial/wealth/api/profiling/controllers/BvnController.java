/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.controllers;

import com.finacial.wealth.api.profiling.breezpay.virt.get.bvn.BvnLookup;
import com.finacial.wealth.api.profiling.breezpay.virt.get.bvn.BvnService;
import com.finacial.wealth.api.profiling.breezpay.virt.get.bvn.ValidateBvnReq;
import com.finacial.wealth.api.profiling.models.ChangePasswordInApp;
import com.finacial.wealth.api.profiling.response.BaseResponse;
import com.finacial.wealth.api.profiling.services.AddAccountService;
import java.io.UnsupportedEncodingException;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
@RequestMapping("/validate")
@RequiredArgsConstructor
public class BvnController {

    private final BvnService bvnService;
    //  private final AddAccountService addAccountService;

    @GetMapping("/to/{bvn}")
    public ResponseEntity<BvnLookup> getBvn(
            @PathVariable String bvn,
            @RequestHeader("Authorization") String auth,
            @RequestHeader("Ocp-Apim-Subscription-Key") String subKey) {

        //BvnLookup result = bvnService.getOrFetchAndPersist(bvn, auth, subKey);
        BvnLookup result = new BvnLookup();
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/bvn")
    public ResponseEntity<BaseResponse> changePasswordInApp(
            @RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid ValidateBvnReq rq) throws UnsupportedEncodingException {

        String bvn = rq.getBvn();

        BaseResponse baseResponse = bvnService.validateBvnCaller(bvn, auth);
        return new ResponseEntity<>(baseResponse, HttpStatus.OK);
    }

}
