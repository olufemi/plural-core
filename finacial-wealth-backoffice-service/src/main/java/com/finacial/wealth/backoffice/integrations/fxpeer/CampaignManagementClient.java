/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.finacial.wealth.backoffice.integrations.fxpeer;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.backoffice.model.ApiResponseModel;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(
    name = "profiling-service",
    contextId = "campaignManagementClient",
    configuration = com.finacial.wealth.backoffice.config.FeignConfig.class
)
public interface CampaignManagementClient {

    // CREATE campaign (with items[])
    @PostMapping(value = "/api/campaigns", consumes = "application/json")
    ApiResponseModel createCampaign(@RequestBody Map<String, Object> request);

    // UPDATE campaign (triggers re-approval)
    @PutMapping(value = "/api/campaigns/{id}", consumes = "application/json")
    ApiResponseModel updateCampaign(
            @PathVariable("id") Long id,
            @RequestBody Map<String, Object> request
    );

    // APPROVE campaign
    @PostMapping(value = "/api/campaigns/{id}/approve", consumes = "application/json")
    ApiResponseModel approveCampaign(
            @PathVariable("id") Long id,
            @RequestBody Map<String, Object> request
    );

    // STOP campaign
    @PostMapping(value = "/api/campaigns/{id}/stop")
    ApiResponseModel stopCampaign(@PathVariable("id") Long id);

    // CANCEL campaign
    @PostMapping(value = "/api/campaigns/{id}/cancel")
    ApiResponseModel cancelCampaign(@PathVariable("id") Long id);

    // RESTART campaign (moves to pending approval)
    @PostMapping(value = "/api/campaigns/{id}/restart")
    ApiResponseModel restartCampaign(@PathVariable("id") Long id);

    // LIST all campaigns (current + historical)
    @GetMapping(value = "/api/campaigns")
    ApiResponseModel listCampaigns();

    // GET a campaign by id (details)
    @GetMapping(value = "/api/campaigns/{id}")
    ApiResponseModel getCampaign(@PathVariable("id") Long id);

    // GET audit trail
    @GetMapping(value = "/api/campaigns/{id}/audit")
    ApiResponseModel getCampaignAudit(@PathVariable("id") Long id);

    // MOBILE view - active campaign (carousel payload)
    @GetMapping(value = "/api/campaigns/active")
    ApiResponseModel getActiveCampaign();
}

