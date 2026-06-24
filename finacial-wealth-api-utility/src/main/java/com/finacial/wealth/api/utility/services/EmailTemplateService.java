/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.finacial.wealth.api.utility.services;

/**
 *
 * @author olufemioshin
 */
import com.finacial.wealth.api.utility.models.EmailEvent;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class EmailTemplateService {

    public String subject(String module, String process, Map<String, Object> data) {
        if ("ONBOARDING".equalsIgnoreCase(module) && "SUCCESSFUL_SIGNUP".equalsIgnoreCase(process)) {
            return "Welcome to PluralApp";
        }
        if ("AUTHENTICATION".equalsIgnoreCase(module) && "LOGIN".equalsIgnoreCase(process)) {
            return "New Login to Your PluralApp Account";
        }
        if ("WALLET".equalsIgnoreCase(module) && "SUCCESSFUL_WALLET_DEPOSIT".equalsIgnoreCase(process)) {
            return "Wallet Deposit Successful";
        }
        if ("WALLET".equalsIgnoreCase(module) && "SUCCESSFUL_WALLET_WITHDRAWAL".equalsIgnoreCase(process)) {
            return "Wallet Withdrawal Successful";
        }
        if ("WALLET".equalsIgnoreCase(module) && "SUCCESSFUL_LOCAL_TRANSFER_SENT".equalsIgnoreCase(process)) {
            return "Transfer Sent Successfully";
        }
        if ("WALLET".equalsIgnoreCase(module) && "SUCCESSFUL_LOCAL_TRANSFER_RECEIVED".equalsIgnoreCase(process)) {
            return "Transfer Received Successfully";
        }
        return "PluralApp Notification";
    }

    public String html(String module, String process, EmailEvent.Recipient recipient, Map<String, Object> data) {
        String name = recipient != null && recipient.getName() != null ? recipient.getName() : "Customer";

        if ("WALLET".equalsIgnoreCase(module) && "SUCCESSFUL_WALLET_DEPOSIT".equalsIgnoreCase(process)) {
            String amount = str(data, "amount");
            String currency = str(data, "currency");
            String ref = str(data, "ref");

            return wrap(
                    "<h3>Hi " + esc(name) + ",</h3>"
                    + "<p>Your wallet has been credited successfully.</p>"
                    + "<p><b>Amount:</b> " + esc(currency) + " " + esc(amount) + "<br/>"
                    + "<b>Reference:</b> " + esc(ref) + "</p>"
                    + "<p>Thanks,<br/>PluralApp Support</p>"
            );
        }

        if ("WALLET".equalsIgnoreCase(module) && "SUCCESSFUL_WALLET_WITHDRAWAL".equalsIgnoreCase(process)) {
            return transactionTemplate(name,
                    "Your wallet has been debited successfully.",
                    str(data, "currency"),
                    str(data, "amount"),
                    str(data, "ref"));
        }

        if ("WALLET".equalsIgnoreCase(module) && "SUCCESSFUL_LOCAL_TRANSFER_SENT".equalsIgnoreCase(process)) {
            return transactionTemplate(name,
                    "Your transfer has been sent successfully.",
                    str(data, "currency"),
                    str(data, "amount"),
                    str(data, "ref"));
        }

        if ("WALLET".equalsIgnoreCase(module) && "SUCCESSFUL_LOCAL_TRANSFER_RECEIVED".equalsIgnoreCase(process)) {
            return transactionTemplate(name,
                    "You have received a transfer.",
                    str(data, "currency"),
                    str(data, "amount"),
                    str(data, "ref"));
        }

        // default
        return wrap("<h3>Hi " + esc(name) + ",</h3><p>" + esc(str(data, "message")) + "</p>");
    }

    private String transactionTemplate(String name, String message, String currency, String amount, String ref) {
        return wrap(
                "<h3>Hi " + esc(name) + ",</h3>"
                + "<p>" + esc(message) + "</p>"
                + "<p><b>Amount:</b> " + esc(currency) + " " + esc(amount) + "<br/>"
                + "<b>Reference:</b> " + esc(ref) + "</p>"
                + "<p>Thanks,<br/>PluralApp Support</p>"
        );
    }

    private String wrap(String body) {
        return "<div style='font-family:Arial, sans-serif; font-size:14px; color:#111'>"
                + body
                + "</div>";
    }

    private String str(Map<String, Object> data, String key) {
        if (data == null) {
            return "";
        }
        Object v = data.get(key);
        return v == null ? "" : String.valueOf(v);
    }

    private String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
