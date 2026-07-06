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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailSender {

    private static final Logger log = LoggerFactory.getLogger(EmailSender.class);

    private static final String PROVIDER_SENDGRID = "SENDGRID";
    private static final String SENDGRID_MAIL_SEND_URL = "https://api.sendgrid.com/v3/mail/send";

    private final JavaMailSender mailSender;
    private final RestTemplate restTemplate;

    @Value("${app.email.provider:SMTP}")
    private String emailProvider;

    @Value("${app.mail.from:support@finacialwealth.com}")
    private String from;

    @Value("${spring.mail.username:}")
    private String username;

    @Value("${spring.mail.password:}")
    private String password;

    @Value("${sendgrid.api.key:}")
    private String sendgridApiKey;

    @Value("${sendgrid.from.email:${app.mail.from:support@finacialwealth.com}}")
    private String sendgridFromEmail;

    @Value("${sendgrid.from.name:Plural}")
    private String sendgridFromName;

    public EmailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
        this.restTemplate = new RestTemplate();
    }

    public void sendHtml(String to, String subject, String html) {
        try {
            validateMessage(to, subject, html);
            if (isSendGridProvider()) {
                sendGridHtml(to, subject, html, Collections.<Map<String, Object>>emptyList());
                return;
            }

            validateMailConfig();
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    msg, false, StandardCharsets.UTF_8.name()
            );

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true); // true = HTML

            mailSender.send(msg);
            log.info("Email sent successfully via SMTP. to={}, subject={}, from={}", to, subject, from);
        } catch (Exception e) {
            log.error("Email send failed. provider=SMTP, to={}, subject={}, error={}", safe(to), safe(subject), e.getMessage(), e);
            throw new RuntimeException("Email send failed: " + e.getMessage(), e);
        }
    }

    public void sendHtmlWithAttachment(String to, String subject, String html,
                                       String filename, byte[] bytes, String contentType) {
        try {
            validateMessage(to, subject, html);
            validateAttachment(filename, bytes, contentType);
            if (isSendGridProvider()) {
                sendGridHtml(to, subject, html, Collections.singletonList(sendGridAttachment(filename, bytes, contentType)));
                return;
            }

            validateMailConfig();
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
            log.info("Email sent successfully via SMTP. to={}, subject={}, from={}, attachment={}", to, subject, from, filename);
        } catch (Exception e) {
            log.error("Email send failed. provider=SMTP, to={}, subject={}, attachment={}, error={}", safe(to), safe(subject), safe(filename), e.getMessage(), e);
            throw new RuntimeException("Email send failed: " + e.getMessage(), e);
        }
    }

    private void sendGridHtml(String to, String subject, String html, List<Map<String, Object>> attachments) {
        validateSendGridConfig();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(sendgridApiKey);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("personalizations", Collections.singletonList(personalization(to, subject)));
        payload.put("from", emailAddress(sendgridFromEmail, sendgridFromName));
        payload.put("content", Collections.singletonList(content("text/html", html)));
        if (attachments != null && !attachments.isEmpty()) {
            payload.put("attachments", attachments);
        }

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    SENDGRID_MAIL_SEND_URL,
                    HttpMethod.POST,
                    new HttpEntity<>(payload, headers),
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new IllegalStateException("SendGrid returned " + response.getStatusCodeValue() + ": " + response.getBody());
            }
            log.info("Email sent successfully via SendGrid. to={}, subject={}, from={}, status={}, messageId={}, attachments={}",
                    to,
                    subject,
                    sendgridFromEmail,
                    response.getStatusCodeValue(),
                    response.getHeaders().getFirst("X-Message-Id"),
                    attachments == null ? 0 : attachments.size());
        } catch (HttpStatusCodeException e) {
            log.error("Email send failed. provider=SENDGRID, to={}, subject={}, status={}, response={}, error={}",
                    safe(to), safe(subject), e.getStatusCode().value(), safe(e.getResponseBodyAsString()), e.getMessage(), e);
            throw new RuntimeException("SendGrid send failed: " + e.getStatusCode().value() + " " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Email send failed. provider=SENDGRID, to={}, subject={}, error={}", safe(to), safe(subject), e.getMessage(), e);
            throw new RuntimeException("SendGrid send failed: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> personalization(String to, String subject) {
        Map<String, Object> personalization = new LinkedHashMap<>();
        personalization.put("to", Collections.singletonList(emailAddress(to, null)));
        personalization.put("subject", subject);
        return personalization;
    }

    private Map<String, Object> emailAddress(String email, String name) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("email", email);
        if (!isBlank(name)) {
            value.put("name", name);
        }
        return value;
    }

    private Map<String, Object> content(String type, String value) {
        Map<String, Object> content = new LinkedHashMap<>();
        content.put("type", type);
        content.put("value", value);
        return content;
    }

    private Map<String, Object> sendGridAttachment(String filename, byte[] bytes, String contentType) {
        Map<String, Object> attachment = new LinkedHashMap<>();
        attachment.put("content", Base64.getEncoder().encodeToString(bytes));
        attachment.put("filename", filename);
        attachment.put("type", contentType);
        attachment.put("disposition", "attachment");
        return attachment;
    }

    private boolean isSendGridProvider() {
        return PROVIDER_SENDGRID.equalsIgnoreCase(emailProvider);
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

    private void validateSendGridConfig() {
        if (isBlank(sendgridApiKey)) {
            throw new IllegalStateException("SendGrid API key is not configured. Set SENDGRID_API_KEY or sendgrid.api.key.");
        }
        if (isBlank(sendgridFromEmail)) {
            throw new IllegalStateException("SendGrid from email is not configured. Set SENDGRID_FROM_EMAIL or sendgrid.from.email.");
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

    private void validateAttachment(String filename, byte[] bytes, String contentType) {
        if (isBlank(filename)) {
            throw new IllegalArgumentException("Attachment filename is required.");
        }
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("Attachment content is required.");
        }
        if (isBlank(contentType)) {
            throw new IllegalArgumentException("Attachment content type is required.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safe(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("[\\r\\n\\t]", " ").trim();
    }
}
