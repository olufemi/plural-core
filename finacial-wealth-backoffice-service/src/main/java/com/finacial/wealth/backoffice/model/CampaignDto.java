/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.backoffice.model;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author olufemioshin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignDto {

    private Long id;

    private String title;
    private String description;

    private String mediaObjectName;
    private String mediaContentType;
    private String mediaSignedUrl;

    private String embeddedLink;

    private Date startAt;
    private Date endAt;

    private String status;        // CampaignStatus as String
    private String mediaKind;     // MediaKind as String

    private Integer rotationSeconds;
    private String displayMode;

    private String createdBy;
    private Date createdAt;

    private String updatedBy;
    private Date updatedAt;

    private String approvedBy;
    private Date approvedAt;
}

