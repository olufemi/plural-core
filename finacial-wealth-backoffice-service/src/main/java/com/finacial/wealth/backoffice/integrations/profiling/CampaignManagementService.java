/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.backoffice.integrations.profiling;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.backoffice.campaign.model.ApproveCampaignRequest;
import com.finacial.wealth.backoffice.campaign.model.CreateCampaignRequest;
import com.finacial.wealth.backoffice.campaign.model.UpdateCampaignRequest;
import com.finacial.wealth.backoffice.integrations.fxpeer.CampaignManagementClient;
import com.finacial.wealth.backoffice.model.ApiResponseModel;
import com.finacial.wealth.backoffice.model.CampaignAudit;
import com.finacial.wealth.backoffice.model.CampaignDto;
import com.google.gson.Gson;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CampaignManagementService {

    private final CampaignManagementClient client;

    public CampaignManagementService(CampaignManagementClient client) {
        this.client = client;
    }

    public ApiResponseModel createCampaign(CreateCampaignRequest payload) {
        System.out.println("createCampaign payload ::::: %S " + new Gson().toJson(payload));
        return client.createCampaign(payload);
    }

    public ApiResponseModel updateCampaign(Long id, UpdateCampaignRequest payload) {

        CampaignDto list = client.updateCampaign(id, payload);

        ApiResponseModel res = new ApiResponseModel();
        res.setStatusCode(200);
        res.setDescription("OK");
        res.setData(list);

        return res;
    }

    /* public ApiResponseModel approveCampaign(Long id, String note) {
        ApproveCampaignRequest req = new ApproveCampaignRequest();
        req.setNote(note);
        return client.approveCampaign(id, req);
    }*/
    public ApiResponseModel approveCampaign(Long id, ApproveCampaignRequest req) {
        return client.approveCampaign(id, req);

    }

    public ApiResponseModel stopCampaign(Long id) {
        CampaignDto list = client.stopCampaign(id);

        ApiResponseModel res = new ApiResponseModel();
        res.setStatusCode(200);
        res.setDescription("OK");
        res.setData(list);

        return res;
    }

    public ApiResponseModel cancelCampaign(Long id) {
        CampaignDto list = client.cancelCampaign(id);

        ApiResponseModel res = new ApiResponseModel();
        res.setStatusCode(200);
        res.setDescription("OK");
        res.setData(list);

        return res;
    }

    public ApiResponseModel restartCampaign(Long id) {
        CampaignDto list = client.restartCampaign(id);

        ApiResponseModel res = new ApiResponseModel();
        res.setStatusCode(200);
        res.setDescription("OK");
        res.setData(list);

        return res;

    }

    public ApiResponseModel listCampaigns() {

        List<CampaignDto> list = client.listCampaigns();

        ApiResponseModel res = new ApiResponseModel();
        res.setStatusCode(200);
        res.setDescription("OK");
        res.setData(list);

        return res;
    }

    public ApiResponseModel getCampaign(Long id) {

        CampaignDto list = client.getCampaign(id);

        ApiResponseModel res = new ApiResponseModel();
        res.setStatusCode(200);
        res.setDescription("OK");
        res.setData(list);

        return res;
    }

    public ApiResponseModel getCampaignAudit(Long id) {
        List<CampaignAudit> list = client.getCampaignAudit(id);

        ApiResponseModel res = new ApiResponseModel();
        res.setStatusCode(200);
        res.setDescription("OK");
        res.setData(list);

        return res;
    }

    public ApiResponseModel getActiveCampaign() {
        return client.getActiveCampaign();
    }
}
