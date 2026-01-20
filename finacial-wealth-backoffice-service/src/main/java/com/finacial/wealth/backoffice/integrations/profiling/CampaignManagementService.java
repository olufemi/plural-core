/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.backoffice.integrations.profiling;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.backoffice.integrations.fxpeer.CampaignManagementClient;
import com.finacial.wealth.backoffice.model.ApiResponseModel;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CampaignManagementService {

    private final CampaignManagementClient client;

    public CampaignManagementService(CampaignManagementClient client) {
        this.client = client;
    }

    public ApiResponseModel createCampaign(Map<String, Object> payload) {
        return client.createCampaign(payload);
    }

    public ApiResponseModel updateCampaign(Long id, Map<String, Object> payload) {
        return client.updateCampaign(id, payload);
    }

    public ApiResponseModel approveCampaign(Long id, String note) {
        Map<String, Object> req = new HashMap<String, Object>();
        req.put("note", note);
        return client.approveCampaign(id, req);
    }

    public ApiResponseModel stopCampaign(Long id) {
        return client.stopCampaign(id);
    }

    public ApiResponseModel cancelCampaign(Long id) {
        return client.cancelCampaign(id);
    }

    public ApiResponseModel restartCampaign(Long id) {
        return client.restartCampaign(id);
    }

    public ApiResponseModel listCampaigns() {
        return client.listCampaigns();
    }

    public ApiResponseModel getCampaign(Long id) {
        return client.getCampaign(id);
    }

    public ApiResponseModel getCampaignAudit(Long id) {
        return client.getCampaignAudit(id);
    }

    public ApiResponseModel getActiveCampaign() {
        return client.getActiveCampaign();
    }
}

