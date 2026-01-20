/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.campaign.model;

import com.finacial.wealth.api.profiling.campaign.ennum.CampaignStatus;
import com.finacial.wealth.api.profiling.campaign.ennum.MediaKind;
import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class ActiveCampaignResponse {

    public Long id;
    public String title;
    public String embeddedLink;

    public MediaKind mediaKind;
    public String mediaContentType;
    public String mediaObjectName;
    public String mediaUrl; // signedUrl

    public Date startAt;
    public Date endAt;

    public Long campaignId;
    public Integer rotationSeconds; // e.g. 6
    public String displayMode;      // SINGLE or CAROUSEL

    public List<CampaignMediaItemResponse> items;
    public CampaignStatus status;
}
