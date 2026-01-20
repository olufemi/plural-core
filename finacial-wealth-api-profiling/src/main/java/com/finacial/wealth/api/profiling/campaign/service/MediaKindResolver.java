/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.campaign.service;

import com.finacial.wealth.api.profiling.campaign.ennum.MediaKind;

/**
 *
 * @author olufemioshin
 */
public final class MediaKindResolver {

    private MediaKindResolver(){}

     public static MediaKind resolve(String contentType, String objectName) {
        String ct = contentType == null ? "" : contentType.toLowerCase();
        String name = objectName == null ? "" : objectName.toLowerCase();

        if (ct.startsWith("image/")) {
            if (ct.contains("gif") || name.endsWith(".gif")) return MediaKind.GIF;
            return MediaKind.IMAGE;
        }
        if (ct.startsWith("video/")) return MediaKind.VIDEO;

        if (ct.contains("pdf") || name.endsWith(".pdf")) return MediaKind.PDF;
        if (name.endsWith(".ppt") || name.endsWith(".pptx")) return MediaKind.SLIDE;

        if (name.endsWith(".mp4") || name.endsWith(".mov") || name.endsWith(".mkv")) return MediaKind.VIDEO;
        if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".webp")) return MediaKind.IMAGE;

        return MediaKind.OTHER;
    }
}

