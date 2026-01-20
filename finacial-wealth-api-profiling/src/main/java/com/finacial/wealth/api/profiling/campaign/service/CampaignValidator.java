/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.campaign.service;

/**
 *
 * @author olufemioshin
 */
import java.net.URL;
import java.util.Date;

public final class CampaignValidator {

    private CampaignValidator(){}

    public static void validateDates(Date startAt, Date endAt) {
        if (startAt == null || endAt == null) {
            throw new IllegalArgumentException("startAt and endAt are required");
        }
        if (!endAt.after(startAt)) {
            throw new IllegalArgumentException("endAt must be after startAt");
        }
        Date now = new Date();
        // “present or future” start date – allow small clock skew by not being too strict if you want
        if (startAt.before(now)) {
            throw new IllegalArgumentException("startAt must be present or future");
        }
    }

    public static void validateUrl(String embeddedLink) {
        if (embeddedLink == null || embeddedLink.trim().isEmpty()) {
            throw new IllegalArgumentException("embeddedLink is required");
        }
        try {
            new URL(embeddedLink.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("embeddedLink must be a valid URL");
        }
    }

    public static void validateMedia(String mediaObjectName) {
        if (mediaObjectName == null || mediaObjectName.trim().isEmpty()) {
            throw new IllegalArgumentException("mediaObjectName is required (upload media first)");
        }
    }
}

