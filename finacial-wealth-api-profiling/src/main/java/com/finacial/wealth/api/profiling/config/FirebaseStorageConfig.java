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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.InputStream;

@Configuration
public class FirebaseStorageConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseStorageConfig.class);

    @Value("${fcm.service.account.file:}")
    private String serviceAccountPath;

    @Value("${fcm.project.id:}")
    private String projectId;

    @Bean
    @ConditionalOnProperty(name = "firebase.storage.enabled", havingValue = "true")
    public Storage firebaseStorage() throws Exception {

        GoogleCredentials credentials;

        if (serviceAccountPath != null && !serviceAccountPath.trim().isEmpty()) {
            log.info("Initializing Firebase Storage using service account file");

            try (InputStream inputStream
                    = new FileInputStream(serviceAccountPath.trim())) {
                credentials = GoogleCredentials.fromStream(inputStream);
            }

        } else {
            log.info("Initializing Firebase Storage using Application Default Credentials");
            credentials = GoogleCredentials.getApplicationDefault();
        }

        StorageOptions.Builder builder = StorageOptions.newBuilder()
                .setCredentials(credentials);

        if (projectId != null && !projectId.trim().isEmpty()) {
            builder.setProjectId(projectId.trim());
        }

        return builder.build().getService();
    }
}
