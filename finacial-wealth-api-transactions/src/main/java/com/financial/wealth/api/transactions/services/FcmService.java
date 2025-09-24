/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.services;

/**
 *
 * @author olufemioshin
 */
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class FcmService {

    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${fcm.project.id}")
    private String projectId;

    @Value("${fcm.service.account.file}")
    private String serviceAccountPath;

    private volatile GoogleCredentials cachedCreds;

    /**
     * Java 8-friendly loader: absolute path -> filesystem, else classpath.
     */
    private InputStream openServiceAccount() throws IOException {
        if (serviceAccountPath.startsWith("file:")) {
            return new FileSystemResource(serviceAccountPath.substring("file:".length())).getInputStream();
        }
        if (serviceAccountPath.startsWith("/")) {
            return new FileSystemResource(serviceAccountPath).getInputStream();
        }
        return new ClassPathResource(serviceAccountPath).getInputStream();
    }

    private GoogleCredentials credentials() throws IOException {
        if (cachedCreds != null) {
            return cachedCreds;
        }
        InputStream in = openServiceAccount();
        cachedCreds = GoogleCredentials.fromStream(in)
                .createScoped(Collections.singletonList("https://www.googleapis.com/auth/firebase.messaging"));
        return cachedCreds;
    }

    private String getAccessToken() throws IOException {
        GoogleCredentials creds = credentials();
        creds.refreshIfExpired();
        AccessToken token = creds.getAccessToken();
        if (token == null || token.getExpirationTime() == null
                || token.getExpirationTime().before(new Date(System.currentTimeMillis() + 60_000))) {
            creds.refresh();
            token = creds.getAccessToken();
        }
        return token.getTokenValue();
    }

    /**
     * Send a simple notification + optional data to a single device token.
     */
    public void sendToToken(String deviceToken, String title, String body, Map<String, String> data) throws Exception {
        String url = "https://fcm.googleapis.com/v1/projects/" + projectId + "/messages:send";

        // Build payload using small DTOs (Java 8 safe)
        Notification n = new Notification();
        n.title = (title == null ? "" : title);
        n.body = (body == null ? "" : body);

        Message msg = new Message();
        msg.token = deviceToken;
        msg.notification = n;
        msg.data = (data == null ? new HashMap<String, String>() : data);

        FcmV1Request req = new FcmV1Request();
        req.message = msg;
        req.validate_only = false;

        String json = mapper.writeValueAsString(req);

        HttpPost post = new HttpPost(url);
        post.setHeader("Authorization", "Bearer " + getAccessToken());
        post.setHeader("Content-Type", "application/json; charset=UTF-8");
        post.setEntity(new StringEntity(json, StandardCharsets.UTF_8));

        CloseableHttpClient http = HttpClients.createDefault();
        CloseableHttpResponse resp = null;
        try {
            resp = http.execute(post);
            int code = resp.getStatusLine().getStatusCode();
            HttpEntity entity = resp.getEntity();
            String respBody = (entity != null)
                    ? EntityUtils.toString(entity, StandardCharsets.UTF_8)
                    : "";
            log.info("FCM response: {} {}", code, respBody);
            if (code >= 400) {
                throw new RuntimeException("FCM error " + code + ": " + respBody);
            }
        } finally {
            try {
                if (resp != null && resp.getEntity() != null) {
                    EntityUtils.consumeQuietly(resp.getEntity());
                }
            } catch (Exception ignore) {
            }
            try {
                if (resp != null) {
                    resp.close();
                }
            } catch (Exception ignore) {
            }
            try {
                http.close();
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * ===== DTOs for FCM HTTP v1 payload (Java 8, no Lombok needed) =====
     */
    static class FcmV1Request {

        public Message message;
        public boolean validate_only;
    }

    static class Message {

        public Notification notification;
        public Map<String, String> data;
        public String token;
    }

    static class Notification {

        public String title;
        public String body;
    }
}
