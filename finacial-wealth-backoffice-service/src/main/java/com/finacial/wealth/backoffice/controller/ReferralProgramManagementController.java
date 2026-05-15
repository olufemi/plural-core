package com.finacial.wealth.backoffice.controller;

import com.finacial.wealth.backoffice.integrations.profiling.ReferralProgramManagementService;
import com.finacial.wealth.backoffice.model.ApiResponseModel;
import com.finacial.wealth.backoffice.referral.model.CreateReferralProgramRequest;
import com.finacial.wealth.backoffice.referral.model.UpdateReferralProgramRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/backoffice/referral-programs")
public class ReferralProgramManagementController {

    private final ReferralProgramManagementService service;

    public ReferralProgramManagementController(ReferralProgramManagementService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public ApiResponseModel create(@RequestBody CreateReferralProgramRequest req,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return service.createReferralProgram(req, userId);
    }

    @PutMapping("/{id}")
    public ApiResponseModel update(@PathVariable Long id,
            @RequestBody UpdateReferralProgramRequest req,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return service.updateReferralProgram(id, req, userId);
    }

    @PostMapping("/{id}/activate")
    public ApiResponseModel activate(@PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return service.activateReferralProgram(id, userId);
    }

    @PostMapping("/{id}/pause")
    public ApiResponseModel pause(@PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return service.pauseReferralProgram(id, userId);
    }

    @PostMapping("/{id}/end")
    public ApiResponseModel end(@PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return service.endReferralProgram(id, userId);
    }

    @GetMapping("/get-all")
    public ApiResponseModel list(@RequestParam(value = "productType", required = false) String productType) {
        return service.listReferralPrograms(productType);
    }

    @GetMapping("/{id}")
    public ApiResponseModel get(@PathVariable Long id) {
        return service.getReferralProgram(id);
    }

    @GetMapping("/{id}/audit")
    public ApiResponseModel audit(@PathVariable Long id) {
        return service.getReferralProgramAudit(id);
    }

    @GetMapping("/active")
    public ApiResponseModel active(@RequestParam("productType") String productType) {
        return service.getActiveReferralProgram(productType);
    }
}
