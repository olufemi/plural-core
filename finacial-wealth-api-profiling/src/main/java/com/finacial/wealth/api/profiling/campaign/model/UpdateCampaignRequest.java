/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.campaign.model;

/**
 *
 * @author olufemioshin
 */
import java.util.Date;
import java.util.List;
import lombok.Data;
@Data
public class UpdateCampaignRequest {
    public String title;
    public String description;
    public String mediaObjectName;
    public String mediaContentType;
    public String mediaSignedUrl;
    public String embeddedLink;
    public Date startAt;
    public Date endAt;


    public Integer rotationSeconds;
    public String displayMode;

    // If provided, we REPLACE the items list (simple + clean)
    public List<CampaignMediaItemRequest> items;
}
