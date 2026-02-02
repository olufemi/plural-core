/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.controllers;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.profiling.campaign.ennum.CampaignStatus;
import com.finacial.wealth.api.profiling.campaign.entity.Campaign;
import com.finacial.wealth.api.profiling.campaign.entity.CampaignMediaItem;
import com.finacial.wealth.api.profiling.campaign.model.ActiveCampaignResponse;
import com.finacial.wealth.api.profiling.campaign.model.CampaignMediaItemResponse;
import com.finacial.wealth.api.profiling.campaign.repo.CampaignMediaItemRepository;
import com.finacial.wealth.api.profiling.campaign.repo.CampaignRepository;
import com.finacial.wealth.api.profiling.models.ApiResponseModel;
import com.finacial.wealth.api.profiling.storage.firebase.FirebaseStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/walletmgt/campaigns")
public class CampaignMobileController {

    private final CampaignRepository campaignRepo;
    private final CampaignMediaItemRepository itemRepo;
    private final FirebaseStorageService firebaseStorageService;

    public CampaignMobileController(CampaignRepository campaignRepo,
            CampaignMediaItemRepository itemRepo,
            FirebaseStorageService firebaseStorageService) {
        this.campaignRepo = campaignRepo;
        this.itemRepo = itemRepo;
        this.firebaseStorageService = firebaseStorageService;
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponseModel> getActiveCampaigns() {
        ApiResponseModel resp = new ApiResponseModel();

        List<Campaign> campaigns = campaignRepo.findByStatusOrderByStartAtAsc(CampaignStatus.ACTIVE);
        if (campaigns == null || campaigns.isEmpty()) {
            resp.setStatusCode(400);
            resp.setDescription("No active campaign");
            resp.setData(null);
            return ResponseEntity.ok(resp);
        }

        List<ActiveCampaignResponse> payloads = new ArrayList<>();

        for (Campaign c : campaigns) {
            List<CampaignMediaItem> items = itemRepo.findByCampaignIdOrderByOrderNoAsc(c.getId());
            if (items == null || items.isEmpty()) {
                // skip campaigns with no items (or you can include them with empty list)
                continue;
            }

            ActiveCampaignResponse payload = new ActiveCampaignResponse();
            payload.campaignId = c.getId();
            payload.title = c.getTitle();
            payload.status = c.getStatus();
            payload.embeddedLink = c.getEmbeddedLink();
            payload.displayMode = c.getDisplayMode();
            payload.rotationSeconds = c.getRotationSeconds();
            payload.startAt = c.getStartAt();
            payload.endAt = c.getEndAt();

            payload.items = new ArrayList<>();
            for (CampaignMediaItem it : items) {
                CampaignMediaItemResponse x = new CampaignMediaItemResponse();
                x.orderNo = it.getOrderNo();
                x.mediaKind = it.getMediaKind().name();
                x.contentType = it.getContentType();
                x.objectName = it.getObjectName();
                x.embeddedLink = it.getEmbeddedLink();
                x.mediaUrl = firebaseStorageService.signUrl(it.getObjectName());
                payload.items.add(x);
            }

            payloads.add(payload);
        }

        if (payloads.isEmpty()) {
            resp.setStatusCode(400);
            resp.setDescription("No active campaign");
            resp.setData(null);
            return ResponseEntity.ok(resp);
        }

        resp.setStatusCode(200);
        resp.setDescription("Active campaigns");
        resp.setData(payloads);
        return ResponseEntity.ok(resp);
    }

}
