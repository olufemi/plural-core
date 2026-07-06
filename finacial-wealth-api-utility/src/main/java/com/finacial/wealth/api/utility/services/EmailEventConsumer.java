/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.utility.services;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.utility.config.RabbitConfig;
import com.finacial.wealth.api.utility.models.EmailEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class EmailEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(EmailEventConsumer.class);

    private final EmailSender emailSender;
    private final EmailTemplateService templateService;
    private final ObjectMapper mapper;

    public EmailEventConsumer(EmailSender emailSender,
            EmailTemplateService templateService,
            ObjectMapper mapper) {
        this.emailSender = emailSender;
        this.templateService = templateService;
        this.mapper = mapper;
    }

    @RabbitListener(
            queues = RabbitConfig.EMAIL_QUEUE,
            containerFactory = "rabbitListenerContainerFactory"
    )
    public void consume(Message message) {
        String payload = null;

        try {
            payload = new String(message.getBody(), StandardCharsets.UTF_8);

            // parse explicitly (your DTO can have @JsonIgnoreProperties(ignoreUnknown = true))
            EmailEvent evt = mapper.readValue(payload, EmailEvent.class);

            String to = (evt.getRecipient() == null) ? null : evt.getRecipient().getEmail();
            if (to == null || to.trim().isEmpty()) {
                // bad message => DLQ (if configured), no retry loop
                throw new AmqpRejectAndDontRequeueException("Missing recipient.email. payload=" + payload);
            }

            String subject = templateService.subject(evt.getModule(), evt.getProcess(), evt.getData());
            String html = templateService.html(evt.getModule(), evt.getProcess(), evt.getRecipient(), evt.getData());

            emailSender.sendHtml(to, subject, html);

        } catch (JsonProcessingException e) {
            throw new AmqpRejectAndDontRequeueException(
                    "Invalid EmailEvent payload. payload=" + payload + ", err=" + e.getMessage(), e
            );
        } catch (AmqpRejectAndDontRequeueException e) {
            throw e;
        } catch (Exception e) {
            log.error("EmailEvent send failed; event acknowledged to avoid retry loop. payload={}, err={}",
                    payload, e.getMessage(), e);
        }
    }
}
