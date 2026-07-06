package com.finacial.wealth.api.utility.services;

import com.finacial.wealth.api.utility.models.EmailEvent;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class EmailTemplateService {

    private static final String APP_NAME = "Plural";
    private static final String SUPPORT_NAME = "Plural Support";

    public String subject(String module, String process, Map<String, Object> data) {
        if ("ONBOARDING".equalsIgnoreCase(module) && "SUCCESSFUL_SIGNUP".equalsIgnoreCase(process)) {
            return "Welcome to Plural";
        }
        if ("AUTHENTICATION".equalsIgnoreCase(module) && "LOGIN".equalsIgnoreCase(process)) {
            return "New Login to Your Plural Account";
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
        return "Plural Notification";
    }

    public String html(String module, String process, EmailEvent.Recipient recipient, Map<String, Object> data) {
        String name = recipient != null && recipient.getName() != null ? recipient.getName() : "Customer";

        if ("WALLET".equalsIgnoreCase(module) && "SUCCESSFUL_WALLET_DEPOSIT".equalsIgnoreCase(process)) {
            return transactionTemplate(name, "Deposit successful",
                    "Your wallet has been credited successfully.", "Credit", data);
        }
        if ("WALLET".equalsIgnoreCase(module) && "SUCCESSFUL_WALLET_WITHDRAWAL".equalsIgnoreCase(process)) {
            return transactionTemplate(name, "Withdrawal successful",
                    "Your wallet has been debited successfully.", "Debit", data);
        }
        if ("WALLET".equalsIgnoreCase(module) && "SUCCESSFUL_LOCAL_TRANSFER_SENT".equalsIgnoreCase(process)) {
            return transactionTemplate(name, "Transfer sent",
                    "Your transfer has been sent successfully.", "Debit", data);
        }
        if ("WALLET".equalsIgnoreCase(module) && "SUCCESSFUL_LOCAL_TRANSFER_RECEIVED".equalsIgnoreCase(process)) {
            return transactionTemplate(name, "Transfer received",
                    "You have received a transfer.", "Credit", data);
        }

        return wrap(name, "Notification", "<p>" + esc(str(data, "message")) + "</p>");
    }

    private String transactionTemplate(String name, String heading, String message, String direction, Map<String, Object> data) {
        String currency = first(data, "currency", "currencyCode");
        String amount = str(data, "amount");
        String ref = first(data, "ref", "transactionId", "reference");
        String counterparty = first(data, "counterparty", "receiverName", "senderName");
        String narration = first(data, "narration", "theNarration", "description");
        String dateTime = first(data, "datetime", "createdAt", "transactionDate");

        String details = row("Direction", direction)
                + row("Amount", currencyAmount(currency, amount))
                + row("Counterparty", counterparty)
                + row("Narration", narration)
                + row("Reference", ref)
                + row("Date", dateTime);

        return wrap(name, heading,
                "<p style='margin:0 0 16px;color:#3f4857;font-size:15px;line-height:1.6;'>"
                + esc(message)
                + "</p>"
                + "<table role='presentation' width='100%' cellpadding='0' cellspacing='0' "
                + "style='border-collapse:collapse;background:#f7f9fc;border:1px solid #e6eaf1;border-radius:8px;overflow:hidden;'>"
                + details
                + "</table>"
                + "<p style='margin:20px 0 0;color:#3f4857;font-size:14px;line-height:1.6;'>"
                + "If you did not authorize this transaction, please contact " + SUPPORT_NAME + " immediately."
                + "</p>");
    }

    private String wrap(String name, String heading, String body) {
        return "<!DOCTYPE html>"
                + "<html lang='en'>"
                + "<head>"
                + "<meta charset='UTF-8'/>"
                + "<meta name='viewport' content='width=device-width,initial-scale=1'/>"
                + "<title>" + esc(heading) + "</title>"
                + "</head>"
                + "<body style='margin:0;padding:0;background:#f5f6fa;font-family:-apple-system,BlinkMacSystemFont,Segoe UI,Arial,sans-serif;'>"
                + "<table role='presentation' width='100%' cellpadding='0' cellspacing='0' style='background:#f5f6fa;padding:24px 0;'>"
                + "<tr><td align='center'>"
                + "<table role='presentation' width='100%' cellpadding='0' cellspacing='0' "
                + "style='max-width:600px;background:#ffffff;border-radius:8px;overflow:hidden;border:1px solid #edf0f5;'>"
                + "<tr><td style='padding:20px 24px;background:#050814;color:#ffffff;font-size:24px;font-weight:700;'>"
                + APP_NAME
                + "</td></tr>"
                + "<tr><td style='padding:24px 20px 28px;color:#222222;'>"
                + "<p style='margin:0 0 8px;color:#7a8494;font-size:13px;text-transform:uppercase;letter-spacing:.08em;'>Transaction notification</p>"
                + "<h1 style='margin:0 0 16px;color:#111827;font-size:24px;line-height:1.25;font-weight:700;'>"
                + esc(heading)
                + "</h1>"
                + "<p style='margin:0 0 16px;color:#3f4857;font-size:15px;line-height:1.6;'>Hi "
                + esc(firstName(name))
                + ",</p>"
                + body
                + "<p style='margin:24px 0 0;color:#3f4857;font-size:14px;line-height:1.6;'>The Plural Team</p>"
                + "</td></tr>"
                + "<tr><td style='padding:18px 20px 22px;font-size:12px;color:#777777;background:#f5f6fa;'>"
                + "&copy; Plural Financial Inc. &middot; Registered with FINTRAC"
                + "</td></tr>"
                + "</table>"
                + "</td></tr>"
                + "</table>"
                + "</body></html>";
    }

    private String row(String label, String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        return "<tr>"
                + "<td style='padding:12px 14px;border-bottom:1px solid #e6eaf1;color:#6b7280;font-size:13px;width:40%;'>"
                + esc(label)
                + "</td>"
                + "<td style='padding:12px 14px;border-bottom:1px solid #e6eaf1;color:#111827;font-size:14px;font-weight:600;'>"
                + esc(value)
                + "</td>"
                + "</tr>";
    }

    private String currencyAmount(String currency, String amount) {
        if (currency == null || currency.trim().isEmpty()) {
            return amount;
        }
        if (amount == null || amount.trim().isEmpty()) {
            return currency;
        }
        return currency + " " + amount;
    }

    private String first(Map<String, Object> data, String... keys) {
        if (data == null || keys == null) {
            return "";
        }
        for (String key : keys) {
            String value = str(data, key);
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        return "";
    }

    private String firstName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "Customer";
        }
        String trimmed = name.trim();
        int space = trimmed.indexOf(' ');
        return space > 0 ? trimmed.substring(0, space) : trimmed;
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
