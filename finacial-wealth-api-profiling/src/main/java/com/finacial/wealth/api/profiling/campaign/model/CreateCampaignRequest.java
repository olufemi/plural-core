/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.campaign.model;

import java.util.Date;
import java.util.List;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class CreateCampaignRequest {
    public String title;
    public String description;
    public String mediaObjectName;   // from Firebase upload result
    public String mediaContentType;
    public String mediaSignedUrl;    // optional
    public String embeddedLink;
    public Date startAt;
    public Date endAt;
    
  

    public Integer rotationSeconds;   // e.g. 6 (optional)
    public String displayMode;        // SINGLE/CAROUSEL (optional)

    public List<CampaignMediaItemRequest> items; // 1..N
}
