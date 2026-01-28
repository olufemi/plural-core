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
public class FileItem {
    private String name;        // Firebase objectName (IMPORTANT)
    private String fileName;
    private Long size;
    private String contentType;
    private Long updatedAtMs;
    private String signedUrl;
}

