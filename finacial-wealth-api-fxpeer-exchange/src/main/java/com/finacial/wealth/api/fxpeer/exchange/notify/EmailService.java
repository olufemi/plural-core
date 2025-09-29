/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.fxpeer.exchange.notify;

/**
 *
 * @author olufemioshin
 */
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender sender;

    public EmailService(JavaMailSender sender) {
        this.sender = sender;
    }

    public void sendWithPdf(String to, String subject, String htmlBody, byte[] pdfBytes, String filename) {
        try {
            MimeMessage msg = sender.createMimeMessage();
            MimeMessageHelper h = new MimeMessageHelper(msg, true);
            h.setTo(to);
            h.setSubject(subject);
            h.setText(htmlBody, true);
            if (pdfBytes != null) {
                h.addAttachment(filename, new ByteArrayResource(pdfBytes));
            }
            sender.send(msg);
        } catch (Exception e) {
            throw new RuntimeException("Email send failed", e);
        }
    }
}
