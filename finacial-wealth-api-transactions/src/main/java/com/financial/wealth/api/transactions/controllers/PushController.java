/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.controllers;

/**
 *
 * @author olufemioshin
 */
import com.financial.wealth.api.transactions.domain.DeviceDetails;
import com.financial.wealth.api.transactions.repo.DeviceDetailsRepo;
import com.financial.wealth.api.transactions.repo.DeviceTokenRepositoryCustom;
import com.financial.wealth.api.transactions.services.FcmService;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/push")
public class PushController {

    private final DeviceDetailsRepo repo; // implement with JPA or JDBC
    private final FcmService fcmService;
    private final DeviceTokenRepositoryCustom deviceTokenRepositoryCustom;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterTokenRq rq) {
        deviceTokenRepositoryCustom.upsert(rq.getUserId(), rq.getPlatform(), rq.getDeviceToken());
        return ResponseEntity.ok().build();
    }

    // Send to a single token (client supplies token)
    @PostMapping("/send")
    public ResponseEntity<?> send(@RequestBody SendRq rq) throws Exception {
        Map<String, String> data = new HashMap<String, String>();
        data.put("type", "ALERT");            // sample custom data
        if (rq.getData() != null) {
            data.putAll(rq.getData());
        }

        fcmService.sendToToken(
                rq.getDeviceToken(),
                rq.getTitle(),
                rq.getBody(),
                data
        );
        return ResponseEntity.ok().build();
    }

    // Fan-out to all of a user’s devices (optional helper)
    @PostMapping("/sendToUser")
    public ResponseEntity<?> sendToUser(@RequestBody SendToUserRq rq) throws Exception {
        Map<String, String> data = new HashMap<String, String>();
        data.put("type", "ALERT");
        if (rq.getData() != null) {
            data.putAll(rq.getData());
        }

        for (DeviceDetails dt : repo.findAllByWalletId(rq.getUserId())) {
            fcmService.sendToToken(dt.getToken(), rq.getTitle(), rq.getBody(), data);
        }
        return ResponseEntity.ok().build();
    }

    // ===== DTOs =====
    @Data
    public static class RegisterTokenRq {

        private String userId;        // required
        private String platform;      // ANDROID or IOS
        private String deviceToken;   // FCM token from client
    }

    @Data
    public static class SendRq {

        private String deviceToken;   // send to one token
        private String title;
        private String body;
        private Map<String, String> data; // optional custom data
    }

    @Data
    public static class SendToUserRq {

        private String userId;        // fan-out by user
        private String title;
        private String body;
        private Map<String, String> data; // optional
    }
}
