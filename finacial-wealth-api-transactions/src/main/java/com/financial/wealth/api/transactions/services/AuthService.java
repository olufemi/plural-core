/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.services;

import com.financial.wealth.api.transactions.models.LoginRequest;
import com.financial.wealth.api.transactions.models.LoginResponse;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author olufemioshin
 */
@Service
public class AuthService {

    private final RestTemplate restTemplate = new RestTemplate();

    // cached token & expiry
    private volatile String cachedToken;
    private volatile Instant tokenExpiry = Instant.EPOCH;

    @Value("${transfaar.auth.login.url}")
    private String loginUrl;

    public synchronized String getBearerToken(LoginRequest req) {
        // If token is valid for >30s, reuse
        if (cachedToken != null && Instant.now().isBefore(tokenExpiry.minusSeconds(30))) {
            return cachedToken;
        }
        // Otherwise login
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> entity = new HttpEntity<>(req, headers);

        ResponseEntity<LoginResponse> resp
                = restTemplate.exchange(loginUrl, HttpMethod.POST, entity, LoginResponse.class);

        LoginResponse body = resp.getBody();
        if (body == null || body.getAccessToken() == null) {
            throw new IllegalStateException("Login failed: empty token");
        }
        cachedToken = body.getAccessToken();
        long seconds = (body.getExpiresIn() == null || body.getExpiresIn() <= 0) ? 3600 : body.getExpiresIn();
        tokenExpiry = Instant.now().plusSeconds(seconds);
        return cachedToken;
    }

    public synchronized void invalidateToken() {
        cachedToken = null;
        tokenExpiry = Instant.EPOCH;
    }

}
