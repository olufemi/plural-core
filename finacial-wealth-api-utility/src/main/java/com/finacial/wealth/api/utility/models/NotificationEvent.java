/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.utility.models;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.utility.enumm.NotificationModule;
import com.finacial.wealth.api.utility.enumm.NotificationProcess;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

public class NotificationEvent implements Serializable {
    private String eventId;            // UUID from publisher (idempotency)
    private NotificationModule module;
    private NotificationProcess process;

    private String userId;
    private String email;              // for EMAIL
    private String pushToken;          // for PUSH (FCM token)
    private String title;
    private String message;

    private Map<String, Object> data;  // extra: amounts, refs, etc.
    private Instant createdAt;

    // getters/setters

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public NotificationModule getModule() {
        return module;
    }

    public void setModule(NotificationModule module) {
        this.module = module;
    }

    public NotificationProcess getProcess() {
        return process;
    }

    public void setProcess(NotificationProcess process) {
        this.process = process;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPushToken() {
        return pushToken;
    }

    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    
}
