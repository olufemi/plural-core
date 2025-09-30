/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.financial.wealth.api.transactions.services.notify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financial.wealth.api.transactions.domain.DeviceDetails;
import com.financial.wealth.api.transactions.domain.NotificationPushed;
import com.financial.wealth.api.transactions.domain.UserNotification;
import com.financial.wealth.api.transactions.repo.DeviceDetailsRepo;
import com.financial.wealth.api.transactions.repo.NotificationRepository;
import com.financial.wealth.api.transactions.repo.UserNotificationRepository;
import com.google.gson.Gson;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 *
 * @author olufemioshin
 */
@Service
@RequiredArgsConstructor
public class MessageCenterService {

    private final NotificationRepository notifRepo;
    private final UserNotificationRepository userNotifRepo;
    private final DeviceDetailsRepo deviceTokenRepo; // your existing repo

    private final ObjectMapper mapper = new ObjectMapper();
    private final FcmService fcm; // your class with sendToToken (see below for enhancements)

    @Transactional
    public Long createAndPushToUser(String userId, String title, String body,
            Map<String, String> data, Integer ttlSeconds, String collapseKey) throws Exception {
        NotificationPushed n = new NotificationPushed();
        n.setTitle(title);
        n.setBody(body);
        n.setDataJson(data == null ? "{}" : mapper.writeValueAsString(data));
        n.setTtlSeconds(ttlSeconds == null ? 86400 : ttlSeconds); // default 24h
        n.setCollapseKey(collapseKey);
        if (n.getTtlSeconds() != null) {
            n.setExpiresAt(new Date(System.currentTimeMillis() + n.getTtlSeconds() * 1000L));
        }
        n = notifRepo.save(n);

        UserNotification un = new UserNotification();
        un.setUserId(userId);
        un.setNotification(n);
        un.setCreatedBy("System");
        un.setCreatedDate(Instant.now());
       // un.setDeliveredAt(new Date());
       // un.setReadAt(new Date());

        //un = userNotifRepo.save(un);
        System.out.println("NotificationPushed ::::::::::::::::  %S  " + n);
        System.out.println("UserNotification ::::::::::::::::  %S  " + un);

        // Try push (non-blocking of DB durability)
        List<DeviceDetails> tokens = deviceTokenRepo.findAllByWalletId(userId);
        System.out.println("List<DeviceDetails> tokens ::::::::::::::::  %S  " + new Gson().toJson(tokens));

        for (DeviceDetails t : tokens) {
            try {
                System.out.println("finally sending data ::::::::::::::::  %S  " + t.getToken());

                fcm.sendToTokenWithBroadCast(t.getToken(), title, body, data);
                //fcm.sendToToken(t.getToken(), title, body, data);
                // public void sendToTokenWithBroadCast(String deviceToken, String title, String body, Map<String, String> data) throws Exception {

                un.setStatus("SENT");
                un.setSentAt(new Date());
            } catch (Exception ex) {
                ex.printStackTrace();
                un.setStatus("FAILED");
                un.setLastError(ex.getMessage());
            }
        }
        userNotifRepo.save(un);

        return n.getId();
    }

    /**
     * Broadcast by iterating users or (preferable) via topics; still store a
     * row per user
     */
    @Transactional
    public void broadcastToUsers(Collection<String> userIds, String title, String body,
            Map<String, String> data, Integer ttlSeconds, String collapseKey) throws Exception {
        for (String uid : userIds) {
            createAndPushToUser(uid, title, body, data, ttlSeconds, collapseKey);
        }
    }

    //@Transactional(readOnly = true)
    public Page<Map<String, Object>> listInbox(String userId, int page, int size, boolean unreadOnly) {
        Collection<String> statuses = unreadOnly ? Arrays.asList("NEW", "SENT")
                : Arrays.asList("NEW", "SENT", "DELIVERED", "READ", "FAILED");
        Page<UserNotification> p = userNotifRepo
                .findByUserIdAndStatusInOrderByIdDesc(userId, statuses, PageRequest.of(page, size));

        return p.map(un -> {
            NotificationPushed n = un.getNotification();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("userNotificationId", un.getId());
            m.put("status", un.getStatus());
            m.put("sentAt", ts(un.getSentAt()));
            m.put("readAt", ts(un.getReadAt()));
            m.put("title", n.getTitle());
            m.put("body", n.getBody());
            m.put("data", parseOrEmpty(n.getDataJson()));
            m.put("collapseKey", n.getCollapseKey());
            m.put("createdAt", ts(n.getCreatedAt()));
            m.put("expiresAt", ts(n.getExpiresAt()));
            return m;
        });
    }

    @Transactional
    public void markRead(String userId, Long userNotificationId) {
        UserNotification un = userNotifRepo.findByIdAndUserId(userNotificationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Not found"));
        un.setStatus("READ");
        un.setReadAt(new Date());
        userNotifRepo.save(un);
    }

    private static Long ts(Date d) {
        return d == null ? null : d.getTime();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseOrEmpty(String json) {
        try {
            return json == null ? new HashMap<>() : new ObjectMapper().readValue(json, Map.class);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
}
