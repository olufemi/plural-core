/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.utility.models;

/**
 *
 * @author olufemioshin
 */
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import lombok.Data;
//@Data
@JsonIgnoreProperties(ignoreUnknown = true) // ✅ prevents future “new field” breaks
public class EmailEvent implements Serializable {

    private String eventId;
    private String module;
    private String process;

    // ✅ add this (maps: "2026-02-16T08:48:50.451Z")
    private Instant createdAt;

    private Recipient recipient;
    private Map<String, Object> data;

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }

    public String getProcess() { return process; }
    public void setProcess(String process) { this.process = process; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Recipient getRecipient() { return recipient; }
    public void setRecipient(Recipient recipient) { this.recipient = recipient; }

    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Recipient implements Serializable {
        private String email;
        private String name;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}


