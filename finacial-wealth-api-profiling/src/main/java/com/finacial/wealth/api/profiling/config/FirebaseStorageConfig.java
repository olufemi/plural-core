/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.profiling.config;

/**
 *
 * @author olufemioshin
 */
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;

@Configuration
public class FirebaseStorageConfig {

    @Value("${fcm.service.account.file}")
    private String serviceAccountPath;

    @Value("${fcm.project.id")
    private String projectId;

    @Bean
    public Storage firebaseStorage() throws Exception {
        FileInputStream in = new FileInputStream(serviceAccountPath);
        GoogleCredentials creds = GoogleCredentials.fromStream(in);

        return StorageOptions.newBuilder()
                .setProjectId(projectId)
                .setCredentials(creds)
                .build()
                .getService();
    }
}
