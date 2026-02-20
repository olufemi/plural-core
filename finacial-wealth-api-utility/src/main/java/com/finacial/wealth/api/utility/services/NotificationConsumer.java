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
import com.finacial.wealth.api.utility.enumm.NotificationChannel;
import com.finacial.wealth.api.utility.models.NotificationEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class NotificationConsumer {

    private final EmailSender emailSender;
    // private final PushSender pushSender; // your FCM sender wrapper

    public NotificationConsumer(EmailSender emailSender
       //   ,  PushSender pushSender
    ) {
        this.emailSender = emailSender;
        //  this.pushSender = pushSender;
    }

    @RabbitListener(queues = RabbitConfig.NOTIF_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void consume(NotificationEvent evt) {

        Set<NotificationChannel> channels
                = NotificationChannelPolicy.channels(evt.getModule(), evt.getProcess());

        // EMAIL
        if (channels.contains(NotificationChannel.EMAIL)) {
            if (evt.getEmail() != null && !evt.getEmail().trim().isEmpty()) {
                String html = EmailTemplates.render(evt); // build nice HTML per process
                emailSender.sendHtml(evt.getEmail(), EmailTemplates.subject(evt), html);
            }
        }

        // PUSH
        if (channels.contains(NotificationChannel.PUSH)) {
            if (evt.getPushToken() != null && !evt.getPushToken().trim().isEmpty()) {
                //pushSender.send(evt.getPushToken(), evt.getTitle(), evt.getMessage(), evt.getData());
            }
        }
    }
}
