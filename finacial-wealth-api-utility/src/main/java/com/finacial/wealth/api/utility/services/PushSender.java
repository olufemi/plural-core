package com.finacial.wealth.api.utility.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

@Service
public class PushSender {

    private static final Logger log = LoggerFactory.getLogger(PushSender.class);
    private static final String FCM_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";

    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${fcm.push.enabled:false}")
    private boolean enabled;

    @Value("${fcm.project.id:}")
    private String projectId;

    @Value("${fcm.service.account.file:}")
    private String serviceAccountPath;

    private volatile GoogleCredentials cachedCredentials;

    public void send(String token, String title, String body, Map<String, Object> data) {
        if (!enabled) {
            log.debug("FCM push disabled; skipping push notification title={}", title);
            return;
        }
        if (isBlank(token) || isBlank(projectId) || isBlank(serviceAccountPath)) {
            log.warn("FCM push skipped because token/project/service account config is missing");
            return;
        }

        try {
            Map<String, Object> request = new HashMap<>();
            Map<String, Object> message = new HashMap<>();
            Map<String, String> notification = new HashMap<>();
            notification.put("title", safe(title));
            notification.put("body", safe(body));
            message.put("token", token);
            message.put("notification", notification);
            message.put("data", stringifyData(data));
            request.put("message", message);
            request.put("validate_only", false);

            byte[] payload = mapper.writeValueAsBytes(request);
            URL url = new URL("https://fcm.googleapis.com/v1/projects/" + projectId + "/messages:send");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Authorization", "Bearer " + accessToken());
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            try (OutputStream output = connection.getOutputStream()) {
                output.write(payload);
            }

            int status = connection.getResponseCode();
            if (status >= 400) {
                log.warn("FCM push failed status={} title={}", status, title);
            } else {
                log.info("FCM push sent status={} title={}", status, title);
            }
            connection.disconnect();
        } catch (Exception ex) {
            log.warn("FCM push send failed title={}", title, ex);
        }
    }

    private String accessToken() throws Exception {
        GoogleCredentials credentials = credentials();
        credentials.refreshIfExpired();
        AccessToken token = credentials.getAccessToken();
        if (token == null || token.getExpirationTime() == null
                || token.getExpirationTime().before(new Date(System.currentTimeMillis() + 60000))) {
            credentials.refresh();
            token = credentials.getAccessToken();
        }
        return token.getTokenValue();
    }

    private GoogleCredentials credentials() throws Exception {
        if (cachedCredentials != null) {
            return cachedCredentials;
        }
        try (InputStream input = openServiceAccount()) {
            cachedCredentials = GoogleCredentials.fromStream(input)
                    .createScoped(Collections.singletonList(FCM_SCOPE));
            return cachedCredentials;
        }
    }

    private InputStream openServiceAccount() throws Exception {
        String path = serviceAccountPath.trim();
        if (path.startsWith("file:")) {
            return new FileSystemResource(path.substring("file:".length())).getInputStream();
        }
        if (path.startsWith("/")) {
            return new FileSystemResource(path).getInputStream();
        }
        return new ClassPathResource(path).getInputStream();
    }

    private static Map<String, String> stringifyData(Map<String, Object> data) {
        Map<String, String> result = new HashMap<>();
        if (data == null) {
            return result;
        }
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                result.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        return result;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
