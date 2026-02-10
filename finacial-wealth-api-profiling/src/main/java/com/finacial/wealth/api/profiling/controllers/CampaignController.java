/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.controllers;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.profiling.campaign.entity.Campaign;
import com.finacial.wealth.api.profiling.campaign.entity.CampaignAudit;
import com.finacial.wealth.api.profiling.campaign.model.ApproveCampaignRequest;
import com.finacial.wealth.api.profiling.campaign.model.CreateCampaignRequest;
import com.finacial.wealth.api.profiling.campaign.model.UpdateCampaignRequest;
import com.finacial.wealth.api.profiling.campaign.service.CampaignService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/campaigns")
public class CampaignController {

    private final CampaignService campaignService;

    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    private String actor(String header) {
        return (header == null || header.trim().isEmpty()) ? "UNKNOWN" : header.trim();
    }

    @PostMapping
    public ResponseEntity<Campaign> create(@RequestBody CreateCampaignRequest req,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ResponseEntity.ok(campaignService.create(req, actor(userId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Campaign> update(@PathVariable Long id,
            @RequestBody UpdateCampaignRequest req,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ResponseEntity.ok(campaignService.update(id, req, actor(userId)));
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<Campaign> stop(@PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ResponseEntity.ok(campaignService.stop(id, actor(userId)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Campaign> cancel(@PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ResponseEntity.ok(campaignService.cancel(id, actor(userId)));
    }

    @PostMapping("/{id}/restart")
    public ResponseEntity<Campaign> restart(@PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return ResponseEntity.ok(campaignService.restart(id, actor(userId)));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Campaign> approve(@PathVariable Long id,
            @RequestBody(required = false) ApproveCampaignRequest req,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        String note = (req == null ? null : req.note);
        return ResponseEntity.ok(campaignService.approve(id, actor(userId), note));
    }

    @GetMapping
    public ResponseEntity<List<Campaign>> list() {
        return ResponseEntity.ok(campaignService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Campaign> get(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.get(id));
    }

    @GetMapping("/{id}/audit")
    public ResponseEntity<List<CampaignAudit>> audit(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.auditTrail(id));
    }
}
