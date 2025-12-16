/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.storage.firebase;

/**
 *
 * @author olufemioshin
 */
import lombok.Data;

@Data
public class FileItem {
    private String name;        // object name in bucket (e.g. slides/a.pptx)
    private String fileName;    // convenience (e.g. a.pptx)
    private Long size;
    private String contentType;
    private String signedUrl;   // optional
    private Long updatedAtMs;
}
