/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.backoffice.campaign.model;

import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Data
public class CampaignMediaItemRequest {
    public Integer orderNo;
    public String objectName;     // firebase objectName
    public String contentType;    // optional (if you know it)
    public String embeddedLink;   // optional per slide
}
