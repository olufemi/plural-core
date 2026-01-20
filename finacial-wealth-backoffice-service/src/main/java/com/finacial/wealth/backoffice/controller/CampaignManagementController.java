/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.backoffice.controller;

import com.finacial.wealth.backoffice.integrations.profiling.CampaignManagementService;
import com.finacial.wealth.backoffice.model.ApiResponseModel;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author olufemioshin
 */
@RestController
@RequestMapping("/bo/campaigns")
public class CampaignManagementController {

    private final CampaignManagementService service;

    public CampaignManagementController(CampaignManagementService service) {
        this.service = service;
    }

    @PostMapping
    public ApiResponseModel create(@RequestBody Map<String, Object> payload) {
        return service.createCampaign(payload);
    }

    @PutMapping("/{id}")
    public ApiResponseModel update(@PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        return service.updateCampaign(id, payload);
    }

    @PostMapping("/{id}/approve")
    public ApiResponseModel approve(@PathVariable Long id,
            @RequestParam(required = false) String note) {
        return service.approveCampaign(id, note);
    }

    @PostMapping("/{id}/stop")
    public ApiResponseModel stop(@PathVariable Long id) {
        return service.stopCampaign(id);
    }

    @PostMapping("/{id}/cancel")
    public ApiResponseModel cancel(@PathVariable Long id) {
        return service.cancelCampaign(id);
    }

    @PostMapping("/{id}/restart")
    public ApiResponseModel restart(@PathVariable Long id) {
        return service.restartCampaign(id);
    }

    @GetMapping
    public ApiResponseModel list() {
        return service.listCampaigns();
    }

    @GetMapping("/{id}")
    public ApiResponseModel get(@PathVariable Long id) {
        return service.getCampaign(id);
    }

    @GetMapping("/{id}/audit")
    public ApiResponseModel audit(@PathVariable Long id) {
        return service.getCampaignAudit(id);
    }
}
