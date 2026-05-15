package com.finacial.wealth.backoffice.integrations.profiling;

import com.finacial.wealth.backoffice.config.FeignConfig;
import com.finacial.wealth.backoffice.referral.model.CreateReferralProgramRequest;
import com.finacial.wealth.backoffice.referral.model.ReferralProgramAuditDto;
import com.finacial.wealth.backoffice.referral.model.ReferralProgramDto;
import com.finacial.wealth.backoffice.referral.model.UpdateReferralProgramRequest;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "profiling-service",
        contextId = "referralProgramManagementClient",
        configuration = FeignConfig.class
)
public interface ReferralProgramManagementClient {

    @PostMapping(value = "/referral-programs", consumes = "application/json")
    ReferralProgramDto createReferralProgram(@RequestBody CreateReferralProgramRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId);

    @PutMapping(value = "/referral-programs/{id}", consumes = "application/json")
    ReferralProgramDto updateReferralProgram(@PathVariable("id") Long id,
            @RequestBody UpdateReferralProgramRequest request,
            @RequestHeader(value = "X-User-Id", required = false) String userId);

    @PostMapping("/referral-programs/{id}/activate")
    ReferralProgramDto activateReferralProgram(@PathVariable("id") Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId);

    @PostMapping("/referral-programs/{id}/pause")
    ReferralProgramDto pauseReferralProgram(@PathVariable("id") Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId);

    @PostMapping("/referral-programs/{id}/end")
    ReferralProgramDto endReferralProgram(@PathVariable("id") Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId);

    @GetMapping("/referral-programs")
    List<ReferralProgramDto> listReferralPrograms(@RequestParam(value = "productType", required = false) String productType);

    @GetMapping("/referral-programs/{id}")
    ReferralProgramDto getReferralProgram(@PathVariable("id") Long id);

    @GetMapping("/referral-programs/{id}/audit")
    List<ReferralProgramAuditDto> getReferralProgramAudit(@PathVariable("id") Long id);

    @GetMapping("/referral-programs/active")
    ReferralProgramDto getActiveReferralProgram(@RequestParam("productType") String productType);
}
