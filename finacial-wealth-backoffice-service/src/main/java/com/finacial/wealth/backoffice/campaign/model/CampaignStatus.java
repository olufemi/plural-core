/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package com.finacial.wealth.backoffice.campaign.model;

/**
 *
 * @author olufemioshin
 */
public enum CampaignStatus {
     PENDING,              // optional (draft)
    PENDING_APPROVAL,     // submitted or updated/restarted awaiting approval
    APPROVED,             // approved but not started yet (future start)
    ACTIVE,               // currently showing in mobile app
    STOPPED,              // stopped manually (immediate removal)
    CANCELLED,            // cancelled manually
    COMPLETED             // auto-ended
}
