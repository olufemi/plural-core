/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.backoffice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;
import lombok.Data;

/**
 *
 * @author olufemioshin
 */
@Entity
@Table(name="campaign_audit",
       indexes = {
           @Index(name="idx_audit_campaign", columnList="campaignId"),
           @Index(name="idx_audit_time", columnList="eventAt")
       })
@Data
public class CampaignAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private Long campaignId;

    @Column(nullable=false, length=40)
    private String action; // CREATE, UPDATE, SUBMIT_FOR_APPROVAL, APPROVE, STOP, CANCEL, RESTART, AUTO_START, AUTO_END

    @Column(nullable=false, length=80)
    private String actor; // username/userId

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable=false)
    private Date eventAt;

    @Column(length=2000)
    private String note; // optional details (what changed, why blocked, etc.)

    // getters/setters...
}
