package com.finacial.wealth.api.profiling.controllers;

import com.finacial.wealth.api.profiling.referralprogram.model.ApplyReferralAttributionRequest;
import com.finacial.wealth.api.profiling.referralprogram.model.CompleteReferralAttributionRequest;
import com.finacial.wealth.api.profiling.referralprogram.model.QualifyReferralAttributionRequest;
import com.finacial.wealth.api.profiling.referralprogram.service.ReferralProgramRuntimeService;
import com.finacial.wealth.api.profiling.response.BaseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/referral-programs/runtime")
public class ReferralProgramRuntimeController {

    private final ReferralProgramRuntimeService referralProgramRuntimeService;

    public ReferralProgramRuntimeController(ReferralProgramRuntimeService referralProgramRuntimeService) {
        this.referralProgramRuntimeService = referralProgramRuntimeService;
    }

    @PostMapping("/apply")
    public ResponseEntity<BaseResponse> apply(@RequestBody ApplyReferralAttributionRequest req,
            @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(referralProgramRuntimeService.apply(req, auth));
    }

    @PostMapping("/qualify")
    public ResponseEntity<BaseResponse> qualify(@RequestBody QualifyReferralAttributionRequest req,
            @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(referralProgramRuntimeService.qualify(req, auth));
    }

    @PostMapping("/{attributionId}/complete")
    public ResponseEntity<BaseResponse> complete(@PathVariable Long attributionId,
            @RequestBody CompleteReferralAttributionRequest req,
            @RequestHeader("Authorization") String auth) {
        return ResponseEntity.ok(referralProgramRuntimeService.complete(attributionId, req, auth));
    }
}
