/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.campaign.entity;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.profiling.campaign.ennum.MediaKind;
import javax.persistence.*;
import lombok.Data;

@Entity
@Table(
  name="campaign_media_items",
  uniqueConstraints = {
    @UniqueConstraint(name="uq_campaign_order", columnNames={"campaign_id", "order_no"})
  },
  indexes = {
    @Index(name="idx_media_campaign", columnList="campaign_id")
  }
)
@Data
public class CampaignMediaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="campaign_id", nullable=false)
    private Long campaignId;

    @Column(nullable=false, length=300)
    private String objectName; // firebase objectName

    @Column(nullable=true, length=120)
    private String contentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private MediaKind mediaKind;

    @Column(name="order_no", nullable=false)
    private Integer orderNo;

    @Column(nullable=true, length=1000)
    private String embeddedLink; // optional per item override

    // getters/setters...
}

