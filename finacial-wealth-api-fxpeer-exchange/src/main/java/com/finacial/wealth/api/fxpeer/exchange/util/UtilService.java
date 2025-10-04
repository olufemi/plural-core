/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.stereotype.Service;

/**
 *
 * @author olufemioshin
 */
@Service
public class UtilService {

    public String getClaimFromJwt(String authorization, String claimName) {
        try {
            if (authorization == null) {
                return null;
            }
            String token = authorization.replaceFirst("(?i)^Bearer\\s+", "").trim();
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return null;
            }

            // Base64URL decode payload
            byte[] payload = Base64.getUrlDecoder().decode(parts[1]);
            String json = new String(payload, StandardCharsets.UTF_8);

            JsonObject obj = new Gson().fromJson(json, JsonObject.class);
            return obj.has(claimName) && !obj.get(claimName).isJsonNull()
                    ? obj.get(claimName).getAsString()
                    : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
