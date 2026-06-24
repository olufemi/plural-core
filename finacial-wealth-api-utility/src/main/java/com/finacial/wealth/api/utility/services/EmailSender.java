/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.utility.services;

/**
 *
 * @author olufemioshin
 */
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;

@Service
public class EmailSender {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:support@finacialwealth.com}")
    private String from;

    @Value("${spring.mail.username:}")
    private String username;

    @Value("${spring.mail.password:}")
    private String password;

    public EmailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendHtml(String to, String subject, String html) {
        try {
            validateMailConfig();
            validateMessage(to, subject, html);

            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    msg, false, StandardCharsets.UTF_8.name()
            );

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true); // true = HTML

            mailSender.send(msg);
        } catch (Exception e) {
            throw new RuntimeException("Email send failed: " + e.getMessage(), e);
        }
    }

    public void sendHtmlWithAttachment(String to, String subject, String html,
                                       String filename, byte[] bytes, String contentType) {
        try {
            validateMailConfig();
            validateMessage(to, subject, html);

            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    msg, true, StandardCharsets.UTF_8.name()
            );

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            helper.addAttachment(filename, new ByteArrayResource(bytes), contentType);

            mailSender.send(msg);
        } catch (Exception e) {
            throw new RuntimeException("Email send failed: " + e.getMessage(), e);
        }
    }

    private void validateMailConfig() {
        if (isBlank(from)) {
            throw new IllegalStateException("SMTP from address is not configured. Set SMTP_FROM or app.mail.from.");
        }
        if (isBlank(username)) {
            throw new IllegalStateException("SMTP username is not configured. Set SMTP_USERNAME or spring.mail.username.");
        }
        if (isBlank(password)) {
            throw new IllegalStateException("SMTP password is not configured. Set SMTP_PASSWORD or spring.mail.password.");
        }
    }

    private void validateMessage(String to, String subject, String html) {
        if (isBlank(to)) {
            throw new IllegalArgumentException("Email recipient is required.");
        }
        if (isBlank(subject)) {
            throw new IllegalArgumentException("Email subject is required.");
        }
        if (isBlank(html)) {
            throw new IllegalArgumentException("Email body is required.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
