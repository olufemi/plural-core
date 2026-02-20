/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.sessionmanager.email;

/**
 *
 * @author olufemioshin
 */
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import lombok.Data;
@Data
public class EmailJob implements Serializable {
    private String jobId;          // UUID for idempotency
    private String to;
    private String subject;
    private String template;       // e.g. WALLET_DEPOSIT, SIGNUP_SUCCESS
    private Map<String, Object> data;
    private Instant createdAt;

    // getters/setters
}
