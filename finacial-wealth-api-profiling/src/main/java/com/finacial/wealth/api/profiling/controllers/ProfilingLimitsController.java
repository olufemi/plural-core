/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.controllers;

import com.finacial.wealth.api.profiling.limits.LimitsUiRequest;
import com.finacial.wealth.api.profiling.limits.LimitsUiResponse;
import com.finacial.wealth.api.profiling.limits.ProfilingLimitsService;
import com.finacial.wealth.api.profiling.models.ProfilingLimitsResponse;
import com.finacial.wealth.api.profiling.models.WalletNo;
import com.finacial.wealth.api.profiling.services.ProfilingLimitsServiceVersionOne;
import java.io.UnsupportedEncodingException;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
/*@RestController
@RequestMapping("/v1/profiling")
public class ProfilingLimitsController {

    @Autowired
    private ProfilingLimitsService service;

    @GetMapping("/limits")
    public ProfilingLimitsResponse limits(
            @RequestParam String accountNumber,
            @RequestParam String productCode,
            @RequestParam String currency
    ) {
        return service.getLimits(accountNumber, productCode, currency);
    }
}*/
@RestController
@RequestMapping("/limits")
class ProfilingLimitsController {

    private final ProfilingLimitsService limitsService;

    public ProfilingLimitsController(ProfilingLimitsService limitsService) {
        this.limitsService = limitsService;
    }

    @PostMapping("/account")
    public ResponseEntity<LimitsUiResponse> getLimitsUi(
            @RequestHeader(value = "authorization", required = true) String auth,
            @RequestBody @Valid LimitsUiRequest rq
    //  @RequestHeader(value = "channel", required = true) String channel,
    ) throws UnsupportedEncodingException {

        LimitsUiResponse r = limitsService.getLimitsUi(rq, auth, "Api");
        return new ResponseEntity<LimitsUiResponse>(r, HttpStatus.OK);
    }
}
