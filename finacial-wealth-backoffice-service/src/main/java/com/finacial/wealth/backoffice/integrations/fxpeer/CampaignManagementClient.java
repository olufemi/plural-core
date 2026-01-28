/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.backoffice.integrations.fxpeer;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.backoffice.campaign.model.ApproveCampaignRequest;
import com.finacial.wealth.backoffice.campaign.model.CreateCampaignRequest;
import com.finacial.wealth.backoffice.campaign.model.UpdateCampaignRequest;
import com.finacial.wealth.backoffice.model.ApiResponseModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(
        name = "profiling-service",
        contextId = "campaignClient",
        configuration = com.finacial.wealth.backoffice.config.FeignConfig.class
)
public interface CampaignManagementClient {

    @PostMapping(value = "/campaigns", consumes = "application/json")
    ApiResponseModel createCampaign(@RequestBody CreateCampaignRequest request);

    @PutMapping(value = "/campaigns/{id}", consumes = "application/json")
    ApiResponseModel updateCampaign(@PathVariable("id") Long id,
            @RequestBody UpdateCampaignRequest request);

    @PostMapping(value = "/campaigns/{id}/approve", consumes = "application/json")
    ApiResponseModel approveCampaign(@PathVariable("id") Long id,
            @RequestBody ApproveCampaignRequest request);

    @PostMapping("/campaigns/{id}/stop")
    ApiResponseModel stopCampaign(@PathVariable("id") Long id);

    @PostMapping("/campaigns/{id}/cancel")
    ApiResponseModel cancelCampaign(@PathVariable("id") Long id);

    @PostMapping("/campaigns/{id}/restart")
    ApiResponseModel restartCampaign(@PathVariable("id") Long id);

    @GetMapping("/campaigns")
    ApiResponseModel listCampaigns();

    @GetMapping("/campaigns/{id}")
    ApiResponseModel getCampaign(@PathVariable("id") Long id);

    @GetMapping("/campaigns/{id}/audit")
    ApiResponseModel getCampaignAudit(@PathVariable("id") Long id);

    @GetMapping("/campaigns/active")
    ApiResponseModel getActiveCampaign();
}
