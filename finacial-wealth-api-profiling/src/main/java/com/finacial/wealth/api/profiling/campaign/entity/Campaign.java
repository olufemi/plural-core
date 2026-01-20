/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.campaign.entity;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.profiling.campaign.ennum.CampaignStatus;
import com.finacial.wealth.api.profiling.campaign.ennum.MediaKind;
import javax.persistence.*;
import java.util.Date;
import lombok.Data;

@Entity
@Table(name = "campaigns",
        indexes = {
            @Index(name = "idx_campaign_status", columnList = "status"),
            @Index(name = "idx_campaign_start", columnList = "startAt"),
            @Index(name = "idx_campaign_end", columnList = "endAt")
        }
)
@Data
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(length = 500)
    private String description;

    // Firebase storage reference e.g. "pictures/170...._uuid.png"
    @Column(nullable = false, length = 300)
    private String mediaObjectName;

    @Column(nullable = true, length = 120)
    private String mediaContentType;

    @Column(nullable = true, length = 1000)
    private String mediaSignedUrl; // optional preview convenience

    @Column(nullable = false, length = 1000)
    private String embeddedLink;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date startAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CampaignStatus status;

    @Column(nullable = false, length = 80)
    private String createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createdAt;

    @Column(nullable = true, length = 80)
    private String updatedBy;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @Column(nullable = true, length = 80)
    private String approvedBy;

    @Temporal(TemporalType.TIMESTAMP)
    private Date approvedAt;

    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MediaKind mediaKind;

    public Integer rotationSeconds;   // e.g. 6 (optional)
    public String displayMode;        // SINGLE/CAROUSEL (optional)

    // getters/setters...
}
