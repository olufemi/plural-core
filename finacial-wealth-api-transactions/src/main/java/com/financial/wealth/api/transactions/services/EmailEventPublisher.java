package com.financial.wealth.api.transactions.services;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${finwealth.email.exchange:finwealth.email.exchange}")
    private String emailExchange;

    @Value("${finwealth.email.routing-key:finwealth.email}")
    private String emailRoutingKey;

    @Value("${finwealth.email.enabled:true}")
    private boolean emailEnabled;

    public EmailEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishWalletDeposit(String email, String name, String amount, String currency, String ref) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("amount", amount);
        data.put("currency", currency);
        data.put("ref", ref);

        publish("WALLET", "SUCCESSFUL_WALLET_DEPOSIT", email, name, data);
    }

    public void publish(String module, String process, String email, String name, Map<String, Object> data) {
        if (!emailEnabled || isBlank(email)) {
            return;
        }

        String eventId = UUID.randomUUID().toString();
        Map<String, Object> recipient = new LinkedHashMap<>();
        recipient.put("email", email);
        recipient.put("name", name);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventId", eventId);
        payload.put("module", module);
        payload.put("process", process);
        payload.put("createdAt", Instant.now().toString());
        payload.put("recipient", recipient);
        payload.put("data", data);

        rabbitTemplate.convertAndSend(emailExchange, emailRoutingKey, payload, message -> withEventHeaders(message, eventId));
    }

    private Message withEventHeaders(Message message, String eventId) {
        message.getMessageProperties().setMessageId(eventId);
        message.getMessageProperties().setCorrelationId(eventId);
        return message;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
