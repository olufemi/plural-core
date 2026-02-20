/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.sessionmanager.email;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 *
 * @author olufemioshin
 */
@Service
public class EmailPublisher {

   private final RabbitTemplate rabbitTemplate;

    // keep these constants identical across services
    private static final String EXCHANGE = "finwealth.email.exchange";
    private static final String ROUTING_KEY = "finwealth.email";

    public EmailPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Generic publisher:
     * Utility-service will choose template using module+process and will render HTML using data.
     */
    public void publish(String module,
                        String process,
                        String recipientEmail,
                        String recipientName,
                        Map<String, Object> data) {

        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("eventId", UUID.randomUUID().toString());
        payload.put("module", module);
        payload.put("process", process);
        payload.put("createdAt", Instant.now().toString());

        Map<String, Object> recipient = new HashMap<String, Object>();
        recipient.put("email", recipientEmail);
        recipient.put("name", recipientName);

        payload.put("recipient", recipient);
        payload.put("data", data == null ? new HashMap<String, Object>() : data);

        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, payload);
    }
}
